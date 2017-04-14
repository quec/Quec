package com.gmail.stefvanschiedev.browser;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a tab for the browser
 */
public class BrowserTab extends Tab {

    @FXML private HBox itemContainer;
    @FXML private WebView webView;
    @FXML private TextField urlField;
    @FXML private Button backButton;
    @FXML private Button forwardButton;
    @FXML private Button goButton;
    @FXML private ToggleButton bookmarkButton;
    @FXML private HBox bookmarks;
    @FXML private Label zoomLabel;

    private ImageView imageView;

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

        imageView = new ImageView();

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

            if (pageTitle == null || pageTitle.trim().isEmpty())
                pageTitle = location.get();

            setText(pageTitle);
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

                populateBookmarks();
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

        engine.setCreatePopupHandler(handler -> {
            Stage stage = new Stage(StageStyle.UTILITY);
            WebView view = new WebView();
            stage.setScene(new Scene(view));
            stage.show();

            return view.getEngine();
        });

        location.addListener((observable, oldValue, newValue) -> {
            //check bookmarks
            URL url;
            String loc = location.get();

            try {
                url = new URL(loc);

                if (Bookmark.getBookmark(url.getProtocol() + "://" + url.getHost()) != null)
                    bookmarkButton.setSelected(true);
                else
                    bookmarkButton.setSelected(false);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }

            urlField.setText(loc);

            if (url.getProtocol().equals("https"))
                urlField.setStyle("-fx-border-color: green;");
            else
                urlField.setStyle(null);

            //favicon
            imageView.setImage(getFavicon(location.get()));
        });

        Platform.runLater(() -> {
            AutoCompletionBinding<String> autoCompletion = TextFields.bindAutoCompletion(urlField, sort(BrowserMain.getInstance().getVisitedPages()).keySet());
            autoCompletion.setHideOnEscape(true);
            autoCompletion.setVisibleRowCount(10);
            autoCompletion.prefWidthProperty().bind(urlField.widthProperty());
            autoCompletion.setOnAutoCompleted(event -> goButton.fire());
            autoCompletion.setDelay(0L);
        });

        //populate bookmarks
        populateBookmarks();

        engine.setOnError(event -> engine.loadContent("<h1>Error</h1><br /><p>" + event.getMessage() + "</p>"));

        setGraphic(imageView);
    }

    void addIcon(Node node) {
        itemContainer.getChildren().add(itemContainer.getChildren().size() - 1, node);
    }

    public WebView getWebView() {
        return webView;
    }

    void setURL(String urlText) {
        if (!BrowserMain.getInstance().getVisitedPages().containsKey(urlText))
            BrowserMain.getInstance().getVisitedPages().put(urlText, 1);
        else
            BrowserMain.getInstance().getVisitedPages().put(urlText, BrowserMain.getInstance().getVisitedPages().get(urlText) + 1);

        webView.getEngine().load(urlText);
    }

    private void search(String text) {
        setURL("https://google.com/search?q=" + text);
    }

    private Image getFavicon(String url) {
        try {
            return new Image(String.format("http://www.google.com/s2/favicons?domain_url=%s", URLEncoder.encode(url, "UTF-8")), true);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void populateBookmarks() {
        bookmarks.getChildren().clear();

        for (Bookmark bookmark : Bookmark.getBookmarks()) {
            String url = bookmark.getBaseURL().toString();
            Button button = new Button(url);
            button.setGraphic(new ImageView(getFavicon(url)));
            button.setOnMouseClicked(event -> setURL(url));
            bookmarks.getChildren().add(button);
        }
    }

    private static LinkedHashMap<String, Integer> sort(Map<String, Integer> map) {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}