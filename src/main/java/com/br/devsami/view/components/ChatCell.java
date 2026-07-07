package com.br.devsami.view.components;

import com.br.devsami.model.dto.ChatMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ChatCell extends ListCell<ChatMessage> {

    private final HBox container = new HBox();
    private final Label messageLabel = new Label();
    private final Region spacer = new Region();

    public ChatCell() {

        container.setMaxWidth(Double.MAX_VALUE);
        container.setPadding(new Insets(5, 10, 5, 10));

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);

        // linha para deixar o texto inteiro
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        HBox.setHgrow(spacer, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(ChatMessage item, boolean empty) {

        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        messageLabel.setText(item.text());

        if (item.isUser()) {

            messageLabel.getStyleClass().removeAll("ai-bubble");
            messageLabel.getStyleClass().add("user-bubble");

            container.getChildren().setAll(spacer, messageLabel);
            container.setAlignment(Pos.CENTER_RIGHT);

        } else {

            messageLabel.getStyleClass().removeAll("user-bubble");
            messageLabel.getStyleClass().add("ai-bubble");

            container.getChildren().setAll(messageLabel, spacer);
            container.setAlignment(Pos.CENTER_LEFT);
        }

        setGraphic(container);
    }
}