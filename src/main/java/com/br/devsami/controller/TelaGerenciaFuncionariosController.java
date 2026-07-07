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
            stage.setScene(new Scene(root, 800, 600));
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

        // Aplica visibilidade
        idField.setVisible(showId); idField.setManaged(showId);
        nameField.setVisible(showName); nameField.setManaged(showName);
        cpfField.setVisible(showCpf); cpfField.setManaged(showCpf);
        typeComboBox.setVisible(showType); typeComboBox.setManaged(showType);

        // Senha começa oculta
        passwordField.setVisible(false); passwordField.setManaged(false);

        actionButton.setText(btnText);
        actionButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px;");
    }

    @FXML
    private void handleAction() {
        try {
            switch (currentMode) {
                case "ADD" -> {
                    service.createEmployee(nameField.getText(), cpfField.getText(), typeComboBox.getValue());
                    showAlert("Sucesso", "Funcionário cadastrado!");
                }
                case "EDIT" -> {
                    long id = Long.parseLong(idField.getText());
                    service.updateEmployee(id, nameField.getText(), typeComboBox.getValue());
                    showAlert("Sucesso", "Funcionário atualizado!");
                }
                case "REMOVE" -> {
                    long id = Long.parseLong(idField.getText());
                    service.deleteEmployee(id); // Soft Delete
                    showAlert("Sucesso", "Funcionário inativado!");
                }
            }
        } catch (Exception e) {
            showAlert("Erro", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
