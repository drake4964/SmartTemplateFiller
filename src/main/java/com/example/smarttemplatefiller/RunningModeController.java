package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.model.MappingConfiguration;
import com.example.smarttemplatefiller.model.WatchFolder;
import com.example.smarttemplatefiller.model.FileSlot;
import com.example.smarttemplatefiller.model.WatchConfiguration;
import com.example.smarttemplatefiller.model.MatchingStrategy;
import com.example.smarttemplatefiller.service.FolderWatchService;
import com.example.smarttemplatefiller.ui.FileStatusIndicator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for Running Mode - automated multi-folder watching and conversion.
 */
public class RunningModeController implements Initializable {

    @FXML
    private TextField mappingFileField;
    @FXML
    private TextField outputFolderField;
    @FXML
    private VBox watchFoldersContainer;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea activityLogArea;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button addWatchFolderButton;

    private Stage stage;
    private MappingConfiguration mappingConfig;
    private List<WatchFolderRow> watchFolderRows = new ArrayList<>();
    private FolderWatchService folderWatchService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI state
        statusLabel.setText("Not watching");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
        stopButton.setDisable(true);

        logMessage("Ready to start. Load a mapping file and add watch folders.");
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        // Stop watcher on window close
        stage.setOnCloseRequest(event -> {
            if (folderWatchService != null && folderWatchService.isWatching()) {
                folderWatchService.stopWatching();
            }
        });
    }

    @FXML
    private void handleBrowseMapping(ActionEvent event) {
        File file = new FileChooserBuilder(stage)
                .withTitle("Select Mapping File")
                .withExtension("JSON Files", "*.json")
                .open();

        if (file != null) {
            mappingFileField.setText(file.getAbsolutePath());
            loadMappingFile(file);
        }
    }

    private void loadMappingFile(File file) {
        try {
            mappingConfig = objectMapper.readValue(file, MappingConfiguration.class);
            logMessage("✓ Loaded mapping configuration: " + file.getName());

            // Display detected file slots
            if (mappingConfig.getFileSlots() != null && !mappingConfig.getFileSlots().isEmpty()) {
                logMessage("  Detected " + mappingConfig.getFileSlots().size() + " file slots:");
                for (FileSlot slot : mappingConfig.getFileSlots()) {
                    String desc = slot.getDescription() != null ? slot.getDescription() : "File " + slot.getSlot();
                    logMessage("    - Slot " + slot.getSlot() + ": " + desc);
                }
                addWatchFolderButton.setDisable(false);
            } else {
                logMessage("  Warning: No file slots defined in mapping. Using single-file mode.");
                addWatchFolderButton.setDisable(true);
            }

        } catch (IOException e) {
            logMessage("✗ Failed to load mapping: " + e.getMessage());
            showAlert("Mapping Error", "Could not load mapping file:\\n" + e.getMessage());
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
    private void handleAddWatchFolder(ActionEvent event) {
        if (mappingConfig == null || mappingConfig.getFileSlots() == null) {
            showAlert("No Mapping", "Please load a mapping file first.");
            return;
        }

        // Check if we've reached the maximum number of watch folders
        if (watchFolderRows.size() >= mappingConfig.getFileSlots().size()) {
            showAlert("Maximum Folders",
                    "You've already added folders for all file slots (" + mappingConfig.getFileSlots().size() + ").");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Watch");

        File folder = chooser.showDialog(stage);
        if (folder != null) {
            // Find next available slot
            Set<Integer> usedSlots = new HashSet<>();
            for (WatchFolderRow row : watchFolderRows) {
                usedSlots.add(row.getSelectedSlot());
            }

            for (FileSlot slot : mappingConfig.getFileSlots()) {
                if (!usedSlots.contains(slot.getSlot())) {
                    addWatchFolderRow(folder.toPath(), slot);
                    logMessage("+ Added watch folder for Slot " + slot.getSlot() + ": " + folder.getAbsolutePath());
                    break;
                }
            }
        }
    }

    private void addWatchFolderRow(Path folderPath, FileSlot slot) {
        WatchFolderRow row = new WatchFolderRow(folderPath, slot, mappingConfig.getFileSlots());
        watchFolderRows.add(row);
        watchFoldersContainer.getChildren().add(row.getNode());
    }

    @FXML
    private void handleStart(ActionEvent event) {
        // Validate configuration
        if (!validateConfig()) {
            return;
        }

        // Initialize folder watch service
        try {
            // Get watch configuration from mapping or use defaults
            WatchConfiguration watchConfig = mappingConfig.getWatchConfig();
            if (watchConfig == null) {
                watchConfig = new WatchConfiguration();
                watchConfig.setStabilityCheckSeconds(2);
                watchConfig.setMatchingStrategy(MatchingStrategy.PREFIX);
            }

            folderWatchService = new FolderWatchService(watchConfig);

            // Add watch folders
            for (WatchFolderRow row : watchFolderRows) {
                folderWatchService.addWatchFolder(row.getSelectedSlot(), row.getFolderPath());
            }

            // Add listener for file status updates
            folderWatchService.addListener(new FolderWatchService.FolderWatchListener() {
                @Override
                public void onFileReady(int slot, Path filePath) {
                    updateFileStatus(slot, true);
                    logMessage("File ready in slot " + slot + ": " + filePath.getFileName());
                }

                @Override
                public void onAllFilesReady(String matchKey, Map<Integer, Path> files) {
                    logMessage("=== All files ready for processing (" + matchKey + ") ===");
                    processFiles(matchKey, files);
                }
            });

            folderWatchService.startWatching();

            // Update UI state
            startButton.setDisable(true);
            stopButton.setDisable(false);
            addWatchFolderButton.setDisable(true);
            statusLabel.setText("● Watching " + watchFolderRows.size() + " folders");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            setConfigFieldsDisabled(true);

            logMessage("=== Started watching ===");
            for (WatchFolderRow row : watchFolderRows) {
                logMessage("  Folder " + row.getSelectedSlot() + ": " + row.getFolderPath());
            }

        } catch (Exception e) {
            logMessage("✗ Failed to start watching: " + e.getMessage());
            showAlert("Start Error", "Could not start folder watching:\\n" + e.getMessage());
        }
    }

    private void processFiles(String matchKey, Map<Integer, Path> files) {
        // Create defensive copy to avoid concurrent modification issues
        final Map<Integer, Path> filesCopy = new HashMap<>(files);

        if (filesCopy.isEmpty()) {
            logMessage("Warning: Processing triggered but no files were provided.");
            return;
        }

        // Run processing in background to avoid blocking watcher thread
        new Thread(() -> {
            try {
                logMessage("Processing " + filesCopy.size() + " files...");

                // Parse all input files
                Map<Integer, List<List<String>>> parsedData = new HashMap<>();
                for (Map.Entry<Integer, Path> entry : filesCopy.entrySet()) {
                    try {
                        List<List<String>> data = com.example.smarttemplatefiller.TxtParser
                                .parseFile(entry.getValue().toFile());
                        parsedData.put(entry.getKey(), data);
                    } catch (Exception e) {
                        throw new IOException("Failed to parse file at slot " + entry.getKey() + ": " + e.getMessage(),
                                e);
                    }
                }

                // Determine output file path
                String outputDir = outputFolderField.getText();
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                // Use matchKey in filename (sanitized)
                String safeMatchKey = matchKey.replaceAll("[^a-zA-Z0-9._-]", "_");
                String fileName = String.format("Merge_%s_%s.xlsx", safeMatchKey, timestamp);
                Path outputPath = Paths.get(outputDir, fileName);

                // Perform merge
                com.example.smarttemplatefiller.service.MultiFileMergeService mergeService = new com.example.smarttemplatefiller.service.MultiFileMergeService();

                mergeService.merge(parsedData, mappingConfig, outputPath);

                logMessage("✓ Conversion successful!");
                logMessage("  Output: " + fileName);

                // Archiving logic (if configured) - omitted for now as allowed by scope

            } catch (Exception e) {
                e.printStackTrace();
                logMessage("✗ Processing failed: " + e.getMessage());
                Platform.runLater(() -> showAlert("Processing Error", "Failed to process files:\\n" + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleStop(ActionEvent event) {
        if (folderWatchService != null) {
            folderWatchService.stopWatching();
            folderWatchService = null;
        }

        // Update UI state
        startButton.setDisable(false);
        stopButton.setDisable(true);
        addWatchFolderButton.setDisable(false);
        statusLabel.setText("Stopped");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
        setConfigFieldsDisabled(false);

        // Reset file status indicators
        for (WatchFolderRow row : watchFolderRows) {
            row.setWaiting();
        }

        logMessage("=== Stopped watching ===");
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
        if (folderWatchService != null && folderWatchService.isWatching()) {
            folderWatchService.stopWatching();
        }
        stage.close();
    }

    private boolean validateConfig() {
        StringBuilder errors = new StringBuilder();

        if (mappingFileField.getText().isEmpty()) {
            errors.append("• Mapping file is required\\n");
        } else if (!new File(mappingFileField.getText()).exists()) {
            errors.append("• Mapping file does not exist\\n");
        }

        if (watchFolderRows.isEmpty()) {
            errors.append("• At least one watch folder is required\\n");
        }

        if (outputFolderField.getText().isEmpty()) {
            errors.append("• Output folder is required\\n");
        }

        if (errors.length() > 0) {
            showAlert("Configuration Error", errors.toString());
            return false;
        }

        return true;
    }

    private void setConfigFieldsDisabled(boolean disabled) {
        mappingFileField.setDisable(disabled);
        outputFolderField.setDisable(disabled);

        for (WatchFolderRow row : watchFolderRows) {
            row.setDisabled(disabled);
        }
    }

    private void updateFileStatus(int slot, boolean fileReady) {
        Platform.runLater(() -> {
            for (WatchFolderRow row : watchFolderRows) {
                if (row.getSelectedSlot() == slot) {
                    if (fileReady) {
                        row.setReady();
                    } else {
                        row.setWaiting();
                    }
                    break;
                }
            }
        });
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            activityLogArea.appendText(String.format("[%s] %s\\n", timestamp, message));
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

    /**
     * Inner class representing a watch folder row in the UI.
     */
    private class WatchFolderRow {
        private Path folderPath;
        private ComboBox<FileSlot> slotComboBox;
        private Label folderLabel;
        // private FileStatusIndicator statusIndicator; // Removed
        private Button removeButton;
        private HBox rowNode;

        public WatchFolderRow(Path folderPath, FileSlot initialSlot, List<FileSlot> availableSlots) {
            this.folderPath = folderPath;

            // Create UI components
            Label slotLabel = new Label("Slot:");
            slotLabel.setPrefWidth(40);

            slotComboBox = new ComboBox<>();
            slotComboBox.getItems().addAll(availableSlots);
            slotComboBox.setConverter(new javafx.util.StringConverter<FileSlot>() {
                @Override
                public String toString(FileSlot slot) {
                    if (slot == null)
                        return "";
                    String desc = slot.getDescription() != null ? slot.getDescription() : "File " + slot.getSlot();
                    return slot.getSlot() + ": " + desc;
                }

                @Override
                public FileSlot fromString(String string) {
                    return null; // Not needed
                }
            });
            slotComboBox.setValue(initialSlot);
            slotComboBox.setPrefWidth(150);

            folderLabel = new Label(folderPath.toString());
            folderLabel.setStyle("-fx-font-family: monospace;");
            HBox.setHgrow(folderLabel, Priority.ALWAYS);

            // statusIndicator = new FileStatusIndicator(initialSlot.getSlot()); // Removed
            // statusIndicator.setFolderPath(folderPath.toString()); // Removed
            // statusIndicator.setWaiting(); // Removed

            removeButton = new Button("−");
            removeButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            removeButton.setOnAction(e -> removeRow());

            rowNode = new HBox(10);
            rowNode.getChildren().addAll(slotLabel, slotComboBox, folderLabel, removeButton); // statusIndicator.getNode()
                                                                                              // removed
            rowNode.setStyle(
                    "-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
            rowNode.setPadding(new Insets(5));
        }

        public HBox getNode() {
            return rowNode;
        }

        public Path getFolderPath() {
            return folderPath;
        }

        public int getSelectedSlot() {
            FileSlot selected = slotComboBox.getValue();
            return selected != null ? selected.getSlot() : 1;
        }

        public void setWaiting() {
            // Status indicators removed from row for simplicity
            // Can be re-added with separate FileStatusIndicator panel
        }

        public void setReady() {
            // Mark visually that file is ready
            folderLabel.setStyle("-fx-font-family: monospace; -fx-text-fill: #2f9e44; -fx-font-weight: bold;");
        }

        public void setDisabled(boolean disabled) {
            slotComboBox.setDisable(disabled);
            removeButton.setDisable(disabled);
        }

        private void removeRow() {
            watchFolderRows.remove(this);
            watchFoldersContainer.getChildren().remove(rowNode);
            logMessage("− Removed watch folder: " + folderPath);
        }
    }
}
