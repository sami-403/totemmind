package com.br.devsami.model.ai;

import com.br.devsami.model.ai.systems.SentimentEspecialist;
import com.br.devsami.model.ai.systems.SystemPrompt;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.cdimascio.dotenv.Dotenv;
import com.br.devsami.utils.enums.Feelling;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;

public class AiOrchestratorService {



    private String baseUrl = "http://localhost:11434";
    private String modelBase = "hf.co/unsloth/gemma-4-E4B-it-qat-GGUF:UD-Q4_K_XL";
    private String modelFeedback = "nemotron-3-nano:4b";

    // 1. Interface para feedbacks rápidos (Sem memória, sem ferramentas, resposta determinística
    // Esse carinha só vai analisar um texto e ver se faz sentido ele ter a classificação do feedback que tem
    public interface SentimentValidatorAi {
        @SystemMessage(SentimentEspecialist.SentimentPrompt)
        @UserMessage("Sentimento original: {{originalFeeling}}. Texto do feedback: {{feedbackText}}")
        Feelling validateSentiment(@V("originalFeeling") String originalFeeling, @V("feedbackText") String feedbackText);
    }

    // 2. Interface para geração de gráficos e B.I (Com memória e ferramentas, focada em Chat/BI)
    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT) // O prompt agora foca SÓ em BI
        String chat(@V("dataAtual") String dataAtual, @UserMessage String userMessage);
    }

    private final TotemAssistant assistant; // chat que o gerente vai pedir gráficos etc
    private final SentimentValidatorAi sentimentValidator; // Novo campo para validação rápida

    public AiOrchestratorService() {

        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/config.properties");) {
            if (input != null) {
                try {
                    props.load(input);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                baseUrl = props.getProperty("OLLAMA_BASE_URL", baseUrl);
                modelBase = props.getProperty("AI_MODEL", modelBase);
                modelFeedback = props.getProperty("AI_FEEDBACK", modelFeedback);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Modelo 1: Focado em Chat e BI (Temperatura 0.2 para ter respostas mais fluidas, mas controladas)
        OllamaChatModel biModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelBase).temperature(0.2).timeout(Duration.ofMinutes(5)).build();

        this.assistant = AiServices.builder(TotemAssistant.class)
                .chatModel(biModel)
                .tools(new TotemTools()) // Ferramentas de banco de dados e gráficos injetadas
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Mantém o contexto da conversa
                .build();

        // Modelo 2: Focado em Classificação Rápida (Temperatura 0.0 para ser estritamente determinístico e não alucinar)
        OllamaChatModel classificationModel = OllamaChatModel.builder()
                .baseUrl(baseUrl).modelName(modelFeedback).temperature(0.0).timeout(Duration.ofMinutes(5)).build();

        this.sentimentValidator = AiServices.builder(SentimentValidatorAi.class)
                .chatModel(classificationModel)
                // NOTA: Sem .tools() e sem .chatMemory() aqui para garantir máxima performance e menor custo de tokens
                .build();
    }

    // Metodo para o BI (usado pelo gerente no dashboard)
    public String processMessage(String message) {
        return assistant.chat(LocalDate.now().toString(), message);
    }

    // Novo metodo exposto para a classificação rápida (usado na hora de salvar o feedback no banco)
    public Feelling classificarSentimento(Feelling originalFeeling, String text) {
        return sentimentValidator.validateSentiment(originalFeeling.name(), text);
    }
}