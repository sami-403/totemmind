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
import javafx.scene.layout.HBox;
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

    private final AiOrchestratorService aiService = new AiOrchestratorService();
    private final ChatHistoryService historyService = new ChatHistoryService();

    @FXML
    public void initialize() {
        ChartManager.registrarTela(this);
        chatListView.setCellFactory(param -> new ChatCell());

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enviarMensagem(null);
            }
        });

        List<MensagemLog> historico = historyService.obterUltimasMensagens(15);
        List<ChatMessage> mensagensDaSessao = new ArrayList<>();

        // Adiciona sempre a saudação fixa no topo
        mensagensDaSessao.add(new ChatMessage("Olá! Sou o Kicer. Como posso ajudar você hoje?", false));

        // Incorpora todo o histórico salvo abaixo da saudação
        for (MensagemLog log : historico) {
            mensagensDaSessao.add(new ChatMessage(log.texto(), log.isUser()));
        }

        chatListView.getItems().setAll(mensagensDaSessao);
        Platform.runLater(this::scrollBottom);
    }

    @FXML
    void enviarMensagem(ActionEvent event) {
        String input = txtInput.getText();
        if (input == null || input.isBlank())
            return;

        ChatMessage userMsg = new ChatMessage(input, true);
        chatListView.getItems().add(userMsg);
        txtInput.clear();
        scrollBottom();

        // Salva o envio incremental imediatamente
        historyService.saveToJson(List.of(userMsg));

        new Thread(() -> {
            try {
                String resposta = aiService.processMessage(input);
                Platform.runLater(() -> {
                    ChatMessage aiMsg = new ChatMessage(resposta, false);
                    chatListView.getItems().add(aiMsg);
                    scrollBottom();
                    historyService.saveToJson(List.of(aiMsg));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ChatMessage erroMsg = new ChatMessage("Erro: " + e.getMessage(), false);
                    chatListView.getItems().add(erroMsg);
                    scrollBottom();
                    historyService.saveToJson(List.of(erroMsg));
                });
            }
        }).start();
    }

    private void scrollBottom() {
        chatListView.scrollTo(chatListView.getItems().size() - 1);
    }

    @Override
    public void exibirGraficoPizza(String titulo, double[] percentagens) {
        javafx.collections.ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();

        if (percentagens.length == 7) {
            // Categorias de Produtos: QUALITY, TEMPERATURE, PORTION, PACKAGING, PRICE, PRAISE, OTHER
            String[] labels = {
                    "Qualidade/Sabor", "Temperatura", "Porção", "Embalagem", "Preço", "Elogios", "Outros"
            };
            for (int i = 0; i < percentagens.length; i++) {
                if (percentagens[i] > 0) {
                    dataList.add(new PieChart.Data(labels[i] + " (" + percentagens[i] + "%)", percentagens[i]));
                }
            }
        } else if (percentagens.length == 5) {
            // Distribuição de Estrelas do Produto: 5 ⭐, 4 ⭐, 3 ⭐, 2 ⭐, 1 ⭐
            String[] labels = {"5 ⭐ (Excelente)", "4 ⭐ (Muito Bom)", "3 ⭐ (Bom)", "2 ⭐ (Ruim)", "1 ⭐ (Péssimo)"};
            for (int i = 0; i < percentagens.length; i++) {
                if (percentagens[i] > 0) {
                    dataList.add(new PieChart.Data(labels[i] + " (" + percentagens[i] + "%)", percentagens[i]));
                }
            }
        } else if (percentagens.length >= 3) {
            // Sentimento de Funcionários: 0=Satisfeito, 1=Neutro, 2=Insatisfeito
            dataList.add(new PieChart.Data("Insatisfeito (" + percentagens[2] + "%)", percentagens[2]));
            dataList.add(new PieChart.Data("Neutro (" + percentagens[1] + "%)", percentagens[1]));
            dataList.add(new PieChart.Data("Satisfeito (" + percentagens[0] + "%)", percentagens[0]));
        }

        PieChart chart = new PieChart(dataList);
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

    @Override
    public void exibirCardsProdutos(String titulo, List<com.br.devsami.model.dto.ProductCardData> produtos) {
        Platform.runLater(() -> {
            painelGrafico.getChildren().clear();

            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(titulo);
            titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10px 0;");

            VBox cardsContainer = new VBox(12);
            cardsContainer.setPadding(new javafx.geometry.Insets(5));

            if (produtos == null || produtos.isEmpty()) {
                javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("Nenhum produto a exibir.");
                emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
                cardsContainer.getChildren().add(emptyLabel);
            } else {
                for (com.br.devsami.model.dto.ProductCardData p : produtos) {
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: #2c2c2e; -fx-border-color: #444444; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 12px;");

                    // Linha Superior: Nome e Preço
                    HBox topRow = new HBox(10);
                    topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(p.name());
                    nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-wrap-text: true;");
                    HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

                    String priceText = p.price() != null ? String.format("R$ %.2f", p.price()) : "N/A";
                    javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(priceText);
                    priceLabel.setStyle("-fx-background-color: #1e9759; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 3px 8px; -fx-background-radius: 12px;");

                    topRow.getChildren().addAll(nameLabel, priceLabel);

                    // Linha Inferior: Estrelas e Total de Avaliações
                    HBox bottomRow = new HBox(8);
                    bottomRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    org.kordamp.ikonli.javafx.FontIcon starIcon = new org.kordamp.ikonli.javafx.FontIcon("mdi-star");
                    starIcon.setIconColor(javafx.scene.paint.Color.web("#f39c12"));
                    starIcon.setIconSize(16);

                    String ratingText = String.format("%.1f ⭐", p.ratingAverage());
                    javafx.scene.control.Label ratingLabel = new javafx.scene.control.Label(ratingText);
                    ratingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px; -fx-font-weight: bold;");

                    String feedbackCountText = String.format("(%d %s)", p.totalFeedbacks(), p.totalFeedbacks() == 1 ? "avaliação" : "avaliações");
                    javafx.scene.control.Label countLabel = new javafx.scene.control.Label(feedbackCountText);
                    countLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

                    bottomRow.getChildren().addAll(starIcon, ratingLabel, countLabel);

                    card.getChildren().addAll(topRow, bottomRow);
                    cardsContainer.getChildren().add(card);
                }
            }

            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(cardsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

            painelGrafico.getChildren().addAll(titleLabel, scrollPane);
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