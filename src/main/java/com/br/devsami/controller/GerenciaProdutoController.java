package com.br.devsami.controller;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.service.ProductService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class GerenciaProdutoController implements Initializable {

    @FXML private VBox formContainer;
    @FXML private Label formTitle;
    @FXML private TextField barCodeField, nameField, priceField;
    @FXML private Button actionButton;

    private final ProductService service = new ProductService();
    private String currentMode;
    private Product managedProduct;

    private static class ProductFormData{
        String name;
        String barCode;
        double price;

        private ProductFormData(String name, String barCode, String price){
            this.name=name; this.barCode = barCode; this.price = Double.parseDouble(price);
        }
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentMode = "ADD";
        formTitle.setText("Cadastrando novo Produto");
        managedProduct = null;
    }

    public void setProduct(Product product){
        // Popula o formulário e define para modo de edição se receber um Produto

        this.managedProduct = product;
        if (product != null){
            this.currentMode = "EDIT";

            formTitle.setText("Atualizando Produto");
            barCodeField.setText(managedProduct.getBarCode() != null ? managedProduct.getBarCode() : "");
            nameField.setText(managedProduct.getName());
            priceField.setText(String.valueOf(managedProduct.getPrice()));
            actionButton.setText("Salvar Alterações");
        }
    }

    private ProductFormData getFormData(){
        return new ProductFormData(
                nameField.getText(),
                barCodeField.getText(),
                priceField.getText()
        );
    }

    @FXML
    private void handleAction(ActionEvent event) {
        try {
            ProductFormData newProduct = getFormData();
            switch (currentMode) {
                case "ADD" -> {
                    service.createProduct(newProduct.name, newProduct.barCode, newProduct.price);
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto cadastrado!");
                }
                case "EDIT" -> {
                    service.updateProduct(managedProduct.getId(), newProduct.name, newProduct.barCode, newProduct.price);
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto atualizado!");
                }
            }
            voltar(event);
        } catch (IllegalArgumentException e){
            showAlert(Alert.AlertType.WARNING, "Erro de Validação", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ListaProdutos.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600)); // Ajuste as dimensões se necessário
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        nameField.clear();
        barCodeField.clear();
        priceField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}