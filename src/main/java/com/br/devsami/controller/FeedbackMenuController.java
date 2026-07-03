package com.br.devsami.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class FeedbackMenuController {

    private String cpfCliente;

    public void setCpfCliente(String cpf) {
        this.cpfCliente = cpf;
    }

    @FXML
    void abrirFeedbackProdutos(ActionEvent event) {
    }

    @FXML
    void abrirFeedbackAtendimento(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AvaliacaoFuncionario.fxml"));
            Parent root = loader.load();

            AvaliacaoFuncionarioController controller = loader.getController();
            controller.setDadosCliente(this.cpfCliente, false);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void voltarMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/MenuPrincipal.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}