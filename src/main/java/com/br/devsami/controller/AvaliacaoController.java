package com.br.devsami.controller;

import com.br.devsami.model.repository.UserRepository;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.br.devsami.util.CpfValidator;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AvaliacaoController {

    @FXML
    private TextField txtCpf;

    @FXML
    public void initialize() {

    }

    // referente à tela inical dos feedbacks, ao qual coloca o cpf
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

    @FXML
    void seguirParaAvaliacao(ActionEvent event) {
        String cpf = txtCpf.getText();
        String validationError = CpfValidator.validate(cpf);
        if (validationError != null) {
            showAlert(Alert.AlertType.WARNING, "Erro de Validação", validationError);
            return;
        }

        // Desabilita temporariamente o campo para evitar cliques duplicados
        txtCpf.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            UserRepository userRepository = new UserRepository();
            return userRepository.existsByCpf(cpf);
        }).thenAccept(exists -> Platform.runLater(() -> {
            txtCpf.setDisable(false);
            if (exists) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FeedbackMenu.fxml"));
                    Parent root = loader.load();

                    // Passa o CPF para o próximo Controller
                    FeedbackMenuController controller = loader.getController();
                    controller.setCpfCliente(cpf);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CadastroCliente.fxml"));
                    Parent root = loader.load();

                    CadastroClienteController controller = loader.getController();
                    controller.setCpf(cpf);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                txtCpf.setDisable(false);
                ex.printStackTrace();
            });
            return null;
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}