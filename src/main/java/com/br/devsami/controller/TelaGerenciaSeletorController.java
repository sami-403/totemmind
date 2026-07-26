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

public class TelaGerenciaSeletorController {

    @FXML
    private void voltar(ActionEvent event) {
        carregarTela(event, "/fxml/MenuPrincipal.fxml");
    }

    @FXML
    private void abrirGerenciaFuncionarios(ActionEvent event) {
        carregarTela(event, "/fxml/TelaGerenciaFuncionarios.fxml");
    }

    @FXML
    private void abrirGerenciaProdutos(ActionEvent event) {
        carregarTela(event, "/fxml/GerenciaProduto.fxml");
    }

    @FXML
    private void abrirGerenciaFeedback(ActionEvent event) {
        carregarTela(event, "/fxml/TelaGerenciaFeedback.fxml");
    }

    private void carregarTela(ActionEvent event, String caminhoFxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(caminhoFxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao tentar carregar a tela: " + caminhoFxml);
        }
    }
}
