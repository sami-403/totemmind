package com.br.devsami.ai;

import com.br.devsami.ai.systems.SentimentEspecialist;
import com.br.devsami.ai.systems.SystemPrompt;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import com.br.devsami.infrastructure.config.ConfigManager;
import com.br.devsami.model.enums.Feeling;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;
import dev.langchain4j.memory.ChatMemory;
import com.br.devsami.model.service.ChatHistoryService;
import com.br.devsami.model.service.ChatHistoryService.MensagemLog;

public class AiOrchestratorService {

    // 1. Interface para feedbacks rápidos
    public interface SentimentValidatorAi {
        @SystemMessage(SentimentEspecialist.SentimentPrompt)
        @UserMessage("Sentimento original: {{originalFeeling}}. Texto do feedback: {{feedbackText}}")
        Feeling validateSentiment(@V("originalFeeling") String originalFeeling,
                                  @V("feedbackText") String feedbackText);
    }

    // 2. Interface para geração de gráficos e B.I
    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT) // O prompt agora foca SÓ em BI
        String chat(@V("dataAtual") String dataAtual, @UserMessage String userMessage);
    }

    private final TotemAssistant assistant;
    private final SentimentValidatorAi sentimentValidator;

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
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(15);

        historyService.obterUltimasMensagens(6).forEach((MensagemLog log) -> {
            if (log.isUser()) {

                chatMemory.add(new dev.langchain4j.data.message.UserMessage(log.texto()));
            } else {
                chatMemory.add(new dev.langchain4j.data.message.AiMessage(log.texto()));
            }
        });

        // ==========================================
        // CONFIGURAÇÃO DOS MODELOS DE IA
        // ==========================================

        // Modelo 1: Focado em Chat e BI
        OllamaChatModel biModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelBase).temperature(0.2).timeout(Duration.ofSeconds(150)).build();

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
    }

    // Metodo para o BI (usado pelo gerente no dashboard)
    public String processMessage(String message) {
        return assistant.chat(LocalDate.now().toString(), message);
    }

    public Feeling classificarSentimento(Feeling originalFeeling, String text) {
        return sentimentValidator.validateSentiment(originalFeeling.name(), text);
    }
}