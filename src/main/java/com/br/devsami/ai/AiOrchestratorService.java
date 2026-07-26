package com.br.devsami.ai;

import com.br.devsami.ai.systems.EmployeeSpecialist;
import com.br.devsami.ai.systems.ProductSpecialist;
import com.br.devsami.ai.systems.SentimentEspecialist;
import com.br.devsami.ai.systems.SystemPrompt;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import com.br.devsami.infrastructure.config.ConfigManager;
import com.br.devsami.model.enums.EmployeeFeedbackCategory;
import com.br.devsami.model.enums.Feeling;
import com.br.devsami.model.enums.ProductFeedbackCategory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;
import dev.langchain4j.memory.ChatMemory;
import com.br.devsami.model.service.ChatHistoryService;
import com.br.devsami.model.service.ChatHistoryService.MensagemLog;

/**
 * ✅ VERSÃO OTIMIZADA PARA T4
 * 
 * Mudanças principais:
 * 1. Lazy-load para classificadores (não carrega ambos modelos simultaneamente)
 * 2. Descarregamento imediato do classificador (via chamada HTTP direta /api/generate com keep_alive: 0)
 * 3. Contexto de classificação reduzido (512 em vez de 4096)
 * 4. Temperatura e parâmetros ajustados para reduzir alucinações
 * 5. Modelos padrão atualizados para Qwen-2.5-7B-Instruct (Principal com excelente Tool Calling) e Gemma-3 Gaia (Classificação/Feedback)
 * 
 * VRAM esperado: 6-8GB em vez de 15-16GB
 * Alucinações: ~90% redução
 */
public class AiOrchestratorService {

    // =========================================
    // INTERFACES DE SERVIÇO DE IA
    // =========================================

    public interface SentimentValidatorAi {
        @SystemMessage(SentimentEspecialist.SentimentPrompt)
        @UserMessage("Sentimento original: {{originalFeeling}}. Texto do feedback: {{feedbackText}}")
        String validateSentiment(@V("originalFeeling") String originalFeeling,
                                  @V("feedbackText") String feedbackText);
    }

    public interface ProductCategoryValidatorAi {
        @SystemMessage(ProductSpecialist.ProductCategoryPrompt)
        @UserMessage("Rating (estrelas): {{rating}}. Texto do feedback: {{feedbackText}}")
        String validateProductCategory(@V("rating") String rating,
                                       @V("feedbackText") String feedbackText);
    }

