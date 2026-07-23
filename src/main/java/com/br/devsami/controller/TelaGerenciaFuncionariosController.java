package com.br.devsami.controller;

import com.br.devsami.model.enums.EmployeeType;
import com.br.devsami.model.service.EmployeeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import com.br.devsami.util.CpfValidator;

public class TelaGerenciaFuncionariosController {

    @FXML private VBox formContainer;
    @FXML private Label formTitle;
    @FXML private TextField idField, nameField, cpfField;
    @FXML private ComboBox<EmployeeType> typeComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Button actionButton;

    private final EmployeeService service = new EmployeeService();
    private String currentMode = "";

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll(EmployeeType.values());

        // Listener para mostrar/esconder senha se for Gerente
        typeComboBox.setOnAction(e -> {
            boolean isGerente = typeComboBox.getValue() == EmployeeType.GERENTE;
            passwordField.setVisible(isGerente);
            passwordField.setManaged(isGerente);
        });
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MenuPrincipal.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600)); // Ajuste as dimensões se necessário
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void setModeAdd() { configureForm("ADD", "Adicionar Funcionário", false, true, true, true, "#1A9347", "Cadastrar"); }
    @FXML public void setModeEdit() { configureForm("EDIT", "Editar Funcionário", true, true, true, true, "#1A9347", "Salvar"); }
    @FXML public void setModeRemove() { configureForm("REMOVE", "Remover Funcionário", true, false, false, false, "#D32F2F", "Excluir (Inativar)"); }

    private void configureForm(String mode, String title, boolean showId, boolean showName, boolean showCpf, boolean showType, String color, String btnText) {
        this.currentMode = mode;
        formContainer.setVisible(true);
        formContainer.setManaged(true);
        formTitle.setText(title);

        limparCampos(); // Limpa dados antigos ao trocar de modo

        // Aplica visibilidade
        idField.setVisible(showId); idField.setManaged(showId);
        nameField.setVisible(showName); nameField.setManaged(showName);
        cpfField.setVisible(showCpf); cpfField.setManaged(showCpf);
        typeComboBox.setVisible(showType); typeComboBox.setManaged(showType);

        // Senha começa oculta
        passwordField.setVisible(false); passwordField.setManaged(false);

        actionButton.setText(btnText);
        actionButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    @FXML
    private void handleAction() {
        try {
            // Pega a senha caso o campo esteja visível (ou seja, caso seja Gerente)
            String senha = passwordField.isVisible() ? passwordField.getText() : null;

            switch (currentMode) {
                case "ADD" -> {
                    String cpf = cpfField.getText();
                    String validationError = CpfValidator.validate(cpf);
                    if (validationError != null) {
                        showAlert(Alert.AlertType.WARNING, "Erro de Validação", validationError);
                        return;
                    }
                    // ATENÇÃO: Verifique se o seu service aceita o 4º parâmetro (senha)
                    service.createEmployee(nameField.getText(), cpf, typeComboBox.getValue(), senha);
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Funcionário cadastrado!");
                }
                case "EDIT" -> {
                    long id = Long.parseLong(idField.getText());
                    // ATENÇÃO: Verifique se o seu service aceita a senha na edição também
                    service.updateEmployee(id, nameField.getText(), typeComboBox.getValue(), senha);
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Funcionário atualizado!");
                }
                case "REMOVE" -> {
                    long id = Long.parseLong(idField.getText());
                    service.deleteEmployee(id); // Soft Delete
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Funcionário inativado!");
                }
            }
            limparCampos(); // Limpa os campos depois do sucesso
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Erro de Validação", "O ID deve ser um número válido.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    private void limparCampos() {
        idField.clear();
        nameField.clear();
        cpfField.clear();
        passwordField.clear();
        typeComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}