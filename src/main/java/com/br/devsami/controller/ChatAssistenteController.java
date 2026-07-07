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
import javafx.scene.chart.PieChart;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatAssistenteController implements ChartCallback {

    @FXML
    private ListView<ChatMessage> chatListView;
    @FXML
    private TextField txtInput;
    @FXML
    private VBox painelGrafico; // O painel da direita no FXML

    private final AiOrchestratorService aiService = new AiOrchestratorService();

    @FXML
    public void initialize() {
        // Registra a tela no gerenciador
        ChartManager.registrarTela(this);

        chatListView.setCellFactory(param -> new ChatCell());

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enviarMensagem(null);
            }
        });

        chatListView.getItems().add(
                new ChatMessage("Olá! Sou o Kicer. Como posso ajudar você hoje?", false));
    }

    @FXML
    void enviarMensagem(ActionEvent event) {
        String input = txtInput.getText();
        if (input == null || input.isBlank())
            return;

        chatListView.getItems().add(new ChatMessage(input, true));
        txtInput.clear();
        scrollBottom();

        new Thread(() -> {
            try {
                String resposta = aiService.processMessage(input);
                Platform.runLater(() -> {
                    chatListView.getItems().add(new ChatMessage(resposta, false));
                    scrollBottom();
                });
            } catch (Exception e) {
                Platform.runLater(() -> chatListView.getItems().add(new ChatMessage("Erro: " + e.getMessage(), false)));
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

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}