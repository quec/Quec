package com.gmail.stefvanschiedev.browser.installation;

import com.gmail.stefvanschiedev.browser.Values;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Installation application for the browser
 */
public class Installation extends Application {

    @FXML
    private TextField text;
    @FXML
    private ProgressBar progress;
    @FXML
    private TextArea outputArea;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setLocation(Installation.class.getResource("/installation.fxml"));

            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle("Installation");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startInstallation();
    }

    private void startInstallation() {
        Platform.runLater(() -> {
            text.setText("Creating folders");

            if (!Values.SRCFILE.exists()) {
                if (!Values.SRCFILE.mkdirs()) {
                    outputArea.setText(outputArea.getText() + "Fatal error: unable to create folder\n");
                    outputArea.setText(outputArea.getText() + "Installation terminated; removing created folders\n");
                    text.setText("Terminating installation");
                    undoInstallation();
                    Platform.exit();
                }

                outputArea.setText(outputArea.getText() + "Created directory\n");
            }

            progress.setProgress(0.2);
            text.setText("Creating files");

            if (!Values.COOKIESFILE.exists()) {
                try {
                    if (!Values.COOKIESFILE.createNewFile()) {
                        outputArea.setText(outputArea.getText() + "Fatal error: unable to create cookie file\n");
                        outputArea.setText(outputArea.getText() + "Installation terminated; removing created files and folders\n");
                        text.setText("Terminating installation");
                        undoInstallation();
                        Platform.exit();
                    }
                } catch (IOException e) {
                    outputArea.setText(outputArea.getText() + "Fatal error: unable to create cookie file\n");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    outputArea.setText(outputArea.getText() + sw.toString() + "\nInstallation terminated; removing created files and folders\n");
                    text.setText("Terminating installation");
                    undoInstallation();
                    Platform.exit();
                }

                outputArea.setText(outputArea.getText() + "Created cookie file\n");
            }

            progress.setProgress(0.4);

            if (!Values.BOOKMARKFILE.exists()) {
                try {
                    if (!Values.BOOKMARKFILE.createNewFile()) {
                        outputArea.setText(outputArea.getText() + "Fatal error: unable to create bookmark file\n");
                        outputArea.setText(outputArea.getText() + "Installation terminated; removing created files and folders\n");
                        text.setText("Terminating installation");
                        undoInstallation();
                        Platform.exit();
                    }
                } catch (IOException e) {
                    outputArea.setText(outputArea.getText() + "Fatal error: unable to create bookmark file\n");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    outputArea.setText(outputArea.getText() + sw.toString() + "\nInstallation terminated; removing created files and folders\n");
                    text.setText("Terminating installation");
                    undoInstallation();
                    Platform.exit();
                }

                outputArea.setText(outputArea.getText() + "Created bookmark file\n");
            }

            progress.setProgress(0.6);
            text.setText("Creating folders");

            if (!Values.USERDATAFILE.exists()) {
                if (!Values.USERDATAFILE.mkdirs()) {
                    outputArea.setText(outputArea.getText() + "Fatal error: unable to create userdata folder\n");
                    outputArea.setText(outputArea.getText() + "Installation terminated; removing created files and folders\n");
                    text.setText("Terminating installation");
                    undoInstallation();
                    Platform.exit();
                }

                outputArea.setText(outputArea.getText() + "Created userdata folder\n");
            }

            progress.setProgress(0.8);

            if (!Values.EXTENSIONSFILE.exists()) {
                if (!Values.EXTENSIONSFILE.mkdirs()) {
                    outputArea.setText(outputArea.getText() + "Fatal error: unable to create extensions folder\n");
                    outputArea.setText(outputArea.getText() + "Installation terminated; removing created files and folders\n");
                    text.setText("Terminating installation");
                    undoInstallation();
                    Platform.exit();
                }

                outputArea.setText(outputArea.getText() + "Created extensions folder\n");
            }

            progress.setProgress(1);

            text.setText("Installation finished, run application again to start");
        });
    }

    private void undoInstallation() {
        if (!Values.EXTENSIONSFILE.delete())
            outputArea.setText(outputArea.getText() + "Fatal error: unable to delete extensions folder\n");

        if (!Values.USERDATAFILE.delete())
            outputArea.setText(outputArea.getText() + "Fatal error: unable to delete userdata folder\n");

        if (!Values.BOOKMARKFILE.delete())
            outputArea.setText(outputArea.getText() + "Fatal error: unable to delete bookmark file\n");

        if (!Values.COOKIESFILE.delete())
            outputArea.setText(outputArea.getText() + "Fatal error: unable to delete cookie file\n");

        if (!Values.SRCFILE.delete())
            outputArea.setText(outputArea.getText() + "Fatal error: unable to delete folder\n");
    }
}