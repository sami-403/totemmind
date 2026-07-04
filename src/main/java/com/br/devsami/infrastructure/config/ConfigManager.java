package com.br.devsami.infrastructure.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

// Gerencia as configurações da aplicação num arquivo externo multiplataforma.
public class ConfigManager {

    private static final String DIR_NAME = ".totemassets";
    private static final String FILE_NAME = "config.properties";
    private static Properties properties;

    // Retorna a instância única (Singleton) carregada em memória.
    public static synchronized Properties getInstance() {
        if (properties != null)
            return properties;

        properties = new Properties();
        // Resolve o caminho dinamicamente conforme o sistema operativo (Windows/Linux)
        Path extPath = Paths.get(System.getProperty("user.home"), DIR_NAME, FILE_NAME);

        try {
            // Primeira execução: cria o diretoria e gera o arquivo com chaves vazias
            if (!Files.exists(extPath)) {
                Files.createDirectories(extPath.getParent());

                properties.setProperty("OLLAMA_BASE_URL", "");
                properties.setProperty("AI_MODEL", "");
                properties.setProperty("AI_FEEDBACK", "");

                save();
            } else {
                // Execuções futuras: carrega o ficheiro físico
                try (Reader reader = Files.newBufferedReader(extPath, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
        }

        return properties;
    }

    // Persiste as alteracoes feitas em memoria no ficheiro fisico
    public static synchronized void save() {
        if (properties == null)
            return;
        Path extPath = Paths.get(System.getProperty("user.home"), DIR_NAME, FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(extPath, StandardCharsets.UTF_8)) {
            properties.store(writer, "Configuracoes TotemMind");
        } catch (IOException e) {
            System.err.println("Erro ao guardar: " + e.getMessage());
        }
    }
}