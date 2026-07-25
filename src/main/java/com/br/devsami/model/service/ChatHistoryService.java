package com.br.devsami.model.service;

import com.br.devsami.model.dto.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatHistoryService {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = Paths.get(System.getProperty("user.home"), ".totemassets", "historico_chat.json");

    public record MensagemLog(String data, String texto, boolean isUser) {}

    public void saveToJson(List<ChatMessage> novasMensagens) {
        List<MensagemLog> historico = carregarEFiltrarHistorico();
        String hoje = LocalDate.now().toString();

        novasMensagens.stream()
                .filter(m -> m.text() != null && !m.text().isBlank())
                .forEach(m -> historico.add(new MensagemLog(hoje, m.text(), m.isUser())));

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(historico, writer);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }

    private List<MensagemLog> carregarEFiltrarHistorico() {
        if (!Files.exists(path) || path.toFile().length() == 0) {
            return new ArrayList<>(); // Arquivo não existe ou está vazio
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            List<MensagemLog> logs = gson.fromJson(reader, new TypeToken<List<MensagemLog>>(){}.getType());
            if (logs == null) return new ArrayList<>();

            LocalDate limite = LocalDate.now().minusDays(14);
            return logs.stream()
                    .filter(log -> log.data() != null && !LocalDate.parse(log.data()).isBefore(limite))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("ARQUIVO JSON CORROMPIDO. Lendo como vazio. Erro: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MensagemLog> obterUltimasMensagens(int quantidade) {
        List<MensagemLog> historico = carregarEFiltrarHistorico();
        if (historico.isEmpty()) return new ArrayList<>();
        return historico.subList(Math.max(0, historico.size() - quantidade), historico.size());
    }
}