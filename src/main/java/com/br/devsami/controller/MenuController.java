package com.br.devsami.controller;

import com.br.devsami.model.service.EmployeeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuController {

    private final EmployeeService employeeService = new EmployeeService();

    @FXML
    public void initialize() {
    }

    @FXML
    void abrirAssistente(ActionEvent event) {
        System.out.println("Abrindo Assistente (Chat B.I)...");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ChatAssistente.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirTelaFuncionarios(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/TelaGerenciaFuncionarios.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirTelaFeedbacks(ActionEvent event) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/TelaAvaliacao.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirConfiguracoesIA(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AiConfig.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirListaProdutos(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListaProdutos.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirListaClientes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListaClientes.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}