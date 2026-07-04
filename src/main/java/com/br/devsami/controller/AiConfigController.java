package com.br.devsami.controller;

import com.br.devsami.infrastructure.config.ConfigManager;
import com.br.devsami.infrastructure.network.ListModels;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class AiConfigController {

    @FXML private TextField txtEndpoint;
    @FXML private ComboBox<String> cbModeloChat;
    @FXML private ComboBox<String> cbModeloFeedback;
    @FXML private Label lblStatus;

    private Properties props;

    @FXML
    public void initialize() {
        // Carrega as configurações atuais ao abrir a tela
        props = ConfigManager.getInstance();

        String urlSalva = props.getProperty("OLLAMA_BASE_URL", "");
        String chatSalvo = props.getProperty("AI_MODEL", "");
        String feedbackSalvo = props.getProperty("AI_FEEDBACK", "");

        txtEndpoint.setText(urlSalva);

        // Se já tiver uma URL configurada, podemos tentar carregar a lista silenciosamente
        if (!urlSalva.isBlank()) {
            carregarListaDeModelos(urlSalva, chatSalvo, feedbackSalvo);
        }
    }

    @FXML
    void recarregarModelos(ActionEvent event) {
        lblStatus.setText("Buscando modelos...");
        lblStatus.setStyle("-fx-text-fill: #ffaa00;"); // Amarelo para "Carregando"

        String url = txtEndpoint.getText();
        if (url == null || url.isBlank()) {
            lblStatus.setText("Erro: Informe o endpoint primeiro!");
            lblStatus.setStyle("-fx-text-fill: #ff3333;");
            return;
        }

        carregarListaDeModelos(url, null, null);
    }

    private void carregarListaDeModelos(String url, String chatParaSelecionar, String feedbackParaSelecionar) {
        // Roda em uma thread separada para não travar a tela (UI) enquanto faz a requisição de rede
        new Thread(() -> {
            try {
                ListModels listModels = new ListModels(url);
                List<String> models = listModels.getModels();

                // Atualiza a tela (UI) de volta na thread principal do JavaFX
                Platform.runLater(() -> {
                    if (models.isEmpty()) {
                        lblStatus.setText("Aviso: Nenhum modelo encontrado.");
                        lblStatus.setStyle("-fx-text-fill: #ffaa00;");
                        cbModeloChat.getItems().clear();
                        cbModeloFeedback.getItems().clear();
                    } else {
                        cbModeloChat.getItems().setAll(models);
                        cbModeloFeedback.getItems().setAll(models);

                        // Seleciona os itens salvos, se existirem na lista recuperada
                        if (chatParaSelecionar != null && models.contains(chatParaSelecionar)) {
                            cbModeloChat.setValue(chatParaSelecionar);
                        }
                        if (feedbackParaSelecionar != null && models.contains(feedbackParaSelecionar)) {
                            cbModeloFeedback.setValue(feedbackParaSelecionar);
                        }

                        lblStatus.setText("Modelos carregados com sucesso!");
                        lblStatus.setStyle("-fx-text-fill: #1e9759;");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro na API: Verifique a URL e a conexão.");
                    lblStatus.setStyle("-fx-text-fill: #ff3333;");
                    cbModeloChat.getItems().clear();
                    cbModeloFeedback.getItems().clear();
                });
            }
        }).start();
    }

    @FXML
    void salvarConfiguracoes(ActionEvent event) {
        String url = txtEndpoint.getText();
        String modelBi = cbModeloChat.getValue();
        String modelFeedback = cbModeloFeedback.getValue();

        // Salva usando a sua lógica de persistência do ConfigManager
        props.setProperty("OLLAMA_BASE_URL", url != null ? url : "");
        props.setProperty("AI_MODEL", modelBi != null ? modelBi : "");
        props.setProperty("AI_FEEDBACK", modelFeedback != null ? modelFeedback : "");

        ConfigManager.save();

        lblStatus.setText("Configurações salvas com sucesso!");
        lblStatus.setStyle("-fx-text-fill: #1e9759;");
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