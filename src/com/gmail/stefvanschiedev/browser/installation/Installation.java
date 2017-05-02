package com.gmail.stefvanschiedev.browser.installation;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
        Task task = new Task() {
            @Override
            public Void call() {
                text.setText("Installation started");
                outputArea.setText(outputArea.getText() + "Installation started\n");

                try {
                    copyFolder(new File(getClass().getResource("/.quec.").getPath()), new File(System.getProperty("user.home"), ".quec."));
                } catch (IOException e) {
                    e.printStackTrace();

                    text.setText("Installation interrupted");
                    outputArea.setText(outputArea.getText() + "Installation interrupted\n");
                    cancelInstallation(this);
                }

                cancel();

                System.out.println("Done");

                Notifications.create().title("Installation").text("The installation process has finished!").showInformation();
                return null;
            }
        };
        progress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    private void cancelInstallation(Task task) {
        outputArea.setText(outputArea.getText() + "Fatal error: unable to create files and folder\n");
        text.setText("Terminating installation");
        task.cancel();
        Notifications.create().title("Installation").text("An error occurred while installing the program").showError();
    }

    private void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                if (!destinationFolder.mkdir()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An unexpected error occurred");
                    alert.showAndWait();
                }
            }

            for (String file : sourceFolder.list())
                copyFolder(new File(sourceFolder, file), new File(destinationFolder, file));
        } else
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}