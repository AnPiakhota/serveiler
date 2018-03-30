package com.itsm.component.moderation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Source: https://docs.oracle.com/javase/8/javafx/fxml-tutorial/custom_control.htm
 */
public class ModerationEntity extends GridPane {

    @FXML private Label requestPrompt;

    public ModerationEntity() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "moderation-entity.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    public String getText() {
        return requestPrompt.getText();
    }

    public void setText(String value) {
        requestPrompt.setText(value);
    }

    @FXML
    protected void doSomething() {
        System.out.println("The button was clicked!");
    }

}