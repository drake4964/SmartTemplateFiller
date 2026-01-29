package com.example.smarttemplatefiller.license;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * Modal dialog displayed when license validation fails.
 * Shows configurable error message and optional company logo.
 */
public class LicenseErrorDialog {

    private static final Logger LOGGER = Logger.getLogger(LicenseErrorDialog.class.getName());

    private static final String DEFAULT_ERROR_MESSAGE = "This application requires a valid license to run.";

    private static final int DIALOG_WIDTH = 420;
    private static final int DIALOG_HEIGHT = 250;
    private static final int LOGO_SIZE = 100;

    /**
     * Shows the license error dialog with the specified message.
     * This is a blocking call that waits for the user to close the dialog.
     *
     * @param errorMessage The error message to display
     */
    public static void show(String errorMessage) {
        show(errorMessage, null);
    }

    /**
     * Shows the license error dialog with message and optional logo.
     * This is a blocking call that waits for the user to close the dialog.
     *
     * @param errorMessage The error message to display
     * @param logoPath     Path to the logo file (can be null)
     */
    public static void show(String errorMessage, String logoPath) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("License Validation Failed");
        dialogStage.setResizable(false);

        VBox root = createDialogContent(errorMessage, logoPath, dialogStage);

        Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
        dialogStage.setScene(scene);
        dialogStage.showAndWait(); // Blocking - waits for user to close
    }

    /**
     * Shows the license error dialog using configuration from LicenseConfig.
     *
     * @param config The license configuration containing message and logo path
     */
    public static void show(LicenseConfig config) {
        String message = config.getErrorMessage() != null
                ? config.getErrorMessage()
                : DEFAULT_ERROR_MESSAGE;
        show(message, config.getLogoPath());
    }

    /**
     * Creates the dialog content layout.
     */
    private static VBox createDialogContent(String errorMessage, String logoPath, Stage dialogStage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2b2b2b, #1a1a1a);");

        // Try to load logo
        ImageView logoView = createLogoView(logoPath);

        // Error message label
        Label messageLabel = new Label(errorMessage != null ? errorMessage : DEFAULT_ERROR_MESSAGE);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(DIALOG_WIDTH - 80);
        messageLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-text-alignment: center;");

        // OK button
        Button okButton = new Button("OK");
        okButton.setPrefWidth(100);
        okButton.setPrefHeight(30);
        okButton.setStyle(
                "-fx-background-color: #3a3a3a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-cursor: hand;");
        okButton.setOnAction(e -> dialogStage.close());

        // Add components based on logo availability
        if (logoView != null && logoView.getImage() != null) {
            root.getChildren().addAll(logoView, messageLabel, okButton);
        } else {
            root.getChildren().addAll(messageLabel, okButton);
        }

        return root;
    }

    /**
     * Creates an ImageView for the logo, handling missing files gracefully.
     *
     * @param logoPath Path to the logo file
     * @return ImageView with logo, or null if logo cannot be loaded
     */
    private static ImageView createLogoView(String logoPath) {
        if (logoPath == null || logoPath.isEmpty()) {
            return null;
        }

        try {
            File logoFile = new File(logoPath);
            if (!logoFile.exists()) {
                LOGGER.log(Level.FINE, "Logo file not found: {0}", logoPath);
                return null;
            }

            ImageView logoView = new ImageView();
            logoView.setFitWidth(LOGO_SIZE);
            logoView.setFitHeight(LOGO_SIZE);
            logoView.setPreserveRatio(true);
            logoView.setImage(new Image(new FileInputStream(logoFile)));

            return logoView;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load logo: {0}", e.getMessage());
            return null;
        }
    }
}
