package com.br.devsami.controller;

import com.br.devsami.model.dto.ChatMessage;
import com.br.devsami.view.components.ChatCell;
import com.br.devsami.ai.AiOrchestratorService;
import com.br.devsami.infrastructure.charts.ChartCallback;
import com.br.devsami.infrastructure.charts.ChartManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.List;

import com.br.devsami.model.service.ChatHistoryService;
import com.br.devsami.model.service.ChatHistoryService.MensagemLog;

public class ChatAssistenteController implements ChartCallback {

    @FXML
    private ListView<ChatMessage> chatListView;
    @FXML
    private TextField txtInput;
    @FXML
    private VBox painelGrafico;

    private int qtdMensagensAntigas = 0;

    private final AiOrchestratorService aiService = new AiOrchestratorService();
    private final ChatHistoryService historyService = new ChatHistoryService(); // Instância movida para cima para organização

    @FXML
    public void initialize() {
        ChartManager.registrarTela(this);
        chatListView.setCellFactory(param -> new ChatCell());

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enviarMensagem(null);
            }
        });

        // Carrega o histórico de mensagens guardadas
        List<MensagemLog> historico = historyService.obterUltimasMensagens(10);

        // Cria a lista em tempo de execução para alimentar o componente visual
        List<ChatMessage> mensagensDaSessao = new ArrayList<>();

        // 1. SEMPRE adiciona a saudação inicial no topo
        mensagensDaSessao.add(new ChatMessage("Olá! Sou o Kicer. Como posso ajudar você hoje?", false));

        // 2. Se existirem dados, adiciona as mensagens antigas logo abaixo
        for (MensagemLog log : historico) {
            mensagensDaSessao.add(new ChatMessage(log.texto(), log.isUser()));
        }

        // Alimenta a ListView da interface gráfica de uma só vez
        chatListView.getItems().setAll(mensagensDaSessao);

        Platform.runLater(this::scrollBottom);
    }

    @FXML
    void enviarMensagem(ActionEvent event) {
        String input = txtInput.getText();
        if (input == null || input.isBlank())
            return;

        // 1. Cria a mensagem do utilizador, adiciona na tela e SALVA no JSON
        ChatMessage userMsg = new ChatMessage(input, true);
        chatListView.getItems().add(userMsg);
        txtInput.clear();
        scrollBottom();

        historyService.saveToJson(List.of(userMsg)); // SALVAMENTO IMEDIATO AQUI

        // 2. Processa a IA numa thread separada
        new Thread(() -> {
            try {
                String resposta = aiService.processMessage(input);
                Platform.runLater(() -> {
                    // 3. Cria a mensagem da IA, adiciona na tela e SALVA no JSON
                    ChatMessage aiMsg = new ChatMessage(resposta, false);
                    chatListView.getItems().add(aiMsg);
                    scrollBottom();

                    historyService.saveToJson(List.of(aiMsg)); // SALVAMENTO IMEDIATO AQUI
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // Cria, exibe e SALVA a mensagem de erro da IA também
                    ChatMessage erroMsg = new ChatMessage("Erro: " + e.getMessage(), false);
                    chatListView.getItems().add(erroMsg);
                    historyService.saveToJson(List.of(erroMsg));
                    scrollBottom();
                });
            }
        }).start();
    }

    private void scrollBottom() {
        chatListView.scrollTo(chatListView.getItems().size() - 1);
    }

    // ||Metodo que recebe os dados e desenha grafico
    @Override
    public void exibirGraficoPizza(String titulo, double[] percentagens) {
        PieChart chart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Insatisfeito (" + percentagens[2] + "%)", percentagens[2]),
                new PieChart.Data("Neutro (" + percentagens[1] + "%)", percentagens[1]),
                new PieChart.Data("Satisfeito (" + percentagens[0] + "%)", percentagens[0])));

        chart.setTitle(titulo);

        Platform.runLater(() -> {
            painelGrafico.getChildren().clear();
            painelGrafico.getChildren().add(chart);
        });
    }

    @Override
    public void exibirGraficoLinhas(String titulo, Map<String, int[]> dados) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setPadding(new javafx.geometry.Insets(0, 0, 20, 12));
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(titulo);

        chart.setCreateSymbols(true);
        chart.setAnimated(true);

        XYChart.Series<String, Number> sIns = new XYChart.Series<>();
        sIns.setName("Insatisfeito");

        XYChart.Series<String, Number> sNeu = new XYChart.Series<>();
        sNeu.setName("Neutro");

        XYChart.Series<String, Number> sSat = new XYChart.Series<>();
        sSat.setName("Satisfeito");

        dados.forEach((data, valores) -> {
            sIns.getData().add(new XYChart.Data<>(data, valores[2]));
            sNeu.getData().add(new XYChart.Data<>(data, valores[1]));
            sSat.getData().add(new XYChart.Data<>(data, valores[0]));
        });

        chart.getData().addAll(sIns, sNeu, sSat);

        Platform.runLater(() -> {
            painelGrafico.getChildren().clear();
            painelGrafico.getChildren().add(chart);
        });
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MenuPrincipal.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}