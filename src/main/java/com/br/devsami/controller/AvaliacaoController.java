package com.br.devsami.controller;

import com.br.devsami.model.repository.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AvaliacaoController {

    @FXML
    private TextField txtCpf;

    @FXML
    public void initialize() {

    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/MenuPrincipal.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void seguirParaAvaliacao(ActionEvent event) {
        String cpf = txtCpf.getText();
        if (cpf.isBlank()) {
            return;
        }

        UserRepository userRepository = new UserRepository();

        if (userRepository.existsByCpf(cpf)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedbackMenu.fxml"));
                Parent root = loader.load();

                // Passa o CPF para o próximo Controller
                FeedbackMenuController controller = loader.getController();
                controller.setCpfCliente(cpf);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Fluxo de Cadastro de Usuário (quando implementar, lembre de passar o CPF gerado adiante)
        }
    }

}