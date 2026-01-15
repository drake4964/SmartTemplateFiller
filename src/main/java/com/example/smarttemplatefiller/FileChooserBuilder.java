package com.example.smarttemplatefiller;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Fluent builder for FileChooser dialogs with last directory memory.
 * ENH-003: Automatically remembers the last used directory across all dialogs.
 */
public class FileChooserBuilder {
    private final Stage stage;
    private final FileChooser chooser;

    // ENH-003: Static field to remember last directory across all instances
    private static File lastDirectory = null;

    public FileChooserBuilder(Stage stage) {
        this.stage = stage;
        this.chooser = new FileChooser();
        // Set initial directory to last used, if available
        if (lastDirectory != null && lastDirectory.exists()) {
            chooser.setInitialDirectory(lastDirectory);
        }
    }

    public FileChooserBuilder withTitle(String title) {
        chooser.setTitle(title);
        return this;
    }

    public FileChooserBuilder withExtension(String label, String... extensions) {
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(label, extensions));
        return this;
    }

    public FileChooserBuilder withInitialDirectory(File directory) {
        if (directory != null && directory.exists()) {
            chooser.setInitialDirectory(directory);
        }
        return this;
    }

    public File open() {
        File file = chooser.showOpenDialog(stage);
        updateLastDirectory(file);
        return file;
    }

    public File save(String defaultFileName) {
        chooser.setInitialFileName(defaultFileName);
        File file = chooser.showSaveDialog(stage);
        updateLastDirectory(file);
        return file;
    }

    private void updateLastDirectory(File file) {
        if (file != null) {
            lastDirectory = file.getParentFile();
        }
    }

    /**
     * Reset the last directory memory (useful for testing).
     */
    public static void resetLastDirectory() {
        lastDirectory = null;
    }
}
