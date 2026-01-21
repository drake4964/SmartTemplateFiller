## TeachModeController Multi-File Implementation - Progress Update

### ✅ Completed So Far

**T024: File Slot Dropdown** ✅
- Added `fileSlotComboBox` FXML field
- Initialize with "File 1" through "File 10" in `initialize()`
- Default value set to "File 1"
- Get selected file slot in `handleAddMapping()` and store in mapping Map

**T031: FXML Layout** ✅  
- Added "Loaded Files" panel with file list view
- Added file slot dropdown UI
- Added file count label

### 🔄 Remaining Implementation

The following methods need to be added to TeachModeController.java:

#### 1. Multi-File Loading (T029-T030)

Add this method after the existing `handleLoadMapping()` method:

```java
/**
 * T029-T030: Load multiple files for multi-file merge (2-10 files)
 */
@FXML  
private void handleLoad MultipleFiles() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Load Input Files (2-10 files supported)");
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.asc"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );
    
    List<File> files = chooser.showOpenMultipleDialog(null);
    if (files != null && !files.isEmpty()) {
        // T030: Validate 2-10 files
        if (files.size() < 2) {
            showValidationError("Multi-file mode requires at least 2 files.\\nPlease select 2-10 files.");
            return;
        }
        if (files.size() > 10) {
            showValidationError("Maximum 10 files allowed.\\nYou selected " + files.size() + " files.\\n\\nPlease select 10 or fewer files.");
            return;
        }
        
        loadMultipleFilesIntoSlots(files);
    }
}

private void loadMultipleFilesIntoSlots(List<File> files) {
    loadedFiles.clear();
    loadedTables.clear();
    
    for (int i = 0; i < files.size(); i++) {
        int slot = i + 1;
        File file = files.get(i);
        
        try {
            TableView<List<String>> table = TxtParser.parseToTable(file);
            loadedFiles.put(slot, file);
            loadedTables.put(slot, table);
        } catch (IOException e) {
            showValidationError("Failed to load " + file.getName() + ":\\n" + e.getMessage());
            return;
        }
    }
    
    updateLoadedFilesList();
    updateFileSlotComboBoxAvailable();
    showInfo("Loaded " + files.size() + " files successfully!");
}

private void updateLoadedFilesList() {
    ObservableList<String> items = FXCollections.observableArrayList();
    List<Integer> sortedSlots = new ArrayList<>(loadedFiles.keySet());
    Collections.sort(sortedSlots);
    
    for (int slot : sortedSlots) {
        File file = loadedFiles.get(slot);
        items.add("File " + slot + ": " + file.getName());
    }
    
    loadedFilesListView.setItems(items);
    fileCountLabel.setText(String.valueOf(loadedFiles.size()));
}

private void updateFileSlotComboBoxAvailable() {
    if (loadedFiles.isEmpty()) {
        // Reset to all slots
        List<String> slots = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            slots.add("File " + i);
        }
        fileSlotComboBox.setItems(FXCollections.observableArrayList(slots));
        fileSlotComboBox.setValue("File 1");
    } else {
        // Only show loaded file slots
        List<String> available = new ArrayList<>();
        List<Integer> sortedSlots = new ArrayList<>(loadedFiles.keySet());
        Collections.sort(sortedSlots);
        
        for (int slot : sortedSlots) {
            available.add("File " + slot);
        }
        
        fileSlotComboBox.setItems(FXCollections.observableArrayList(available));
        if (!available.isEmpty()) {
            fileSlotComboBox.setValue(available.get(0));
        }
    }
}
```

#### 2. Update Mapping Display Format (T025-T026)

Replace the existing `updateMappingListView()` method:

```java
private void updateMappingListView() {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (int i = 0; i < colMappings.size(); i++) {
        Map<String, Object> map = colMappings.get(i);
        
        // Get file slot (default to 1 for backward compatibility)
        int fileSlot = map.containsKey("sourceFileSlot") 
            ? ((Number) map.get("sourceFileSlot")).intValue() : 1;
        
        int source = ((Number) map.get("sourceColumn")).intValue() + 1;
        String cell = (String) map.get("startCell");
        String dir = (String) map.get("direction");
        String title = map.getOrDefault("title", "").toString();

        StringBuilder sb = new StringBuilder();
        sb.append(i + 1).append(". ");
        
        // T025-T026: NEW FORMAT: "File1:ColA → A1"
        sb.append("File").append(fileSlot).append(":Col").append(source);
        sb.append(" → ").append(cell);
        sb.append(" [").append(dir).append("]");

        if (map.containsKey("rowPattern")) {
            Map<String, Object> rp = (Map<String, Object>) map.get("rowPattern");
            sb.append(" (").append(rp.get("type")).append(" rows)");
        } else if (map.containsKey("rowIndexes")) {
            List<?> rows = (List<?>) map.get("rowIndexes");
            sb.append(" (").append(rows.size()).append(" specific rows)");
        }

        if (!title.isEmpty()) {
            sb.append(" - \\\"").append(title).append("\\\"");
        }

        items.add(sb.toString());
    }
    mappingListView.setItems(items);
}
```

### Summary

**Completed:**
- T024: File slot dropdown ✅
- T031: FXML layout ✅

**Remaining to add:**
- T025-T026: Update `updateMappingListView()` method (code above)
- T027-T028: Save/load extended JSON (keep for next update)
- T029-T030: Add `handleLoadMultipleFiles()` and helper methods (code above)
- T032: Integration test (later)

**Next Step:** Add the methods shown above to TeachModeController.java, then implement T027-T028 for JSON save/load.