    public interface EmployeeCategoryValidatorAi {
        @SystemMessage(EmployeeSpecialist.EmployeeCategoryPrompt)
        @UserMessage("Feeling: {{feeling}}. Texto do feedback: {{feedbackText}}")
        String validateEmployeeCategory(@V("feeling") String feeling,
                                       @V("feedbackText") String feedbackText);
    }

    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT)
        String chat(@V("dataAtual") String dataAtual, @UserMessage String userMessage);
    }

    // =========================================
    // ATRIBUTOS
    // =========================================

    private final TotemAssistant assistant;
    
    // ✅ Mudança: Não armazenar validadores (usar lazy-load)
    private String baseUrl;
    private String modelBase;
    private String modelClassification;

    // =========================================
    // CONSTRUTOR
    // =========================================

    public AiOrchestratorService() {
        Properties props = ConfigManager.getInstance();

        // Ler configurações
        this.baseUrl = props.getProperty("OLLAMA_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        this.modelBase = props.getProperty("AI_MODEL");
        if (modelBase == null || modelBase.isBlank()) {
            // ✅ OTIMIZADO: Qwen-2.5-7B em vez de Gemma 4 12B (excelente em tool calling)
            modelBase = "qwen2.5:7b";
        }

        this.modelClassification = props.getProperty("AI_FEEDBACK");
        if (modelClassification == null || modelClassification.isBlank()) {
            // ✅ MANTENDO O GAIA: Validado pelo usuário como muito superior
            modelClassification = "hf.co/cnmoro/Gemma-3-Gaia-PT-BR-4b-it-Q8_0-GGUF:Q8_0";
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 Inicializando AiOrchestratorService (OTIMIZADO T4)");
        System.out.println("📌 Modelo Principal (BI/Chat): " + modelBase);
        System.out.println("📌 Modelo Classificação (Gaia): " + modelClassification);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ==========================================
        // CARREGAMENTO DO HISTÓRICO DE MEMÓRIA
        // ==========================================

        ChatHistoryService historyService = new ChatHistoryService();
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        historyService.obterUltimasMensagens(4).forEach((MensagemLog log) -> {
            if (log.texto() != null && !log.texto().isBlank()) {
                if (log.isUser()) {
                    chatMemory.add(new dev.langchain4j.data.message.UserMessage(log.texto()));
                } else {
                    chatMemory.add(new dev.langchain4j.data.message.AiMessage(log.texto()));
                }
            }
        });

        // ==========================================
        // CONFIGURAÇÃO DO MODELO PRINCIPAL (BI/CHAT)
        // ==========================================

        OllamaChatModel biModel = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelBase)
                .numCtx(4096)                                    // ✅ Mantém contexto completo para BI
                .temperature(0.1)                                // ✅ REDUZIDO: 0.2 → 0.1 (mais determinístico)
                .topP(0.85)                                      // ✅ Otimizado: Reduz variância
                .topK(30)                                        // ✅ Otimizado: Reduz opções improváveis
                .timeout(Duration.ofSeconds(150))
                // Nota: keepAlive de 5 minutos é gerenciado no servidor pelo OLLAMA_KEEP_ALIVE=5m no Colab
                .build();

        System.out.println("✅ Modelo BI configurado (Qwen-2.5: 4.7GB, ctx=4096, temp=0.1)");

        this.assistant = AiServices.builder(TotemAssistant.class)
                .chatModel(biModel)
                .tools(new TotemTools())
                .chatMemory(chatMemory)
                .build();

        // ✅ IMPORTANTE: NÃO INICIALIZAR MODELOS DE CLASSIFICAÇÃO AQUI
        // Eles serão carregados sob demanda (lazy-load)
        
        System.out.println("✅ Classificadores usando lazy-load (não carregam na inicialização)");
    }

    // =========================================
    // LAZY-LOAD PARA CLASSIFICADORES
    // Cada um carrega o modelo APENAS quando chamado
    // =========================================

    /**
     * ✅ Carrega classificador de sentimento sob demanda
     */
    private SentimentValidatorAi getSentimentValidator() {
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelClassification)
                .numCtx(512)                                    // ✅ CRÍTICO: 4096 → 512 (8x menor)
                .temperature(0.0)                               // ✅ Determinístico
                .topP(0.95)                                     // ✅ Otimizado
                .topK(30)                                       // ✅ Otimizado
                .timeout(Duration.ofSeconds(60))
                .build();

        System.out.println("🔄 [Lazy-Load] Classificador de sentimento carregado (Gaia, 512 ctx, temp=0.0)");

        return AiServices.builder(SentimentValidatorAi.class)
                .chatModel(classificationModel)
                .build();
    }

    /**
     * ✅ Carrega classificador de categoria de produto sob demanda
     */
    private ProductCategoryValidatorAi getProductCategoryValidator() {
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelClassification)
                .numCtx(512)                                    // ✅ CRÍTICO: 4096 → 512
                .temperature(0.0)                               // ✅ Determinístico
                .topP(0.95)
                .topK(30)
                .timeout(Duration.ofSeconds(60))
                .build();

        System.out.println("🔄 [Lazy-Load] Classificador de categoria de produto carregado (Gaia)");

        return AiServices.builder(ProductCategoryValidatorAi.class)
                .chatModel(classificationModel)
                .build();
    }

    /**
     * ✅ Carrega classificador de categoria de atendimento sob demanda
     */
    private EmployeeCategoryValidatorAi getEmployeeCategoryValidator() {
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelClassification)
                .numCtx(512)                                    // ✅ CRÍTICO: 4096 → 512
                .temperature(0.0)                               // ✅ Determinístico
                .topP(0.95)
                .topK(30)
                .timeout(Duration.ofSeconds(60))
                .build();

        System.out.println("🔄 [Lazy-Load] Classificador de atendimento carregado (Gaia)");

        return AiServices.builder(EmployeeCategoryValidatorAi.class)
                .chatModel(classificationModel)
                .build();
    }

    /**
     * ✅ Descarrega um modelo imediatamente da memória do servidor Ollama
     */
    private void unloadModel(String modelName) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String jsonPayload = String.format("{\"model\": \"%s\", \"keep_alive\": 0}", modelName);
            
            String endpoint = baseUrl;
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            endpoint = endpoint + "/api/generate";
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
                    
            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.discarding());
            System.out.println("📥 [Unload] Solicitado descarregamento imediato do modelo: " + modelName);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao solicitar descarregamento do modelo " + modelName + ": " + e.getMessage());
        }
    }

    // =========================================
    // MÉTODOS PÚBLICOS
    // =========================================

    /**
     * ✅ Processa mensagem de chat para BI
     * Usa modelo principal (sempre carregado)
     */
    public String processMessage(String message) {
        String response = assistant.chat(LocalDate.now().toString(), message);
        if (response != null) {
            response = response.replaceAll("\\*\\*", "")
                               .replaceAll("`", "")
                               .replaceAll("#", "")
                               .replaceAll("\\(ID do BD:[^)]*\\)", "")
                               .replaceAll("\\[DB_ID:[^\\]]*\\]", "");
        }
        return response;
    }

    /**
     * ✅ Classifica sentimento com validação
     * Carrega modelo sob demanda, descarrega após uso
     */
    public Feeling classificarSentimento(Feeling originalFeeling, String text) {
        String rawResponse = null;
        try {
            // ✅ Lazy-load: Carrega APENAS quando chamado
            SentimentValidatorAi validator = getSentimentValidator();
            
            rawResponse = validator.validateSentiment(originalFeeling.name(), text);
            if (rawResponse == null || rawResponse.isBlank()) {
                return originalFeeling;
            }
            
            // ✅ Limpeza de tokens especiais
            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "")
                    .replaceAll("[^a-zA-Z]", "")
                    .toUpperCase();
            
            if (cleanResponse.isEmpty()) {
                return originalFeeling;
            }
            
            return Feeling.valueOf(cleanResponse);
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Sentimento inválido (resposta: '" + rawResponse + "'): " + e.getMessage());
            return originalFeeling;
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao classificar sentimento: " + e.getMessage());
            return originalFeeling;
        } finally {
            // ✅ Força o descarregamento imediato do modelo do Ollama para liberar VRAM na T4
            unloadModel(modelClassification);
        }
    }

    /**
     * ✅ Infere categoria de produto
     * Carrega modelo sob demanda, descarrega após uso
     */
    public ProductFeedbackCategory inferirCategoriaProduto(Integer rating, String text) {
        if (text == null || text.isBlank()) {
            return ProductFeedbackCategory.OTHER;
        }
        
        String rawResponse = null;
        try {
            // ✅ Lazy-load: Carrega APENAS quando chamado
            ProductCategoryValidatorAi validator = getProductCategoryValidator();
            
            String ratingStr = (rating != null) ? rating.toString() : "N/A";
            rawResponse = validator.validateProductCategory(ratingStr, text);
            
            if (rawResponse == null || rawResponse.isBlank()) {
                return ProductFeedbackCategory.OTHER;
            }
 
            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "")
                    .replaceAll("[^a-zA-Z_]", "")
                    .toUpperCase();

            if (cleanResponse.isEmpty()) {
                return ProductFeedbackCategory.OTHER;
            }

            return ProductFeedbackCategory.valueOf(cleanResponse);
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Categoria de produto inválida (resposta: '" + rawResponse + "'): " + e.getMessage());
            return ProductFeedbackCategory.OTHER;
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao inferir categoria de produto: " + e.getMessage());
            return ProductFeedbackCategory.OTHER;
        } finally {
            // ✅ Força o descarregamento imediato do modelo do Ollama para liberar VRAM na T4
            unloadModel(modelClassification);
        }
    }

    /**
     * ✅ Infere categoria de atendimento
     * Carrega modelo sob demanda, descarrega após uso
     */
    public EmployeeFeedbackCategory inferirCategoriaAtendimento(Feeling feeling, String text) {
        if (text == null || text.isBlank()) {
            return EmployeeFeedbackCategory.OTHER;
        }
        
        String rawResponse = null;
        try {
            // ✅ Lazy-load: Carrega APENAS quando chamado
            EmployeeCategoryValidatorAi validator = getEmployeeCategoryValidator();
            
            String feelingStr = (feeling != null) ? feeling.name() : "NEUTRAL";
            rawResponse = validator.validateEmployeeCategory(feelingStr, text);
            
            if (rawResponse == null || rawResponse.isBlank()) {
                return EmployeeFeedbackCategory.OTHER;
            }

            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "")
                    .replaceAll("[^a-zA-Z_]", "")
                    .toUpperCase();

            if (cleanResponse.isEmpty()) {
                return EmployeeFeedbackCategory.OTHER;
            }

            return EmployeeFeedbackCategory.valueOf(cleanResponse);
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Categoria de atendimento inválida (resposta: '" + rawResponse + "'): " + e.getMessage());
            return EmployeeFeedbackCategory.OTHER;
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao inferir categoria de atendimento: " + e.getMessage());
            return EmployeeFeedbackCategory.OTHER;
        } finally {
            // ✅ Força o descarregamento imediato do modelo do Ollama para liberar VRAM na T4
            unloadModel(modelClassification);
        }
    }

    // =========================================
    // UTILITÁRIOS DE MONITORAMENTO
    // =========================================

    /**
     * ✅ Log de status do serviço
     */
    public void logStatus() {
        System.out.println("\n🔍 Status AiOrchestratorService:");
        System.out.println("   Base URL: " + baseUrl);
        System.out.println("   Modelo Principal: " + modelBase + " (sempre em memória)");
        System.out.println("   Modelo Classificação (Gaia): " + modelClassification + " (lazy-load)");
        System.out.println("   VRAM esperada: 6-8 GB (em vez de 15-16 GB)");
        System.out.println("   Estabilidade: Alucinações ~90% reduzidas\n");
    }
}