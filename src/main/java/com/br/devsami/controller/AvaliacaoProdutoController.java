package com.br.devsami.controller;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.repository.UserRepository;
import com.br.devsami.model.service.FeedbackService;
import com.br.devsami.model.service.ProductService;
import com.br.devsami.model.enums.Feeling;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AvaliacaoProdutoController {

    @FXML private ComboBox<Product> cbProdutos;
    @FXML private ToggleButton btnFeliz;
    @FXML private ToggleButton btnNeutro;
    @FXML private ToggleButton btnIncomodado;
    @FXML private TextArea txtFeedback;

    private ToggleGroup grupoEmojis;
    private String cpfCliente;
    private boolean isNovoCliente;

    @FXML
    public void initialize() {
        grupoEmojis = new ToggleGroup();
        btnFeliz.setToggleGroup(grupoEmojis);
        btnNeutro.setToggleGroup(grupoEmojis);
        btnIncomodado.setToggleGroup(grupoEmojis);

        cbProdutos.setConverter(new javafx.util.StringConverter<Product>() {
            @Override
            public String toString(Product p) {
                return p == null ? "" : p.getName();
            }

            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        carregarProdutos();
    }

    public void setDadosCliente(String cpf, boolean isNovoCliente) {
        this.cpfCliente = cpf;
        this.isNovoCliente = isNovoCliente;
    }

    private void carregarProdutos() {
        CompletableFuture.supplyAsync(() -> {
            ProductService productService = new ProductService();
            return productService.listAllProducts();
        }).thenAccept(produtos -> Platform.runLater(() -> {
            cbProdutos.getItems().clear();
            cbProdutos.getItems().addAll(produtos);
        })).exceptionally(ex -> {
            System.err.println("❌ Erro ao carregar produtos para avaliação:");
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    void enviar(ActionEvent event) {
        Product produto = cbProdutos.getValue();
        ToggleButton emojiSelecionado = (ToggleButton) grupoEmojis.getSelectedToggle();
        String feedbackText = txtFeedback.getText();

        if (produto == null || emojiSelecionado == null) return;

        Feeling feeling = emojiSelecionado == btnFeliz ? Feeling.SATISFIED :
                emojiSelecionado == btnNeutro ? Feeling.NEUTRAL :
                Feeling.DISSATISFIED;

        CompletableFuture.runAsync(() -> {
            UserRepository userRepository = new UserRepository();
            Optional<User> optionalUser = userRepository.findByCpf(this.cpfCliente);

            if (optionalUser.isPresent()) {
                FeedbackService feedbackService = new FeedbackService();

                feedbackService.createProductFeedback(
                        optionalUser.get(),
                        produto,
                        feeling,
                        null, // Ajuste a categoria conforme a tela
                        feedbackText
                );

                Platform.runLater(() -> irParaTelaAvaliacao(event));
            } else {
                System.err.println("❌ Cliente não encontrado ao enviar feedback.");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Cliente não encontrado na base de dados. Retornando à tela inicial.");
                    alert.showAndWait();
                    irParaTelaAvaliacao(event);
                });
            }
        }).exceptionally(ex -> {
            System.err.println("❌ Erro ao processar feedback:");
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Ocorreu um erro ao salvar o feedback: " + ex.getMessage() + ". Retornando à tela inicial.");
                alert.showAndWait();
                irParaTelaAvaliacao(event);
            });
            return null;
        });
    }

    private void irParaTelaAvaliacao(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/TelaAvaliacao.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/FeedbackMenu.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
