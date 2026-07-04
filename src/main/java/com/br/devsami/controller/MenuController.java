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
            // Navega para a Tela de Avaliação que fizemos (Página 2)
            Parent root = FXMLLoader.load(getClass().getResource("/ChatAssistente.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirTelaFuncionarios(ActionEvent event) {
        System.out.println("Abrindo tela de gerenciar funcionários...");
        // Futura lógica de troca para a Tela de Funcionários (Página 5)
    }

    @FXML
    void abrirTelaFeedbacks(ActionEvent event) {
        try {
            // Navega para a Tela de Avaliação que fizemos (Página 2)
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
            // Navega para a Tela de Configurações de IA que fizemos (Página 6)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AiConfig.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}