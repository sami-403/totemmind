package com.br.devsami.view.components;

import com.br.devsami.controller.ListaClientesController;
import com.br.devsami.model.entity.User;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class UserCell extends ListCell<User> {
    private HBox hbox = new HBox(10);
    private Label labelNome = new Label();
    private Label labelCPF = new Label();
    private Pane espacador = new Pane();
    private Button btnEditar = new Button("Editar");
    private Button btnRemover = new Button("Remover");

    public UserCell(ListaClientesController controller) {

        HBox.setHgrow(espacador, Priority.ALWAYS); // Empurra botões pra direita
        hbox.getChildren().addAll(labelNome, labelCPF, espacador, btnEditar, btnRemover);

        btnEditar.setOnAction(e -> {
            if (getItem() != null) controller.editarItem(e, getItem());
        });

        btnRemover.setOnAction(e -> {
            if (getItem() != null) controller.removerItem(getItem());
        });
    }

    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        if (empty || user == null) {
            setGraphic(null);
        } else {
            labelNome.setText(user.getName());
            labelCPF.setText(formatCPF(user.getCpf()));
            setGraphic(hbox);
        }
    }

    private String formatCPF(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return "CPF não informado";
        }

        String apenasNumeros = cpf.replaceAll("\\D", "");

        if (apenasNumeros.length() < 11) {
            apenasNumeros = String.format("%11s", apenasNumeros).replace(' ', '0');
        }

        if (apenasNumeros.length() != 11) {
            return cpf;
        }

        return String.format("%s.%s.%s-%s",
                apenasNumeros.substring(0, 3),
                apenasNumeros.substring(3, 6),
                apenasNumeros.substring(6, 9),
                apenasNumeros.substring(9, 11)
        );
    }
}
