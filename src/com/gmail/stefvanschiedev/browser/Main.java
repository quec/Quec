package com.gmail.stefvanschiedev.browser;

import com.gmail.stefvanschiedev.browser.installation.Installation;
import com.gmail.stefvanschiedev.browser.utils.CookieUtil;
import com.gmail.stefvanschiedev.browser.utils.KeyShortcutManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Main browser application
 */
public class Main extends Application {

    public static void main(String[] args) {
        for (File file : Values.INSTALLABLEFILES) {
            if (!file.exists()) {
                Installation.launch(Installation.class, args);
                return;
            }
        }

        launch(args);
    }

    private Stage stage;

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        KeyShortcutManager keyShortcut = KeyShortcutManager.getInstance();
        keyShortcut.intialize(tabPane);
        keyShortcut.add(Arrays.asList(KeyCode.CONTROL, KeyCode.T), this::addTab);

        CookieManager manager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        try {
            for (String line : Files.readAllLines(Values.COOKIESFILE.toPath())) {
                String[] values = line.split("\\|");
                String[] actualValues = values[1].split(":");

                if (actualValues.length < 2)
                    continue;

                for (String header : actualValues[1].split("~")) {
                    HttpCookie cookie = CookieUtil.fromString(header);

                    if (cookie == null || cookie.hasExpired())
                        continue;

                    manager.getCookieStore().add(new URI(values[0]), cookie);
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        CookieHandler.setDefault(manager);

        //prevent the x button from doing something, we'll handle it ourselves
        Platform.setImplicitExit(false);

        ObservableList<Tab> tabs = tabPane.getTabs();

        stage.setOnCloseRequest(event -> {
            if (tabs.size() > 2) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Closing window");
                alert.setHeaderText("");
                alert.setContentText("Are you sure you want to close this window? Doing so will close all open tabs");
                Optional<ButtonType> button = alert.showAndWait();

                if (!button.isPresent() || button.get() != ButtonType.OK) {
                    event.consume();
                    return;
                }
            }

            //save cookies
            CookieStore store = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            try {
                Path cookieFilePath = Values.COOKIESFILE.toPath();

                Files.write(cookieFilePath, ("").getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

                for (URI uri : store.getURIs()) {
                    Files.write(cookieFilePath, (uri + "|Cookie:").getBytes(), StandardOpenOption.APPEND);

                    for (HttpCookie cookie : store.get(uri)) {
                        if (cookie.hasExpired())
                            continue;

                        Files.write(cookieFilePath, (CookieUtil.toString(cookie) + "~").getBytes(), StandardOpenOption.APPEND);
                    }

                    Files.write(cookieFilePath, "\n".getBytes(), StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //save bookmarks
            Bookmark.save();

            Platform.exit();
        });

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        //load bookmarks
        Bookmark.load();

        Tab tab = new Tab("+");
        tab.setClosable(false);

        tabs.add(tab);
        addTab();
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() + 1 == tabs.size())
                addTab();
        });
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Quec");

        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setLocation(Main.class.getResource("/browser.fxml"));

            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTab() {
        BrowserTab browserTab = new BrowserTab();
        browserTab.setText("Loading...");
        browserTab.setURL("https://google.com/");

        ContextMenu tabContextMenu = new ContextMenu();

        MenuItem closeItem = new MenuItem("Close");
        closeItem.setOnAction(event -> {
            tabPane.getTabs().remove(browserTab);

            if (tabPane.getTabs().size() == 1)
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        MenuItem closeOthers = new MenuItem("Close others");
        closeOthers.setOnAction(event -> {
            int searchIndex = 0;

            while (tabPane.getTabs().size() > 2) {
                Tab tab = tabPane.getTabs().get(searchIndex);

                if (tab.equals(browserTab))
                    searchIndex++;
                else
                    tabPane.getTabs().remove(tab);
            }
        });
        MenuItem closeLeft = new MenuItem("Close all on left side");
        closeLeft.setOnAction(event -> {
            int pos = tabPane.getTabs().indexOf(browserTab);

            for (int i = 0; i < pos; i++)
                tabPane.getTabs().remove(i);
        });
        MenuItem closeRight = new MenuItem("Close all on right side");
        closeRight.setOnAction(event -> {
            int pos = tabPane.getTabs().indexOf(browserTab);

            for (int i = pos + 1; i < tabPane.getTabs().size(); i++)
                tabPane.getTabs().remove(i);
        });
        tabContextMenu.getItems().addAll(closeItem, closeOthers, closeLeft, closeRight);
        browserTab.setContextMenu(tabContextMenu);

        ObservableList<Tab> tabs = tabPane.getTabs();

        browserTab.setOnClosed(event -> {
            if (tabs.size() == 1)
                Platform.exit();
        });

        tabs.add(tabs.size() - 1, browserTab);
        tabPane.getSelectionModel().select(tabs.size() - 2);
    }
}