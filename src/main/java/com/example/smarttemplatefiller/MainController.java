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

}