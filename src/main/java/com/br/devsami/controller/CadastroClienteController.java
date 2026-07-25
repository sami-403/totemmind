package com.br.devsami.controller;

import com.br.devsami.model.entity.User;
import com.br.devsami.model.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class CadastroClienteController {

    @FXML private Label formLabel;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private DatePicker dpNascimento;

    private UserService userService;
    private boolean isEditMode = false;
    private boolean isFromAdminMode = false;
    private User selectedUser;

    public void initialize() {
        this.userService = new UserService();
    }

    public void setFromAdminMode(boolean fromAdminMode) {
        this.isFromAdminMode = fromAdminMode;
    }

    public void setCpf(String cpf) {
        if (cpf != null) {
            txtCpf.setText(cpf);
            txtCpf.setDisable(true);
        }
    }

    private static class UserFormData{
        String nome;
        String cpf;
        LocalDate nascimento;

        UserFormData(String nome, String cpf, LocalDate nascimento){
            this.nome = nome;
            this.cpf = cpf;
            this.nascimento = nascimento;
        }
    }

    public void setUser(User user){
        if (user != null){
            txtCpf.setText(user.getCpf());
            txtNome.setText(user.getName());
            dpNascimento.setValue(user.getBirthDate());

            isEditMode = true;
            selectedUser = user;
            formLabel.setText("Atualizando Dados do Cliente");
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

        UserFormData userFormData = new UserFormData(nome, cpf, nascimento);
        try {
            if(isEditMode){
                editUser(event, userFormData);
            }else{
                createUser(event, userFormData);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    private void createUser(ActionEvent event, UserFormData userData) throws IOException {
        userService.createUser(userData.nome, userData.cpf, userData.nascimento);
        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Cadastrado com sucesso.");

        String targetFxml = isFromAdminMode ? "/fxml/ListaClientes.fxml" : "/fxml/FeedbackMenu.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
        Parent root = loader.load();

        if (!isFromAdminMode) {
            FeedbackMenuController controller = loader.getController();
            controller.setCpfCliente(userData.cpf);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
    }

    private void editUser(ActionEvent event, UserFormData userData) throws IOException {
        userService.editUser(selectedUser.getId(), userData.nome, userData.cpf, userData.nascimento);
        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Cliente Atualizado com sucesso");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListaClientes.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            String targetFxml = isFromAdminMode ? "/fxml/ListaClientes.fxml" : "/fxml/TelaAvaliacao.fxml";
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(targetFxml)));
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