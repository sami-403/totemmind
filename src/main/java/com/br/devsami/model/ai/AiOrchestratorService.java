package com.br.devsami.model.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import io.github.cdimascio.dotenv.Dotenv;
import dev.langchain4j.service.SystemMessage;
import java.time.Duration;

public class AiOrchestratorService {
    // Carregando as variveis do .env
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private String baseUrl = dotenv.get("OLLAMA_BASE_URL", "http://localhost:11434");
    private String modelBase = dotenv.get("AI_MODEL","gemma4:e4b");

    public interface TotemAssistant {
        @SystemMessage(SystemPrompt.PROMPT)
        String chat(String userMessage);
    }

    private final TotemAssistant assistant;

    public AiOrchestratorService() {
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl) // essa parte vamos usar o '.env' para guardar o end point do modelo
                .modelName(modelBase) // esse modelo suporta a tool calling bem, mas podemos testar outras possibilidades.
                .temperature(0.2) // configura a criatividade do modelo e  a racionalidade.
                .timeout(Duration.ofMinutes(5)) // acredito que esse tempo vai ser um problema no começo, leva um tempo até o modelo carregar no kaggle de fato; dá para aumentar até, mas vamos esperar para testar.
                .build();

        this.assistant = AiServices.builder(TotemAssistant.class)
                .chatModel(model)
                .tools(new TotemTools()) // essa parte importa as tools que estão no arquivo
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // essa parte limita a janela de contexto a 10 menssagens, depois disso ela perde o contexto para não sobrecarregar as gpus t4 de 16 gb.
                .build();
    }

    public String processMessage(String message) {
        return assistant.chat(message);
    }
}