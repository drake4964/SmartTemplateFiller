package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.mapping.RowPatternDescriptor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.poi.ss.util.CellReference;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class FlexPatternPanel extends VBox {

    private final TextField startField = new TextField("1");
    private final TextField fillField = new TextField("1");
    private final TextField spaceField = new TextField("0");

    private final VBox previewBox = new VBox(2);
    private Runnable validityChangeListener;

    private Timer debounceTimer = new Timer(true);
    private List<List<String>> currentSourceData;
    private int selectedColumnIndex = -1;
    private String targetCell = "";
    private String direction = "vertical";

    public FlexPatternPanel() {
        setSpacing(10);

        startField.setPrefWidth(50);
        fillField.setPrefWidth(50);
        spaceField.setPrefWidth(50);

        HBox fieldsBox = new HBox(10, 
            new Label("Start:"), startField, 
            new Label("Fill:"), fillField, 
            new Label("Space:"), spaceField);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);

        previewBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 5; -fx-border-color: #ddd;");
        previewBox.setMinHeight(80);

        getChildren().addAll(fieldsBox, new Label("Live Preview (10 output cells):"), previewBox);

        // Add listeners
        startField.textProperty().addListener((obs, o, n) -> triggerPreviewUpdate());
        fillField.textProperty().addListener((obs, o, n) -> triggerPreviewUpdate());
        spaceField.textProperty().addListener((obs, o, n) -> triggerPreviewUpdate());

        validate();
    }

    public void setValidityChangeListener(Runnable listener) {
        this.validityChangeListener = listener;
    }

    public void updateContext(String targetCell, String direction, int selectedColumnIndex, List<List<String>> sourceData) {
        this.targetCell = targetCell == null ? "" : targetCell;
        this.direction = direction == null ? "vertical" : direction;
        this.selectedColumnIndex = selectedColumnIndex;
        this.currentSourceData = sourceData;
        triggerPreviewUpdate();
    }

    private void triggerPreviewUpdate() {
        validate();
        debounceTimer.cancel();
        debounceTimer = new Timer(true);
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(FlexPatternPanel.this::renderPreview);
            }
        }, 150); // < 200ms debounce
    }

    private void validate() {
        boolean valid = true;
        valid &= validateField(startField, 1, "Start must be at least 1");
        valid &= validateField(fillField, 1, "Fill must be at least 1");
        valid &= validateField(spaceField, 0, "Space must be at least 0");

        if (validityChangeListener != null) {
            validityChangeListener.run();
        }
    }

    private boolean validateField(TextField field, int minVal, String errorMsg) {
        try {
            int val = Integer.parseInt(field.getText().trim());
            if (val < minVal) {
                setError(field, errorMsg);
                return false;
            }
            clearError(field);
            return true;
        } catch (NumberFormatException e) {
            setError(field, "Must be a valid integer");
            return false;
        }
    }

    private void setError(TextField field, String msg) {
        field.setStyle("-fx-border-color: red;");
        Tooltip t = new Tooltip(msg);
        field.setTooltip(t);
    }

    private void clearError(TextField field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    public boolean isValid() {
        try {
            int s = Integer.parseInt(startField.getText().trim());
            int f = Integer.parseInt(fillField.getText().trim());
            int sp = Integer.parseInt(spaceField.getText().trim());
            return s >= 1 && f >= 1 && sp >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public int getStart() { return Integer.parseInt(startField.getText().trim()); }
    public int getFill() { return Integer.parseInt(fillField.getText().trim()); }
    public int getSpace() { return Integer.parseInt(spaceField.getText().trim()); }

    public void setValues(int start, int fill, int space) {
        startField.setText(String.valueOf(start));
        fillField.setText(String.valueOf(fill));
        spaceField.setText(String.valueOf(space));
    }

    private void renderPreview() {
        previewBox.getChildren().clear();
        if (!isValid() || targetCell.isEmpty() || !targetCell.matches("^[A-Z]+[0-9]+$")) {
            previewBox.getChildren().add(new Label("Invalid input or missing Excel Cell."));
            return;
        }

        try {
            RowPatternDescriptor desc = new RowPatternDescriptor(getStart(), getFill(), getSpace());
            CellReference ref = new CellReference(targetCell);
            int startRow = ref.getRow();
            int startCol = ref.getCol();
            
            // Generate up to 10 sequences
            // Since there's no fixed limit if there's no source data, we assume at least enough rows to show 10 output cells.
            long totalRows = (currentSourceData == null || currentSourceData.isEmpty()) ? 1000 : currentSourceData.size();

            List<Map.Entry<Integer, Integer>> sequence = desc.generateOutputSequence(totalRows)
                    .limit(10)
                    .collect(Collectors.toList());

            if (sequence.isEmpty()) {
                previewBox.getChildren().add(new Label("Start field exceeds available rows. Output will be empty."));
                return;
            }

            for (int i = 0; i < 10; i++) {
                // Find what source row corresponds to output cell i
                final int outIdx = i;
                int sourceRow = sequence.stream()
                        .filter(e -> e.getKey() == outIdx)
                        .map(Map.Entry::getValue)
                        .findFirst().orElse(-1);

                String cellAddr;
                if ("vertical".equals(direction)) {
                    cellAddr = CellReference.convertNumToColString(startCol) + (startRow + 1 + i);
                } else {
                    cellAddr = CellReference.convertNumToColString(startCol + i) + (startRow + 1);
                }

                String valStr = "—";
                if (sourceRow != -1) {
                    if (currentSourceData != null && !currentSourceData.isEmpty()) {
                        if (sourceRow < currentSourceData.size()) {
                            List<String> rowData = currentSourceData.get(sourceRow);
                            valStr = (selectedColumnIndex >= 0 && selectedColumnIndex < rowData.size()) 
                                    ? rowData.get(selectedColumnIndex) 
                                    : "";
                        }
                    } else {
                        valStr = "Row " + (sourceRow + 1);
                    }
                }

                previewBox.getChildren().add(new Label(cellAddr + " \u2192 " + valStr));
            }
        } catch (Exception e) {
            previewBox.getChildren().add(new Label("Error generating preview."));
        }
    }
}
