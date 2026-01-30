package com.example.smarttemplatefiller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for Running Mode - automated folder watching and conversion.
 */
public class RunningModeController implements Initializable {

    @FXML
    private TextField mappingFileField;
    @FXML
    private TextField watchFolderField;
    @FXML
    private TextField outputFolderField;
    @FXML
    private TextField filePatternField;
    @FXML
    private Spinner<Integer> intervalSpinner;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea activityLogArea;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private CheckBox appendModeCheckBox;

    private Stage stage;
    private RunningModeConfig config;
    private FolderWatcher watcher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize interval spinner (1-60 seconds)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 1);
        intervalSpinner.setValueFactory(valueFactory);

        // Load saved configuration
        config = RunningModeConfig.load();
        loadConfigToUI();
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        // Save config and stop watcher on window close
        stage.setOnCloseRequest(event -> {
            if (watcher != null && watcher.isRunning()) {
                watcher.stop();
            }
            saveConfig();
        });
    }

    private void loadConfigToUI() {
        if (config.getMappingFile() != null) {
            mappingFileField.setText(config.getMappingFile());
        }
        if (config.getWatchFolder() != null) {
            watchFolderField.setText(config.getWatchFolder());
        }
        if (config.getOutputFolder() != null) {
            outputFolderField.setText(config.getOutputFolder());
        }
        filePatternField.setText(config.getFilePattern());
        intervalSpinner.getValueFactory().setValue(config.getIntervalSeconds());
        appendModeCheckBox.setSelected(config.isAppendModeEnabled());
    }

    private void saveConfig() {
        config.setMappingFile(mappingFileField.getText());
        config.setWatchFolder(watchFolderField.getText());
        config.setOutputFolder(outputFolderField.getText());
        config.setFilePattern(filePatternField.getText());
        config.setIntervalSeconds(intervalSpinner.getValue());
        config.setAppendModeEnabled(appendModeCheckBox.isSelected());

        try {
            config.save();
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    @FXML
    private void handleBrowseMapping(ActionEvent event) {
        File file = new FileChooserBuilder(stage)
                .withTitle("Select Mapping File")
                .withExtension("JSON Files", "*.json")
                .open();

        if (file != null) {
            mappingFileField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseWatchFolder(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Watch Folder");

        // Start from current value if set
        String current = watchFolderField.getText();
        if (current != null && !current.isEmpty()) {
            File dir = new File(current);
            if (dir.exists() && dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
        }

        File folder = chooser.showDialog(stage);
        if (folder != null) {
            watchFolderField.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseOutputFolder(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Folder");

        // Start from current value if set
        String current = outputFolderField.getText();
        if (current != null && !current.isEmpty()) {
            File dir = new File(current);
            if (dir.exists() && dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
        }

        File folder = chooser.showDialog(stage);
        if (folder != null) {
            outputFolderField.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    private void handleStart(ActionEvent event) {
        // Validate configuration
        if (!validateConfig()) {
            return;
        }

        // Update config from UI
        saveConfig();

        // T021: Check if append mode is enabled and we have a previous file
        if (appendModeCheckBox.isSelected() && config.getLastGeneratedFilePath() != null) {
            File lastFile = new File(config.getLastGeneratedFilePath());
            if (lastFile.exists()) {
                // Show dialog asking user what to do
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Append Mode");
                alert.setHeaderText("Continue appending to existing file?");
                alert.setContentText("Found previous session file:\n" + lastFile.getName() +
                        "\n\nDo you want to continue appending to this file?");

                ButtonType yesButton = new ButtonType("Yes, Continue");
                ButtonType noButton = new ButtonType("No, Start Fresh");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == cancelButton) {
                        // User cancelled, abort start
                        return;
                    } else if (result.get() == noButton) {
                        // User wants to start fresh, clear the path
                        config.setLastGeneratedFilePath(null);
                    }
                    // If yesButton, keep the path as is
                } else {
                    // Dialog was closed, abort start
                    return;
                }
            } else {
                // File no longer exists, clear the path silently
                config.setLastGeneratedFilePath(null);
            }
        }

        // Start watcher
        watcher = new FolderWatcher(config, this::logMessage);
        watcher.start();

        // Update UI state
        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("Watching: " + config.getWatchFolder());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        setConfigFieldsDisabled(true);
    }

    @FXML
    private void handleStop(ActionEvent event) {
        if (watcher != null) {
            // Save the last generated file path for session persistence
            if (watcher.getLastGeneratedFilePath() != null) {
                config.setLastGeneratedFilePath(watcher.getLastGeneratedFilePath());
            }
            watcher.stop();
            watcher = null;
        }

        // Update UI state
        startButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("Stopped");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
        setConfigFieldsDisabled(false);
    }

    @FXML
    private void handleOpenOutputFolder(ActionEvent event) {
        String outputPath = outputFolderField.getText();
        if (outputPath == null || outputPath.isEmpty()) {
            showAlert("No Output Folder", "Please select an output folder first.");
            return;
        }

        File folder = new File(outputPath);
        if (!folder.exists()) {
            showAlert("Folder Not Found", "The output folder does not exist yet.");
            return;
        }

        try {
            Desktop.getDesktop().open(folder);
        } catch (IOException e) {
            showAlert("Error", "Could not open folder: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        if (watcher != null && watcher.isRunning()) {
            watcher.stop();
        }
        saveConfig();
        stage.close();
    }

    private boolean validateConfig() {
        StringBuilder errors = new StringBuilder();

        if (mappingFileField.getText().isEmpty()) {
            errors.append("• Mapping file is required\n");
        } else if (!new File(mappingFileField.getText()).exists()) {
            errors.append("• Mapping file does not exist\n");
        }

        if (watchFolderField.getText().isEmpty()) {
            errors.append("• Watch folder is required\n");
        } else if (!new File(watchFolderField.getText()).isDirectory()) {
            errors.append("• Watch folder is not a valid directory\n");
        }

        if (outputFolderField.getText().isEmpty()) {
            errors.append("• Output folder is required\n");
        }

        if (filePatternField.getText().isEmpty()) {
            errors.append("• File pattern is required\n");
        }

        if (errors.length() > 0) {
            showAlert("Configuration Error", errors.toString());
            return false;
        }

        return true;
    }

    private void setConfigFieldsDisabled(boolean disabled) {
        mappingFileField.setDisable(disabled);
        watchFolderField.setDisable(disabled);
        outputFolderField.setDisable(disabled);
        filePatternField.setDisable(disabled);
        intervalSpinner.setDisable(disabled);
        appendModeCheckBox.setDisable(disabled);
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            activityLogArea.appendText(message + "\n");
            // Auto-scroll to bottom
            activityLogArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
