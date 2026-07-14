package com.br.devsami.view.components;

import com.br.devsami.controller.ListaProdutosController;
import com.br.devsami.model.entity.Product;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class ProductCell extends ListCell<Product> {
    private HBox hbox = new HBox(10);
    private Label labelNome = new Label();
    private Label labelPreco = new Label();
    private Pane espacador = new Pane();
    private Button btnEditar = new Button("Editar");
    private Button btnRemover = new Button("Remover");

    public ProductCell(ListaProdutosController controller) {

        HBox.setHgrow(espacador, Priority.ALWAYS); // Empurra botões pra direita
        hbox.getChildren().addAll(labelNome, labelPreco, espacador, btnEditar, btnRemover);

        btnEditar.setOnAction(e -> {
            if (getItem() != null) controller.editarItem(e, getItem());
        });

        btnRemover.setOnAction(e -> {
            if (getItem() != null) controller.removerItem(getItem());
        });
    }

    @Override
    protected void updateItem(Product product, boolean empty) {
        super.updateItem(product, empty);

        if (empty || product == null) {
            setGraphic(null);
        } else {
            labelNome.setText(product.getName());
            labelPreco.setText("R$" + product.getPrice());
            setGraphic(hbox);
        }
    }
}
