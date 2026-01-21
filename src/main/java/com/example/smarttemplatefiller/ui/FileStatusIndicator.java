package com.example.smarttemplatefiller.ui;

import com.example.smarttemplatefiller.model.WatchFolder;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Visual indicator component showing the status of a watched folder.
 * Displays a colored circle (red=waiting, green=ready) with folder info.
 */
public class FileStatusIndicator extends HBox {
    
    private final Circle statusCircle;
    private final Label slotLabel;
    private final Label pathLabel;
    private final Label fileLabel;
    
    private static final Color COLOR_WAITING = Color.web("#FF6B6B");  // Red
    private static final Color COLOR_READY = Color.web("#51CF66");    // Green
    private static final Color COLOR_INACTIVE = Color.web("#868E96"); // Gray

    public FileStatusIndicator(int slot) {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        this.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        
        // Status circle
        statusCircle = new Circle(8);
        statusCircle.setFill(COLOR_INACTIVE);
        statusCircle.setStroke(Color.web("#dee2e6"));
        statusCircle.setStrokeWidth(1);
        
        // Slot label
        slotLabel = new Label("Slot " + slot + ":");
        slotLabel.setStyle("-fx-font-weight: bold;");
        slotLabel.setMinWidth(60);
        
        // Path label
        pathLabel = new Label("(not configured)");
        pathLabel.setStyle("-fx-text-fill: #868e96;");
        pathLabel.setMinWidth(200);
        
        // Detected file label
        fileLabel = new Label("");
        fileLabel.setStyle("-fx-text-fill: #495057;");
        
        this.getChildren().addAll(statusCircle, slotLabel, pathLabel, fileLabel);
    }

    /**
     * Update the indicator to show a configured folder.
     */
    public void setWatchFolder(WatchFolder folder) {
        if (folder == null) {
            pathLabel.setText("(not configured)");
            pathLabel.setStyle("-fx-text-fill: #868e96;");
            statusCircle.setFill(COLOR_INACTIVE);
            fileLabel.setText("");
            return;
        }
        
        pathLabel.setText(folder.getFolderPath().toString());
        pathLabel.setStyle("-fx-text-fill: #495057;");
        
        if (folder.isActive()) {
            if (folder.hasFileReady()) {
                statusCircle.setFill(COLOR_READY);
                fileLabel.setText("✓ " + folder.getLastFileDetected().getFileName());
                fileLabel.setStyle("-fx-text-fill: #2f9e44;");
            } else {
                statusCircle.setFill(COLOR_WAITING);
                fileLabel.setText("Waiting...");
                fileLabel.setStyle("-fx-text-fill: #868e96;");
            }
        } else {
            statusCircle.setFill(COLOR_INACTIVE);
            fileLabel.setText("");
        }
    }

    /**
     * Set the status to ready with a detected file.
     */
    public void setReady(String fileName) {
        statusCircle.setFill(COLOR_READY);
        fileLabel.setText("✓ " + fileName);
        fileLabel.setStyle("-fx-text-fill: #2f9e44;");
    }

    /**
     * Set the status to waiting for a file.
     */
    public void setWaiting() {
        statusCircle.setFill(COLOR_WAITING);
        fileLabel.setText("Waiting...");
        fileLabel.setStyle("-fx-text-fill: #868e96;");
    }

    /**
     * Set the status to inactive (not watching).
     */
    public void setInactive() {
        statusCircle.setFill(COLOR_INACTIVE);
        fileLabel.setText("");
    }

    /**
     * Update the folder path display.
     */
    public void setFolderPath(String path) {
        pathLabel.setText(path);
        pathLabel.setStyle("-fx-text-fill: #495057;");
    }
}
