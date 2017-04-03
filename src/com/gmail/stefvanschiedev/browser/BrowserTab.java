package com.gmail.stefvanschiedev.browser;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Represents a tab for the browser
 */
class BrowserTab extends Tab {

    @FXML
    private WebView webView;
    @FXML
    private TextField urlField;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button goButton;
    @FXML
    private ToggleButton bookmarkButton;
    @FXML
    private Label zoomLabel;

    BrowserTab() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setLocation(getClass().getResource("/tab-template.fxml"));

            setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize();
    }

    @FXML
    private void initialize() {
        WebEngine engine = webView.getEngine();
        ReadOnlyObjectProperty<Document> document = engine.documentProperty();
        ReadOnlyStringProperty location = engine.locationProperty();

        engine.setUserAgent("Quec/1.0");
        engine.setUserDataDirectory(Values.USERDATAFILE);
        document.addListener(change -> {
            //add listeners for links
            Platform.runLater(() -> {
                EventListener listener = ev -> {
                    String attribute = ((Element) ev.getTarget()).getAttribute("href");

                    //e.g. name anchor links
                    if (attribute == null)
                        return;

                    if (attribute.startsWith("http://") || attribute.startsWith("https://") || attribute.startsWith("#") || attribute.endsWith(".html"))
                        return;
                    else if (attribute.contains("#") && attribute.split("#")[0].endsWith(".html"))
                        return;

                    //downloadable file
                    //get current url
                    try {
                        URL url = new URL(location.get() + attribute);
                        File destination = new File(System.getProperty("user.home"), "Downloads\\" + new File(url.getFile()).getName());

                        if (!destination.createNewFile()) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("");
                            alert.setContentText("An error occurred while downloading the file");
                            alert.showAndWait();
                            return;
                        }

                        Files.copy(url.openStream(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                Document doc = document.getValue();

                if (doc == null)
                    return;

                NodeList nodes = doc.getElementsByTagName("a");

                for (int i = 0; i < nodes.getLength(); i++)
                    ((EventTarget) nodes.item(i)).addEventListener("click", listener, false);
            });
        });

        ReadOnlyStringProperty title = engine.titleProperty();

        title.addListener(change -> {
            String pageTitle = title.getValue();
            String locationText = location.get();

            if (pageTitle == null || pageTitle.trim().isEmpty())
                pageTitle = locationText;

            setText(pageTitle);
            urlField.setText(locationText);

        });

        WebHistory history = engine.getHistory();
        ReadOnlyIntegerProperty historyIndex = history.currentIndexProperty();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        backButton.setOnAction(event -> {
            history.go(-1);

            int index = historyIndex.get();

            if (index + 1 > entries.size() - 1)
                forwardButton.setDisable(true);
            else
                forwardButton.setDisable(false);

            if (index - 1 < 0)
                backButton.setDisable(true);
            else
                backButton.setDisable(false);
        });

        forwardButton.setOnAction(event -> {
            history.go(1);

            int index = historyIndex.get();

            if (index + 1 > entries.size() - 1)
                forwardButton.setDisable(true);
            else
                forwardButton.setDisable(false);

            if (index - 1 < 0)
                backButton.setDisable(true);
            else
                backButton.setDisable(false);
        });

        urlField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                goButton.fire();
        });

        goButton.setOnAction(event -> {
            String url = urlField.getText();

            if (!url.contains(".")) {
                search(url);
                return;
            }

            if (!(url.startsWith("http://") || url.startsWith("https://")))
                url = "http://" + url;

            setURL(url);
        });

        bookmarkButton.setOnAction(event -> {
            try {
                URL url = new URL(location.get());

                if (bookmarkButton.isSelected())
                    //save to memory and later remove it
                    Bookmark.addBookmark(url.getProtocol() + "://" + url.getHost());
                else
                    //remove from memory and later remove it
                    Bookmark.removeBookmark(url.getProtocol() + "://" + url.getHost());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        entries.addListener((ListChangeListener.Change<? extends WebHistory.Entry> c) -> Platform.runLater(() -> {
            int index = historyIndex.get();

            if (index + 1 > c.getList().size() - 1)
                forwardButton.setDisable(true);
            else
                forwardButton.setDisable(false);

            if (index - 1 < 0)
                backButton.setDisable(true);
            else
                backButton.setDisable(false);
        }));

        webView.setOnKeyPressed(event -> ButtonManager.put(webView, event.getCode()));
        webView.setOnKeyReleased(event -> ButtonManager.remove(webView, event.getCode()));

        webView.setOnScroll(event -> {
            if (ButtonManager.contains(webView, KeyCode.CONTROL)) {
                DoubleProperty zoom = webView.zoomProperty();

                int index = Arrays.asList(Values.ZOOM_LIST).indexOf(zoom.get());

                if (event.getDeltaY() < 0)
                    webView.setZoom(Values.ZOOM_LIST[Math.max(index - 1, 0)]);
                else
                    webView.setZoom(Values.ZOOM_LIST[Math.min(index + 1, Values.ZOOM_LIST.length - 1)]);

                zoomLabel.setText((int) (zoom.get() * 100) + "%");
            }
        });

        engine.setOnAlert(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Webpage alert");
            alert.setHeaderText("");
            alert.setContentText(event.getData());
            alert.showAndWait();
        });

        location.addListener((observable, oldValue, newValue) -> {
            //check bookmarks

            try {
                URL url = new URL(location.get());

                if (Bookmark.getBookmark(url.getProtocol() + "://" + url.getHost()) != null)
                    bookmarkButton.setSelected(true);
                else
                    bookmarkButton.setSelected(false);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        engine.setOnError(event -> engine.loadContent("<h1>Error</h1><br /><p>" + event.getMessage() + "</p>"));
    }

    void setURL(String urlText) {
        webView.getEngine().load(urlText);
    }

    private void search(String text) {
        setURL("https://google.com/search?q=" + text);
    }
}