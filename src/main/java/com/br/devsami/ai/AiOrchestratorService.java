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

public class AiOrchestratorService {

    // 1. Interface para feedbacks rápidos de atendimento
    public interface SentimentValidatorAi {
        @SystemMessage(SentimentEspecialist.SentimentPrompt)
        @UserMessage("Sentimento original: {{originalFeeling}}. Texto do feedback: {{feedbackText}}")
        String validateSentiment(@V("originalFeeling") String originalFeeling,
                                  @V("feedbackText") String feedbackText);
    }

    // 2. Interface para inferência rápida de categoria de produto
    public interface ProductCategoryValidatorAi {
        @SystemMessage(ProductSpecialist.ProductCategoryPrompt)
        @UserMessage("Rating (estrelas): {{rating}}. Texto do feedback: {{feedbackText}}")
        String validateProductCategory(@V("rating") String rating,
                                      @V("feedbackText") String feedbackText);
    }

    // 3. Interface para inferência rápida de categoria de atendimento ao cliente (Employee)
    public interface EmployeeCategoryValidatorAi {
        @SystemMessage(EmployeeSpecialist.EmployeeCategoryPrompt)
        @UserMessage("Feeling: {{feeling}}. Texto do feedback: {{feedbackText}}")
        String validateEmployeeCategory(@V("feeling") String feeling,
                                       @V("feedbackText") String feedbackText);
    }

    // 4. Interface para geração de gráficos e B.I
    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT) // O prompt agora foca SÓ em BI
        String chat(@V("dataAtual") String dataAtual, @UserMessage String userMessage);
    }

    private final TotemAssistant assistant;
    private final SentimentValidatorAi sentimentValidator;
    private final ProductCategoryValidatorAi productCategoryValidator;
    private final EmployeeCategoryValidatorAi employeeCategoryValidator;

    public AiOrchestratorService() {
        Properties props = ConfigManager.getInstance();

        String baseUrl = props.getProperty("OLLAMA_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        String modelBase = props.getProperty("AI_MODEL");
        if (modelBase == null || modelBase.isBlank()) {
            modelBase = "hf.co/unsloth/gemma-4-12B-it-qat-GGUF:UD-Q4_K_XL";
        }

        String modelFeedback = props.getProperty("AI_FEEDBACK");
        if (modelFeedback == null || modelFeedback.isBlank()) {
            modelFeedback = "hf.co/ozgurpolat/gemma-4-E4B-it-text-only-GGUF:Q4_K_M";
        }

        // ==========================================
        // CARREGAMENTO DO HISTÓRICO DE MEMÓRIA

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
        // CONFIGURAÇÃO DOS MODELOS DE IA
        // ==========================================

        // Modelo 1: Focado em Chat e BI
        OllamaChatModel biModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelBase).numCtx(4096).temperature(0.2).timeout(Duration.ofSeconds(150)).build();

        this.assistant = AiServices.builder(TotemAssistant.class)
                .chatModel(biModel)
                .tools(new TotemTools())
                .chatMemory(chatMemory) // Histórico
                .build();

        // Modelo 2: Focado em Classificação Rápida
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelFeedback).temperature(0.0).timeout(Duration.ofSeconds(150)).build();

        this.sentimentValidator = AiServices.builder(SentimentValidatorAi.class)
                .chatModel(classificationModel)
                .build();

        this.productCategoryValidator = AiServices.builder(ProductCategoryValidatorAi.class)
                .chatModel(classificationModel)
                .build();

        this.employeeCategoryValidator = AiServices.builder(EmployeeCategoryValidatorAi.class)
                .chatModel(classificationModel)
                .build();
    }

    // Metodo para o BI (usado pelo gerente no dashboard)
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

    public Feeling classificarSentimento(Feeling originalFeeling, String text) {
        String rawResponse = null;
        try {
            rawResponse = sentimentValidator.validateSentiment(originalFeeling.name(), text);
            if (rawResponse == null || rawResponse.isBlank()) {
                return originalFeeling;
            }
            
            // Clean up possible special tokens and tags like <|im_end|>, etc.
            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "") // remove any <|...|> tags
                    .replaceAll("[^a-zA-Z]", "")   // keep only letters (like SATISFIED, DISSATISFIED, NEUTRAL)
                    .toUpperCase();
            
            return Feeling.valueOf(cleanResponse);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao processar sentimento da IA (resposta original: '" + rawResponse + "'): " + e.getMessage());
            return originalFeeling;
        }
    }

    public ProductFeedbackCategory inferirCategoriaProduto(Integer rating, String text) {
        if (text == null || text.isBlank()) {
            return ProductFeedbackCategory.OTHER;
        }
        String rawResponse = null;
        try {
            String ratingStr = (rating != null) ? rating.toString() : "N/A";
            rawResponse = productCategoryValidator.validateProductCategory(ratingStr, text);
            if (rawResponse == null || rawResponse.isBlank()) {
                return ProductFeedbackCategory.OTHER;
            }

            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "")
                    .replaceAll("[^a-zA-Z_]", "")
                    .toUpperCase();

            return ProductFeedbackCategory.valueOf(cleanResponse);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao inferir categoria de produto via IA (resposta original: '" + rawResponse + "'): " + e.getMessage());
            return ProductFeedbackCategory.OTHER;
        }
    }

    public EmployeeFeedbackCategory inferirCategoriaAtendimento(Feeling feeling, String text) {
        if (text == null || text.isBlank()) {
            return EmployeeFeedbackCategory.OTHER;
        }
        String rawResponse = null;
        try {
            String feelingStr = (feeling != null) ? feeling.name() : "NEUTRAL";
            rawResponse = employeeCategoryValidator.validateEmployeeCategory(feelingStr, text);
            if (rawResponse == null || rawResponse.isBlank()) {
                return EmployeeFeedbackCategory.OTHER;
            }

            String cleanResponse = rawResponse.trim()
                    .replaceAll("<\\|.*?\\|>", "")
                    .replaceAll("[^a-zA-Z_]", "")
                    .toUpperCase();

            return EmployeeFeedbackCategory.valueOf(cleanResponse);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao inferir categoria de atendimento via IA (resposta original: '" + rawResponse + "'): " + e.getMessage());
            return EmployeeFeedbackCategory.OTHER;
        }
    }
}