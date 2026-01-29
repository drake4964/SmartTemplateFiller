package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.license.DefaultLicenseValidator;
import com.example.smarttemplatefiller.license.LicenseErrorDialog;
import com.example.smarttemplatefiller.license.LicenseValidator;
import com.example.smarttemplatefiller.license.ValidationResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
            LicenseErrorDialog.show(result.getErrorMessage());
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

    public static void main(String[] args) {
        launch(args);
    }
}