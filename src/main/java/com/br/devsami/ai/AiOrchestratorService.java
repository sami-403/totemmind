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

public class AiOrchestratorService {

    // 1. Interface para feedbacks rápidos (Sem memória, sem ferramentas, resposta
    // determinística
    // Esse carinha só vai analisar um texto e ver se faz sentido ele ter a
    // classificação do feedback que tem
    public interface SentimentValidatorAi {
        @SystemMessage(SentimentEspecialist.SentimentPrompt)
        @UserMessage("Sentimento original: {{originalFeeling}}. Texto do feedback: {{feedbackText}}")
        Feeling validateSentiment(@V("originalFeeling") String originalFeeling,
                                  @V("feedbackText") String feedbackText);
    }

    // 2. Interface para geração de gráficos e B.I (Com memória e ferramentas,
    // focada em Chat/BI)
    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT) // O prompt agora foca SÓ em BI
        String chat(@V("dataAtual") String dataAtual, @UserMessage String userMessage);
    }

    private final TotemAssistant assistant; // chat que o gerente vai pedir gráficos etc
    private final SentimentValidatorAi sentimentValidator; // Novo campo para validação rápida

    public AiOrchestratorService() {
        Properties props = ConfigManager.getInstance();

        String baseUrl = props.getProperty("OLLAMA_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        String modelBase = props.getProperty("AI_MODEL");
        if (modelBase == null || modelBase.isBlank()) {
            modelBase = "hf.co/unsloth/gemma-4-E4B-it-qat-GGUF:UD-Q4_K_XL";
        }

        String modelFeedback = props.getProperty("AI_FEEDBACK");
        if (modelFeedback == null || modelFeedback.isBlank()) {
            modelFeedback = "hf.co/ozgurpolat/gemma-4-E4B-it-text-only-GGUF:Q4_K_M";
        }

        // Modelo 1: Focado em Chat e BI (Temperatura 0.2 para ter respostas mais
        // fluidas, mas controladas)
        OllamaChatModel biModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelBase).temperature(0.2).timeout(Duration.ofMinutes(5)).build();

        this.assistant = AiServices.builder(TotemAssistant.class)
                .chatModel(biModel)
                .tools(new TotemTools()) // Ferramentas de banco de dados e gráficos injetadas
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Mantém o contexto da conversa
                .build();

        // Modelo 2: Focado em Classificação Rápida (Temperatura 0.0 para ser
        // estritamente determinístico e não alucinar)
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelFeedback).temperature(0.0).timeout(Duration.ofMinutes(5)).build();

        this.sentimentValidator = AiServices.builder(SentimentValidatorAi.class)
                .chatModel(classificationModel)
                // NOTA: Sem .tools() e sem .chatMemory() aqui para garantir máxima performance
                // com menor custo de tokens
                .build();
    }

    // Metodo para o BI (usado pelo gerente no dashboard)
    public String processMessage(String message) {
        return assistant.chat(LocalDate.now().toString(), message);
    }

    // Novo metodo exposto para a classificação rápida (usado na hora de salvar o
    // feedback no banco)
    public Feeling classificarSentimento(Feeling originalFeeling, String text) {
        return sentimentValidator.validateSentiment(originalFeeling.name(), text);
    }
}