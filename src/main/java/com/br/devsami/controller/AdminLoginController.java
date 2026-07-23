package com.br.devsami.controller;

import com.br.devsami.model.service.EmployeeService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.br.devsami.util.CpfValidator;
import java.io.IOException;

public class AdminLoginController {

    @FXML private TextField cpfField;
    @FXML private PasswordField senhaField;

    private final EmployeeService service = new EmployeeService();

    @FXML
    private void handleLogin() {
        String cpf = cpfField.getText();
        String senha = senhaField.getText();

        if (cpf == null || cpf.isBlank() || senha == null || senha.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Campos vazios", "Por favor, preencha o CPF e a senha.");
            return;
        }

        String validationError = CpfValidator.validate(cpf);
        if (validationError != null) {
            showAlert(Alert.AlertType.WARNING, "Erro de Validação", validationError);
            return;
        }

        try {
            // Tenta autenticar. Se não for gerente ou a senha estiver errada, vai lançar Exceção.
            service.authenticateManager(cpf, senha);

            // Se passou daqui, é porque é Gerente e a senha está correta!
            carregarTelaGerenciamento();

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Acesso Negado", e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Autenticação", e.getMessage());
        }
    }

    private void carregarTelaGerenciamento() {
        try {
            Stage stage = (Stage) cpfField.getScene().getWindow();
            // AQUI: Certifique-se de que o caminho aponta para a tela FXML correta que criamos anteriormente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TelaGerenciaFuncionarios.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro do Sistema", "Não foi possível carregar a tela de gerenciamento.");
        }
    }

    @FXML
    private void voltarParaMenu() {
        try {
            Stage stage = (Stage) cpfField.getScene().getWindow();
            // Retorna para a tela de Menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
