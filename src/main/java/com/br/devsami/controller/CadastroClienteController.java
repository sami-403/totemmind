package com.br.devsami.controller;

import com.br.devsami.model.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class CadastroClienteController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private DatePicker dpNascimento;

    private UserService userService;

    public void initialize() {
        this.userService = new UserService();
    }

    public void setCpf(String cpf) {
        if (cpf != null) {
            txtCpf.setText(cpf);
            txtCpf.setDisable(true);
        }
    }

    @FXML
    private void handleConcluir(ActionEvent event) {
        String nome = txtNome.getText();
        String cpf = txtCpf.getText();
        LocalDate nascimento = dpNascimento.getValue();

        if (nome.isBlank() || cpf.isBlank() || nascimento == null) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Todos os campos são obrigatórios.");
            return;
        }

        // CPF Validation Check
        String validationError = com.br.devsami.util.CpfValidator.validate(cpf);
        if (validationError != null) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", validationError);
            return;
        }

        try {
            userService.createUser(nome, cpf, nascimento);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Cadastrado com sucesso.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FeedbackMenu.fxml"));
            Parent root = loader.load();

            FeedbackMenuController controller = loader.getController();
            controller.setCpfCliente(cpf);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/TelaAvaliacao.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}