package com.example.smarttemplatefiller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {
    private Stage stage;

    @FXML
    private TableView<List<String>> tableView;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLoadTxt(ActionEvent event) {
        // ENH-001: Use FileChooserBuilder instead of raw FileChooser
        File selectedFile = new FileChooserBuilder(stage)
                .withTitle("Open Text File")
                .withExtension("Text Files", "*.txt", "*.asc")
                .open();

        if (selectedFile != null) {
            List<List<String>> parsedData = TxtParser.parseFile(selectedFile);

            tableView.getColumns().clear();

            if (!parsedData.isEmpty()) {
                // Add row number column (robust even for duplicate rows)
                TableColumn<List<String>, String> rowIndexCol = new TableColumn<>("Row");
                rowIndexCol.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() >= tableView.getItems().size()) {
                            setText(null);
                        } else {
                            setText("Row " + (getIndex() + 1));
                        }
                    }
                });
                rowIndexCol.setSortable(false);
                tableView.getColumns().add(rowIndexCol);

                // Add dynamic data columns
                int columnCount = parsedData.get(0).size();
                for (int i = 0; i < columnCount; i++) {
                    final int colIndex = i;
                    TableColumn<List<String>, String> column = new TableColumn<>("Col " + (i + 1));
                    column.setCellValueFactory(data -> {
                        List<String> row = data.getValue();
                        return new SimpleStringProperty(
                                colIndex < row.size() ? row.get(colIndex) : "");
                    });
                    tableView.getColumns().add(column);
                }

                // Load data into the table
                tableView.setItems(FXCollections.observableArrayList(parsedData));
            }
        }
    }

    @FXML
    private void handleTeachMode(ActionEvent event) {
        if (tableView.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText(null);
            alert.setContentText("Please load a text file before entering Teach Mode.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teach_mode.fxml"));
            Parent root = loader.load();

            TeachModeController teachController = loader.getController();
            teachController.setTableView(tableView);

            Stage teachStage = new Stage();
            teachStage.setTitle("Teach Mode");
            teachStage.setScene(new Scene(root));
            teachStage.initOwner(stage);
            teachStage.initModality(Modality.APPLICATION_MODAL);
            teachStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load Teach Mode UI: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportToExcel(ActionEvent event) {
        // ENH-001: Use FileChooserBuilder for cleaner code and last-directory memory
        File txtFile = new FileChooserBuilder(stage)
                .withTitle("Select TXT File")
                .withExtension("Text Files", "*.txt", "*.asc")
                .open();
        if (txtFile == null)
            return;

        File mappingFile = new FileChooserBuilder(stage)
                .withTitle("Select Mapping JSON")
                .withExtension("JSON Files", "*.json")
                .open();
        if (mappingFile == null)
            return;

        // T009: Show export mode choice dialog
        ExportConfiguration exportConfig = ExportConfiguration.load();
        boolean appendMode = showExportModeDialog(exportConfig.isAppendMode());

        // Update and save preference
        exportConfig.setAppendMode(appendMode);
        exportConfig.save();

        if (appendMode) {
            // Append to existing file
            File existingFile = new FileChooserBuilder(stage)
                    .withTitle("Select Existing Excel File to Append To")
                    .withExtension("Excel Files", "*.xlsx")
                    .open();
            if (existingFile == null)
                return;

            // T010: Call appendToMappedFile and show result
            AppendResult result = ExcelWriter.appendToMappedFile(txtFile, mappingFile, existingFile);

            if (result.isSuccess()) {
                StringBuilder message = new StringBuilder();
                message.append("Successfully appended to Excel file!\n\n");
                message.append("Rows added: ").append(result.getRowsAdded()).append("\n");
                message.append("Row offset: ").append(result.getRowOffset()).append("\n");
                message.append("Target: ").append(existingFile.getName());

                if (result.hasWarnings()) {
                    message.append("\n\nWarnings:\n");
                    for (String warning : result.getWarnings()) {
                        message.append("• ").append(warning).append("\n");
                    }
                }

                showInfo("Append Successful", message.toString());
            } else {
                // T011B: Check if file appears corrupted and offer fallback
                String errorMsg = result.getErrorMessage();
                if (isCorruptedFileError(errorMsg)) {
                    // Show corrupted file dialog with option to create new
                    boolean createNew = showCorruptedFileDialog(existingFile.getName());
                    if (createNew) {
                        // Fallback to create new file
                        File outputFile = new FileChooserBuilder(stage)
                                .withTitle("Save New Excel File")
                                .withExtension("Excel Files", "*.xlsx")
                                .save("output.xlsx");
                        if (outputFile != null) {
                            try {
                                ExcelWriter.writeAdvancedMappedFile(txtFile, mappingFile, outputFile);
                                showInfo("Success", "New Excel file saved:\n" + outputFile.getAbsolutePath());
                            } catch (Exception e) {
                                showAlert("Error", "Failed to create new file: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    // T011: Show error (includes locked file detection from AppendResult)
                    showAlert("Append Failed", errorMsg);
                }
            }
        } else {
            // Create new file (original behavior)
            File outputFile = new FileChooserBuilder(stage)
                    .withTitle("Save Excel File")
                    .withExtension("Excel Files", "*.xlsx")
                    .save("output.xlsx");
            if (outputFile == null)
                return;

            try {
                ExcelWriter.writeAdvancedMappedFile(txtFile, mappingFile, outputFile);
                showInfo("Success", "Excel file saved:\n" + outputFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export: " + e.getMessage());
            }
        }
    }

    /**
     * Show dialog to choose between creating new file or appending to existing.
     * 
     * @return true if user chose append mode, false for create new file
     */
    private boolean showExportModeDialog(boolean initialAppendMode) {
        // Create a custom dialog with radio buttons
        javafx.scene.control.Dialog<Boolean> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Export Mode");
        dialog.setHeaderText("Choose export mode:");

        // Create radio buttons
        javafx.scene.control.ToggleGroup toggleGroup = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.RadioButton createNewRadio = new javafx.scene.control.RadioButton("Create New File");
        createNewRadio.setToggleGroup(toggleGroup);
        createNewRadio.setSelected(!initialAppendMode); // Default based on preference

        javafx.scene.control.RadioButton appendRadio = new javafx.scene.control.RadioButton("Append to Existing File");
        appendRadio.setToggleGroup(toggleGroup);
        appendRadio.setSelected(initialAppendMode);

        // Layout
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(createNewRadio, appendRadio);
        content.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return appendRadio.isSelected();
            }
            return null;
        });

        // Show and get result
        java.util.Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    @FXML
    private void handleRunningMode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/running_mode.fxml"));
            Parent root = loader.load();

            RunningModeController controller = loader.getController();

            Stage runningModeStage = new Stage();
            runningModeStage.setTitle("Running Mode - Automated Conversion");
            runningModeStage.setScene(new Scene(root));
            runningModeStage.initOwner(stage);

            controller.setStage(runningModeStage);

            runningModeStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load Running Mode UI: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // BUG-008 FIX: Separate method for info/success messages
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * T011B: Check if error message indicates corrupted file.
     */
    private boolean isCorruptedFileError(String errorMessage) {
        if (errorMessage == null)
            return false;
        String lowerMsg = errorMessage.toLowerCase();
        return lowerMsg.contains("corrupt") ||
                lowerMsg.contains("invalid") ||
                lowerMsg.contains("malformed") ||
                lowerMsg.contains("not a valid") ||
                lowerMsg.contains("zip") || // POI often reports zip errors for corrupt xlsx
                lowerMsg.contains("truncated");
    }

    /**
     * T011B: Show dialog offering to create new file when existing is corrupted.
     * 
     * @return true if user wants to create new file, false to cancel
     */
    private boolean showCorruptedFileDialog(String fileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("File Corrupted");
        alert.setHeaderText("File appears corrupted");
        alert.setContentText("The file \"" + fileName + "\" appears to be corrupted or unreadable.\n\n" +
                "Would you like to create a new file instead?");

        alert.getButtonTypes().setAll(
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.YES;
    }

}