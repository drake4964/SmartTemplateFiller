# TeachModeController Multi-File Implementation Summary

## Changes Made to teach_mode.fxml (T031)

✅ 1. **Added File Slot Dropdown**
   - New ComboBox: `fileSlotComboBox` for selecting source file (1-10)
   - Positioned before column selection
   - Default prompt: "File 1"

✅ 2. **Added Loaded Files Panel**
   - New Section 0: Shows loaded files list
   - ListView: `loadedFilesListView` displays file names with slot numbers
   - Label: `fileCountLabel` shows current count
   - Button: "Load Files (Multi-File Mode)" triggers file loading

## Required TeachModeController Changes (T024-T030)

### T024: Add File Slot Dropdown ✅
```java
@FXML private ComboBox<String> fileSlotComboBox;
@FXML private ListView<String> loadedFilesListView;
@FXML private Label fileCountLabel;

// In initialize():
List<String> slots = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    slots.add("File " + i);
}
fileSlotComboBox.setItems(FXCollections.observableArrayList(slots));
fileSlotComboBox.setValue("File 1"); // Default
```

### T029-T030: Multi-File Loading with Validation
```java
private final Map<Integer, File> loadedFiles = new HashMap<>();
private final Map<Integer, TableView<List<String>>> loadedTables = new HashMap<>();

@FXML
private void handleLoadMultipleFiles() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Load Input Files (2-10 files)");
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.asc"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );
    
    List<File> files = chooser.showOpenMultipleDialog(stage);
    if (files != null && !files.isEmpty()) {
        // Validate 2-10 files
        if (files.size() < 2) {
            showError("Please select at least 2 files for multi-file mode");
            return;
        }
        if (files.size() > 10) {
            showError("Maximum 10 files allowed. Please select fewer files.");
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
            showError("Failed to load " + file.getName() + ": " + e.getMessage());
        }
    }
    
    updateLoadedFilesList();
    updateFileSlotComboBox();
}

private void updateLoadedFilesList() {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (Map.Entry<Integer, File> entry : loadedFiles.entrySet()) {
        items.add("File " + entry.getKey() + ": " + entry.getValue().getName());
    }
    loadedFilesListView.setItems(items);
    fileCountLabel.setText(String.valueOf(loadedFiles.size()));
}

private void updateFileSlotComboBox() {
    List<String> available = new ArrayList<>();
    for (int i = 1; i <= loadedFiles.size(); i++) {
        available.add("File " + i);
    }
   fileSlotComboBox.setItems(FXCollections.observableArrayList(available));
    if (!available.isEmpty()) {
        fileSlotComboBox.setValue(available.get(0));
    }
}
```

### T025-T026: Update Mapping Display Format
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
        
        // NEW FORMAT: "File1:ColA → A1"
        sb.append("File").append(fileSlot).append(":Col").append(source);
        sb.append(" → ").append(cell);
        sb.append(" [").append(dir).append("]");

        if (map.containsKey("rowPattern")) {
            Map<String, Object> rp = (Map<String, Object>) map.get("rowPattern");
            sb.append(" (").append(rp.get("type")).append(" rows)");
        }

        if (!title.isEmpty()) {
            sb.append(" - \"").append(title).append("\"");
        }

        items.add(sb.toString());
    }
    mappingListView.setItems(items);
}
```

### T024: Add Mapping with File Slot
```java
@FXML
private void handleAddMapping() {
    // ... existing validation ...
    
    // Get selected file slot
    String selectedSlot = fileSlotComboBox.getValue();
    int fileSlot = selectedSlot != null 
        ? Integer.parseInt(selectedSlot.replace("File ", "")) 
        : 1;
    
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("sourceFileSlot", fileSlot); // NEW!
    map.put("sourceColumn", selectedCol);
    map.put("startCell", startCell);
    map.put("direction", direction);
    // ... rest of existing code ...
    
    colMappings.add(map);
    updateMappingListView();
    updateExcelPreview();
}
```

### T027-T028: Save/Load Extended JSON
```java
@FXML
private void handleSaveMapping() {
    if (colMappings.isEmpty()) {
        showValidationError("No mappings to save.");
        return;
    }

    // Create MappingConfiguration instead of raw list
    MappingConfiguration config = new MappingConfiguration();
    config.setSchemaVersion("2.0");
    
    // Add file slots
    for (Map.Entry<Integer, File> entry : loadedFiles.entrySet()) {
        config.addFileSlot(new FileSlot(entry.getKey(), entry.getValue().getName()));
    }
    
    // Convert mappings
    for (Map<String, Object> legacy : colMappings) {
        MultiFileMapping mapping = new MultiFileMapping();
        mapping.setSourceFileSlot(
            ((Number) legacy.getOrDefault("sourceFileSlot", 1)).intValue());
        
        int colIndex = ((Number) legacy.get("sourceColumn")).intValue();
        mapping.setSourceColumn(String.valueOf((char)('A' + colIndex)));
        mapping.setTargetCell((String) legacy.get("startCell"));
        
        String dir = (String) legacy.get("direction");
        mapping.setDirection("vertical".equals(dir) 
            ? Direction.VERTICAL : Direction.HORIZONTAL);
        
        config.addMapping(mapping);
    }
    
    // Save with pretty print
    ObjectMapper mapper = new ObjectMapper();
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Mapping File");
    fileChooser.setInitialFileName("multi-file-mapping.json");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("JSON Files", "*.json"));
    File file = fileChooser.showSaveDialog(null);

    if (file != null) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
            showInfo("Multi-file mapping saved to:\\n" + file.getName());
        } catch (IOException e) {
            showValidationError("Failed to save: " + e.getMessage());
        }
    }
}

@FXML
private void handleLoadMapping() {
    // Use MappingUpgrader for backward compatibility
    MappingUpgrader upgrader = new MappingUpgrader();
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Mapping File");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("JSON Files", "*.json"));
    File file = fileChooser.showOpenDialog(null);

    if (file != null) {
        try {
            // Load and auto-upgrade if needed
            MappingConfiguration config = upgrader.loadAndUpgrade(file.toPath());
            
            // Convert to internal format
            colMappings.clear();
            for (MultiFileMapping mfm : config.getMappings()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sourceFileSlot", mfm.getSourceFileSlot());
                
                // Convert column letter to index
                char col = mfm.getSourceColumn().charAt(0);
                map.put("sourceColumn", col - 'A');
                
                map.put("startCell", mfm.getTargetCell());
                map.put("direction", mfm.getDirection() == Direction.VERTICAL 
                    ? "vertical" : "horizontal");
                
                colMappings.add(map);
            }
            
            updateMappingListView();
            updateExcelPreview();
            showInfo("Loaded " + config.getMappings().size() + 
                    " mappings (schema v" + config.getSchemaVersion() + ")");
        } catch (IOException e) {
            showValidationError("Failed to load: " + e.getMessage());
        }
    }
}
```

## Implementation Status

**FXML Changes (T031)**: ✅ Complete
- File slot dropdown added
- Loaded files panel added
- UI layout updated

**Controller Changes Needed**:
- T024: File slot dropdown initialization
- T025-T026: Mapping display format update  
- T027-T028: Extended JSON save/load
- T029-T030: Multi-file loading with validation

## Testing
After implementation, test:
1. Load 2-10 files
2. Create mappings from different files
3. Save as JSON (should have schemaVersion: "2.0")
4. Load v1.0 mapping (should auto-upgrade)
5. Preview shows correct data from all files
