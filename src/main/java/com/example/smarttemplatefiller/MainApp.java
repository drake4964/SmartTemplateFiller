package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.license.DefaultLicenseValidator;
import com.example.smarttemplatefiller.license.LicenseValidator;
import com.example.smarttemplatefiller.license.ValidationResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        // STEP 1: License validation - must pass before showing main window
        LicenseValidator validator = new DefaultLicenseValidator();
        ValidationResult result = validator.validate();

        if (!result.isValid()) {
            LOGGER.log(Level.WARNING, "License validation failed: {0}", result.getErrorCode());
            showLicenseErrorDialog(result.getErrorMessage());
            Platform.exit();
            return;
        }

        LOGGER.log(Level.INFO, "License validation passed, starting application");

        // STEP 2: Normal application startup
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        // Get the controller and pass the stage
        MainController controller = loader.getController();
        controller.setStage(primaryStage);

        // Set up the scene and stage
        primaryStage.setTitle("SmartTemplateFiller");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Shows a blocking error dialog for license validation failure.
     * The dialog must be closed before the application exits.
     *
     * @param errorMessage The error message to display
     */
    private void showLicenseErrorDialog(String errorMessage) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("License Validation Failed");
        dialogStage.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2b2b2b, #1a1a1a);");

        // Try to load logo from config
        ImageView logoView = new ImageView();
        logoView.setFitWidth(100);
        logoView.setFitHeight(100);
        logoView.setPreserveRatio(true);

        try {
            // Try to load logo from resources or file system
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                logoView.setImage(new Image(new FileInputStream(logoFile)));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not load logo: {0}", e.getMessage());
        }

        Label messageLabel = new Label(
                errorMessage != null ? errorMessage : "This application requires a valid license to run.");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        messageLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-text-alignment: center;");

        Button okButton = new Button("OK");
        okButton.setPrefWidth(100);
        okButton.setPrefHeight(30);
        okButton.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ffffff; " +
                "-fx-font-size: 13px; -fx-background-radius: 5px; -fx-cursor: hand;");
        okButton.setOnAction(e -> dialogStage.close());

        if (logoView.getImage() != null) {
            root.getChildren().addAll(logoView, messageLabel, okButton);
        } else {
            root.getChildren().addAll(messageLabel, okButton);
        }

        Scene scene = new Scene(root, 420, 250);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Blocking - waits for user to close
    }

    public static void main(String[] args) {
        launch(args);
    }
}