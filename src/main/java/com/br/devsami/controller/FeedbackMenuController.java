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

// Import CPF validator at the top level
import com.br.devsami.util.CpfValidator;

public class FeedbackMenuController {

    private String cpfCliente;
    private boolean isCpfValid = true; // State to track validation success

    public void setCpfCliente(String cpf) {
        if (cpf != null && CpfValidator.isValidCpf(cpf)) {
            this.cpfCliente = cpf;
            this.isCpfValid = true;
        } else {
            // CPF is invalid or null, prevent setting it and set error state
            this.cpfCliente = null;
            this.isCpfValid = false;
        }
    }

    public boolean isCpfValid() {
        return this.isCpfValid;
    }

    @FXML
    void abrirFeedbackProdutos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AvaliacaoProduto.fxml"));
            Parent root = loader.load();

            AvaliacaoProdutoController controller = loader.getController();
            controller.setDadosCliente(this.cpfCliente, false);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirFeedbackAtendimento(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AvaliacaoFuncionario.fxml"));
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MenuPrincipal.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}