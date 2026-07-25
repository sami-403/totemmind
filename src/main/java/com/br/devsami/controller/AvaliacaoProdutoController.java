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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AvaliacaoProdutoController {

    @FXML private ComboBox<Product> cbProdutos;
    @FXML private Button btnEstrela1;
    @FXML private Button btnEstrela2;
    @FXML private Button btnEstrela3;
    @FXML private Button btnEstrela4;
    @FXML private Button btnEstrela5;

    @FXML private FontIcon iconEstrela1;
    @FXML private FontIcon iconEstrela2;
    @FXML private FontIcon iconEstrela3;
    @FXML private FontIcon iconEstrela4;
    @FXML private FontIcon iconEstrela5;

    @FXML private Label lblNotaDescricao;
    @FXML private TextArea txtFeedback;

    private static final String STYLE_ACTIVE = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10px 14px; -fx-background-radius: 8px; -fx-font-size: 15px; -fx-font-weight: bold;";
    private static final String STYLE_INACTIVE = "-fx-background-color: #3a3a3c; -fx-text-fill: #aaa; -fx-cursor: hand; -fx-padding: 10px 14px; -fx-background-radius: 8px; -fx-font-size: 15px; -fx-font-weight: bold;";

    private int ratingSelecionado = 0;
    private String cpfCliente;
    private boolean isNovoCliente;

    @FXML
    public void initialize() {
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
        configurarHoverEstrelas();
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

    private void configurarHoverEstrelas() {
        Button[] buttons = {btnEstrela1, btnEstrela2, btnEstrela3, btnEstrela4, btnEstrela5};
        for (int i = 0; i < buttons.length; i++) {
            final int starCount = i + 1;
            if (buttons[i] != null) {
                buttons[i].setOnMouseEntered(e -> renderEstrelas(starCount));
                buttons[i].setOnMouseExited(e -> renderEstrelas(this.ratingSelecionado));
            }
        }
    }

    private void renderEstrelas(int ratingTemp) {
        Button[] buttons = {btnEstrela1, btnEstrela2, btnEstrela3, btnEstrela4, btnEstrela5};
        FontIcon[] icons = {iconEstrela1, iconEstrela2, iconEstrela3, iconEstrela4, iconEstrela5};

        for (int i = 0; i < 5; i++) {
            if (buttons[i] == null) continue;
            if (i < ratingTemp) {
                buttons[i].setStyle(STYLE_ACTIVE);
                if (icons[i] != null) {
                    icons[i].setIconColor(Color.WHITE);
                }
            } else {
                buttons[i].setStyle(STYLE_INACTIVE);
                if (icons[i] != null) {
                    icons[i].setIconColor(Color.web("#666666"));
                }
            }
        }
    }

    private void atualizarEstrelas(int rating) {
        this.ratingSelecionado = rating;
        renderEstrelas(rating);

        if (lblNotaDescricao != null) {
            switch (rating) {
                case 1 -> lblNotaDescricao.setText("1/5 - Péssimo");
                case 2 -> lblNotaDescricao.setText("2/5 - Ruim");
                case 3 -> lblNotaDescricao.setText("3/5 - Regular");
                case 4 -> lblNotaDescricao.setText("4/5 - Bom");
                case 5 -> lblNotaDescricao.setText("5/5 - Excelente");
                default -> lblNotaDescricao.setText("Selecione de 1 a 5 estrelas");
            }
        }
    }

    @FXML void selecionarEstrela1() { atualizarEstrelas(1); }
    @FXML void selecionarEstrela2() { atualizarEstrelas(2); }
    @FXML void selecionarEstrela3() { atualizarEstrelas(3); }
    @FXML void selecionarEstrela4() { atualizarEstrelas(4); }
    @FXML void selecionarEstrela5() { atualizarEstrelas(5); }

    @FXML
    void enviar(ActionEvent event) {
        Product produto = cbProdutos.getValue();
        String feedbackText = txtFeedback.getText();

        if (produto == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione um produto.");
            alert.showAndWait();
            return;
        }

        if (ratingSelecionado <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione uma nota de 1 a 5 estrelas para o produto.");
            alert.showAndWait();
            return;
        }

        CompletableFuture.runAsync(() -> {
            UserRepository userRepository = new UserRepository();
            Optional<User> optionalUser = userRepository.findByCpf(this.cpfCliente);

            if (optionalUser.isPresent()) {
                FeedbackService feedbackService = new FeedbackService();

                feedbackService.createProductFeedback(
                        optionalUser.get(),
                        produto,
                        ratingSelecionado,
                        null, // Categoria (a ser inferida por IA futuramente)
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
