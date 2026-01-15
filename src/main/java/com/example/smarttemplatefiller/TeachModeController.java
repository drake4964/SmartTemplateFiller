package com.example.smarttemplatefiller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeachModeController {
    @FXML
    private ComboBox<String> columnComboBox;
    @FXML
    private TextField cellField;
    @FXML
    private ComboBox<String> directionComboBox;
    @FXML
    private ComboBox<String> rowPatternComboBox;
    @FXML
    private TextField manualRowField;
    @FXML
    private TextField titleField;
    @FXML
    private ListView<String> mappingListView;
    @FXML
    private TableView<ObservableList<String>> excelPreviewTable;
    @FXML
    private Label previewStatusLabel;

    private TableView<List<String>> tableView;
    private final List<Map<String, Object>> colMappings = new ArrayList<>();

    // For drag-and-drop
    private static final DataFormat MAPPING_FORMAT = new DataFormat("application/x-mapping-index");

    public void setTableView(TableView<List<String>> tableView) {
        this.tableView = tableView;
        int colCount = tableView.getColumns().size();
        List<String> columns = new ArrayList<>();
        // Skip first column (Row index) - start from i=1
        for (int i = 1; i < colCount; i++) {
            columns.add("Col " + i);
        }
        columnComboBox.setItems(FXCollections.observableArrayList(columns));
    }

    @FXML
    public void initialize() {
        directionComboBox.setItems(FXCollections.observableArrayList("vertical", "horizontal"));
        directionComboBox.setValue("vertical");
        rowPatternComboBox.setItems(FXCollections.observableArrayList("Odd Rows", "Even Rows", "All Rows"));

        // Setup drag-and-drop for ListView
        setupDragAndDrop();
    }

    /**
     * Configure ListView for drag-and-drop reordering
     */
    private void setupDragAndDrop() {
        mappingListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };

            // Drag detected - start drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.put(MAPPING_FORMAT, cell.getIndex());
                    content.putString(cell.getItem());
                    db.setContent(content);
                    event.consume();
                }
            });

            // Drag over - accept if valid
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasContent(MAPPING_FORMAT)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Drag entered - visual feedback
            cell.setOnDragEntered(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasContent(MAPPING_FORMAT)) {
                    cell.setStyle("-fx-background-color: #e3f2fd;");
                }
            });

            // Drag exited - remove visual feedback
            cell.setOnDragExited(event -> {
                cell.setStyle("");
            });

            // Drop - perform reorder
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasContent(MAPPING_FORMAT)) {
                    int draggedIdx = (Integer) db.getContent(MAPPING_FORMAT);
                    int targetIdx = cell.isEmpty() ? colMappings.size() - 1 : cell.getIndex();

                    if (draggedIdx != targetIdx && draggedIdx >= 0 && draggedIdx < colMappings.size()) {
                        // Reorder the mappings
                        Map<String, Object> draggedItem = colMappings.remove(draggedIdx);
                        colMappings.add(targetIdx, draggedItem);
                        updateMappingListView();
                        updateExcelPreview();
                        mappingListView.getSelectionModel().select(targetIdx);
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // Drag done - cleanup
            cell.setOnDragDone(event -> {
                cell.setStyle("");
                event.consume();
            });

            return cell;
        });
    }

    @FXML
    private void handleAddMapping() {
        int selectedCol = columnComboBox.getSelectionModel().getSelectedIndex();
        String startCell = cellField.getText().trim().toUpperCase();
        String direction = directionComboBox.getValue();
        String rowPattern = rowPatternComboBox.getValue();
        String manualRows = manualRowField.getText().trim();
        String title = titleField.getText().trim();

        // Validation
        if (selectedCol < 0) {
            showValidationError("Please select a source column.");
            return;
        }
        if (startCell.isEmpty()) {
            showValidationError("Please enter a target Excel cell.");
            return;
        }
        if (direction == null) {
            showValidationError("Please select a direction.");
            return;
        }

        // Validate cell reference format (e.g., A1, B2, AA123)
        if (!startCell.matches("^[A-Z]+[0-9]+$")) {
            showValidationError("Invalid cell reference format.\nUse Excel format like A1, B2, or AA123.");
            return;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sourceColumn", selectedCol);
        map.put("startCell", startCell);
        map.put("direction", direction);
        if (!title.isEmpty()) {
            map.put("title", title);
        }

        if (!manualRows.isEmpty()) {
            try {
                List<Integer> rowIndexes = Arrays.stream(manualRows.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && s.matches("\\d+"))
                        .map(s -> Integer.parseInt(s) - 1)
                        .collect(Collectors.toList());
                if (rowIndexes.isEmpty()) {
                    showValidationError("Manual rows field contains no valid numbers.");
                    return;
                }
                map.put("rowIndexes", rowIndexes);
            } catch (NumberFormatException e) {
                showValidationError("Invalid number in manual rows field.");
                return;
            }
        } else if (rowPattern != null) {
            String type = rowPattern.toLowerCase().contains("odd") ? "odd"
                    : rowPattern.toLowerCase().contains("even") ? "even" : "all";
            // Start from index 0 to include all rows
            int start = 0;
            map.put("rowPattern", Map.of("type", type, "start", start));
        }

        colMappings.add(map);
        updateMappingListView();
        updateExcelPreview();
        clearInputFields();

        // Show success feedback
        showInfo("Mapping added: Col " + (selectedCol + 1) + " -> " + startCell);
    }

    @FXML
    private void handleDeleteSelected() {
        int selected = mappingListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0 && selected < colMappings.size()) {
            colMappings.remove(selected);
            updateMappingListView();
            updateExcelPreview();
        } else {
            showValidationError("Please select a mapping to delete.");
        }
    }

    @FXML
    private void handleMoveUp() {
        int selected = mappingListView.getSelectionModel().getSelectedIndex();
        if (selected > 0) {
            Collections.swap(colMappings, selected, selected - 1);
            updateMappingListView();
            mappingListView.getSelectionModel().select(selected - 1);
            updateExcelPreview();
        }
    }

    @FXML
    private void handleMoveDown() {
        int selected = mappingListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0 && selected < colMappings.size() - 1) {
            Collections.swap(colMappings, selected, selected + 1);
            updateMappingListView();
            mappingListView.getSelectionModel().select(selected + 1);
            updateExcelPreview();
        }
    }

    @FXML
    private void handleClearAll() {
        if (!colMappings.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Clear All Mappings");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to clear all " + colMappings.size() + " mappings?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                colMappings.clear();
                updateMappingListView();
                updateExcelPreview();
            }
        }
    }

    @FXML
    private void handleSaveMapping() {
        if (colMappings.isEmpty()) {
            showValidationError("No mappings to save.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mapping File");
        fileChooser.setInitialFileName("mapping.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, colMappings);
                showInfo("Mapping saved to:\n" + file.getName());
            } catch (IOException e) {
                showValidationError("Failed to save: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadMapping() {
        ObjectMapper mapper = new ObjectMapper();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Mapping File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                List<Map<String, Object>> mappings = mapper.readValue(
                        file,
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                colMappings.clear();
                colMappings.addAll(mappings);
                updateMappingListView();
                updateExcelPreview();
                showInfo("Loaded " + mappings.size() + " mappings from:\n" + file.getName());
            } catch (IOException e) {
                showValidationError("Failed to load: " + e.getMessage());
            }
        }
    }

    private void updateMappingListView() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < colMappings.size(); i++) {
            Map<String, Object> map = colMappings.get(i);
            int source = ((Number) map.get("sourceColumn")).intValue() + 1;
            String cell = (String) map.get("startCell");
            String dir = (String) map.get("direction");
            String title = map.getOrDefault("title", "").toString();

            StringBuilder sb = new StringBuilder();
            sb.append(i + 1).append(". ");
            sb.append("Col ").append(source).append(" -> ").append(cell);
            sb.append(" [").append(dir).append("]");

            if (map.containsKey("rowPattern")) {
                Map<String, Object> rp = (Map<String, Object>) map.get("rowPattern");
                sb.append(" (").append(rp.get("type")).append(" rows)");
            } else if (map.containsKey("rowIndexes")) {
                List<?> rows = (List<?>) map.get("rowIndexes");
                sb.append(" (").append(rows.size()).append(" specific rows)");
            }

            if (!title.isEmpty()) {
                sb.append(" - \"").append(title).append("\"");
            }

            items.add(sb.toString());
        }
        mappingListView.setItems(items);
    }

    /**
     * Build an Excel-like preview table showing how data will appear
     */
    private void updateExcelPreview() {
        excelPreviewTable.getColumns().clear();
        excelPreviewTable.getItems().clear();

        if (colMappings.isEmpty()) {
            previewStatusLabel.setText("Add mappings to see preview");
            return;
        }

        // Calculate the bounds of the Excel output
        int minRow = Integer.MAX_VALUE;
        int maxRow = 0;
        int minCol = Integer.MAX_VALUE;
        int maxCol = 0;

        // First pass: determine grid size needed
        List<MappingData> processedMappings = new ArrayList<>();

        for (Map<String, Object> map : colMappings) {
            int sourceCol = ((Number) map.get("sourceColumn")).intValue();
            String startCell = (String) map.get("startCell");
            String direction = (String) map.get("direction");
            String title = map.getOrDefault("title", "").toString();

            CellReference ref = new CellReference(startCell);
            int startRow = ref.getRow();
            int startCol = ref.getCol();

            // Get row indexes
            List<Integer> rowIndexes;
            if (map.containsKey("rowPattern")) {
                Map<String, Object> rp = (Map<String, Object>) map.get("rowPattern");
                int start = ((Number) rp.get("start")).intValue();
                String type = (String) rp.get("type");
                rowIndexes = TxtParser.generateIndexes(tableView.getItems().size(), type, start);
            } else if (map.containsKey("rowIndexes")) {
                rowIndexes = (List<Integer>) map.get("rowIndexes");
            } else {
                rowIndexes = new ArrayList<>();
            }

            // Limit preview rows
            int previewCount = Math.min(rowIndexes.size(), 8);

            // Get values
            List<String> values = new ArrayList<>();
            for (int i = 0; i < previewCount; i++) {
                int rowIndex = rowIndexes.get(i);
                if (rowIndex >= 0 && rowIndex < tableView.getItems().size()) {
                    List<String> row = tableView.getItems().get(rowIndex);
                    if (sourceCol < row.size()) {
                        values.add(row.get(sourceCol));
                    } else {
                        values.add("");
                    }
                }
            }

            // Calculate bounds based on direction
            if ("vertical".equals(direction)) {
                // Title row (if exists and row > 0)
                if (!title.isEmpty() && startRow > 0) {
                    minRow = Math.min(minRow, startRow - 1);
                }
                minRow = Math.min(minRow, startRow);
                maxRow = Math.max(maxRow, startRow + values.size() - 1);
                minCol = Math.min(minCol, startCol);
                maxCol = Math.max(maxCol, startCol);
            } else {
                // Horizontal
                minRow = Math.min(minRow, startRow);
                maxRow = Math.max(maxRow, startRow);
                minCol = Math.min(minCol, startCol);
                maxCol = Math.max(maxCol, startCol + values.size()); // +1 for title if needed
            }

            processedMappings.add(new MappingData(sourceCol, startRow, startCol, direction, title, values));
        }

        if (minRow == Integer.MAX_VALUE) {
            previewStatusLabel.setText("No data to preview");
            return;
        }

        // Build grid
        int numRows = maxRow - minRow + 1;
        int numCols = maxCol - minCol + 1;

        // Create 2D grid initialized with empty strings
        String[][] grid = new String[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            Arrays.fill(grid[i], "");
        }

        // Fill grid with mapping data
        for (MappingData md : processedMappings) {
            if ("vertical".equals(md.direction)) {
                // Write title if present
                if (!md.title.isEmpty() && md.startRow > 0) {
                    int gridRow = (md.startRow - 1) - minRow;
                    int gridCol = md.startCol - minCol;
                    if (gridRow >= 0 && gridRow < numRows && gridCol >= 0 && gridCol < numCols) {
                        grid[gridRow][gridCol] = "[" + md.title + "]";
                    }
                }
                // Write values vertically
                for (int i = 0; i < md.values.size(); i++) {
                    int gridRow = (md.startRow + i) - minRow;
                    int gridCol = md.startCol - minCol;
                    if (gridRow >= 0 && gridRow < numRows && gridCol >= 0 && gridCol < numCols) {
                        grid[gridRow][gridCol] = md.values.get(i);
                    }
                }
            } else {
                // Horizontal: title in first cell, values after
                int gridRow = md.startRow - minRow;
                int gridCol = md.startCol - minCol;
                if (!md.title.isEmpty() && gridRow >= 0 && gridRow < numRows && gridCol >= 0 && gridCol < numCols) {
                    grid[gridRow][gridCol] = "[" + md.title + "]";
                    gridCol++;
                }
                for (int i = 0; i < md.values.size(); i++) {
                    int col = gridCol + i;
                    if (gridRow >= 0 && gridRow < numRows && col >= 0 && col < numCols) {
                        grid[gridRow][col] = md.values.get(i);
                    }
                }
            }
        }

        // Create row number column
        TableColumn<ObservableList<String>, String> rowNumCol = new TableColumn<>("");
        rowNumCol.setCellValueFactory(data -> {
            int idx = excelPreviewTable.getItems().indexOf(data.getValue());
            return new SimpleStringProperty(String.valueOf(minRow + idx + 1));
        });
        rowNumCol.setPrefWidth(40);
        rowNumCol.setStyle("-fx-alignment: CENTER; -fx-background-color: #f0f0f0; -fx-font-weight: bold;");
        excelPreviewTable.getColumns().add(rowNumCol);

        // Create columns with Excel letters
        for (int c = 0; c < numCols; c++) {
            final int colIndex = c;
            String colLetter = CellReference.convertNumToColString(minCol + c);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(colLetter);
            col.setCellValueFactory(data -> {
                ObservableList<String> row = data.getValue();
                if (colIndex < row.size()) {
                    return new SimpleStringProperty(row.get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            col.setPrefWidth(80);
            col.setStyle("-fx-alignment: CENTER-LEFT;");
            excelPreviewTable.getColumns().add(col);
        }

        // Add rows to table
        for (int r = 0; r < numRows; r++) {
            ObservableList<String> row = FXCollections.observableArrayList(grid[r]);
            excelPreviewTable.getItems().add(row);
        }

        previewStatusLabel.setText("Showing preview (rows " + (minRow + 1) + "-" + (maxRow + 1) +
                ", columns " + CellReference.convertNumToColString(minCol) +
                "-" + CellReference.convertNumToColString(maxCol) + ")");
    }

    // Helper class for processed mapping data
    private static class MappingData {
        int sourceCol;
        int startRow;
        int startCol;
        String direction;
        String title;
        List<String> values;

        MappingData(int sourceCol, int startRow, int startCol, String direction, String title, List<String> values) {
            this.sourceCol = sourceCol;
            this.startRow = startRow;
            this.startCol = startCol;
            this.direction = direction;
            this.title = title;
            this.values = values;
        }
    }

    private void clearInputFields() {
        cellField.clear();
        manualRowField.clear();
        titleField.clear();
        rowPatternComboBox.getSelectionModel().clearSelection();
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
