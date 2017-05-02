package com.gmail.stefvanschiedev.browser;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;

/**
 * Settings class
 */
public class Settings {

    private Accordion content;

    @FXML private ChoiceBox<String> appearanceTheme;

    public Settings() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setLocation(getClass().getResource("/settings.fxml"));

            content = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        appearanceTheme.setItems(FXCollections.observableArrayList(Application.STYLESHEET_CASPIAN, Application.STYLESHEET_MODENA));
        appearanceTheme.setValue(BrowserMain.getInstance().getSettings().getProperty("theme"));
        appearanceTheme.setOnAction(action -> {
            String selected = appearanceTheme.getSelectionModel().getSelectedItem();

            BrowserMain.getInstance().getSettings().setProperty("theme", selected);
            BrowserMain.setUserAgentStylesheet(selected);
        });
    }

    public Accordion getContent() {
        return content;
    }
}