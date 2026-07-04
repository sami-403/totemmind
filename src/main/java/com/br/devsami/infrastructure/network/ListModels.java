package com.br.devsami.infrastructure.network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço responsável por gerenciar a comunicação de rede com o ecossistema
 * Ollama.
 */
public class ListModels {

    private final String baseUrl;
    private final HttpClient httpClient;
    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * Inicializa o serviço configurando a URL base e o cliente HTTP com timeouts
     * seguros.
     * 
     * @param baseUrl URL onde o serviço Ollama está hospedado (ex:
     *                "http://localhost:11434")
     */
    public ListModels(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("A URL base do Ollama não pode ser nula ou vazia.");
        }
        this.baseUrl = baseUrl.trim().replaceAll("/+$", ""); // Remove barras no final da URL

        // Cliente HTTP reutilizável com timeout de conexão de 10 segundos
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Recupera a lista de modelos disponíveis no Ollama de forma síncrona.
     * 
     * @return List de Strings contendo os nomes dos modelos.
     * @throws IOException          Se ocorrer um erro de rede ou timeout.
     * @throws InterruptedException Se a requisição for interrompida.
     * @throws RuntimeException     Se o servidor responder com um código de erro
     *                              HTTP diferente de 200.
     */
    public List<String> getModels() throws IOException, InterruptedException {
        String endpoint = this.baseUrl + "/api/tags";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15)) // Timeout da requisição
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Falha na requisição Ollama. Código HTTP: " + response.statusCode());
        }

        return extractModelNames(response.body());
    }

    /**
     * Extrai os nomes dos modelos do JSON retornado via Regex de forma segura.
     */
    private List<String> extractModelNames(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank()) {
            return Collections.emptyList();
        }

        List<String> models = new ArrayList<>();
        Matcher matcher = MODEL_NAME_PATTERN.matcher(jsonBody);

        while (matcher.find()) {
            models.add(matcher.group(1));
        }

        return models;
    }
}
