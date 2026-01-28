# Source Code Map

> **Purpose**: Quick reference for understanding class responsibilities and relationships.  
> **Last Updated**: 2026-01-28

## Package Structure

```
com.example.smarttemplatefiller
├── MainApp.java                 # Application entry point
├── MainController.java          # Main window controller
├── TeachModeController.java     # Teach Mode UI controller
├── RunningModeController.java   # Running Mode UI controller
├── TxtParser.java               # File parsing strategies
├── ExcelWriter.java             # Excel file generation
├── FileChooserBuilder.java      # Fluent file dialog API
├── FolderWatcher.java           # Background folder monitoring
└── RunningModeConfig.java       # Running Mode configuration POJO
```

---

## Class Details

### MainApp.java (Entry Point)

```java
public class MainApp extends Application {
    void start(Stage primaryStage)  // Load main.fxml, show window
    static void main(String[] args) // Launch JavaFX application
}
```

**Lines**: ~30 | **Dependencies**: JavaFX, MainController

---

### MainController.java (Main Window)

```java
public class MainController {
    // Fields
    private Stage stage;
    private TableView<List<String>> tableView;
    
    // Public Methods
    void setStage(Stage stage)
    void handleLoadTxt(ActionEvent event)        // Load TXT/ASC file → parse → display
    void handleTeachMode(ActionEvent event)      // Open Teach Mode dialog
    void handleExportToExcel(ActionEvent event)  // Export using TXT + JSON mapping
    void handleRunningMode(ActionEvent event)    // Open Running Mode window
    
    // Private Methods
    void showAlert(String title, String message) // Error dialog
    void showInfo(String title, String message)  // Info dialog
}
```

**Lines**: 185 | **Dependencies**: TxtParser, TeachModeController, ExcelWriter, FileChooserBuilder, RunningModeController

---

### TeachModeController.java (Mapping Configuration)

```java
public class TeachModeController {
    // FXML UI Fields
    private ComboBox<String> columnComboBox;
    private TextField cellField;
    private ComboBox<String> directionComboBox;
    private ComboBox<String> rowPatternComboBox;
    private TextField manualRowField;
    private TextField titleField;
    private ListView<String> mappingListView;
    private TableView<List<String>> excelPreviewTable;
    private Label previewStatusLabel;
    
    // Data
    private TableView<List<String>> tableView;  // Reference to main table
    private List<Map<String, Object>> colMappings = new ArrayList<>();
    
    // Public Methods
    void setTableView(TableView<List<String>> tableView)  // Inject data source
    void initialize()                                      // FXML lifecycle
    void handleAddMapping()                                // Add mapping to list
    void handleDeleteSelected()                            // Remove selected mapping
    void handleMoveUp()                                    // Move mapping up
    void handleMoveDown()                                  // Move mapping down
    void handleClearAll()                                  // Clear all with confirmation
    void handleSaveMapping()                               // Export to JSON file
    void handleLoadMapping()                               // Import from JSON file
    
    // Private Methods
    void setupDragAndDrop()           // Configure ListView drag-drop
    void updateMappingListView()      // Refresh ListView display
    void updateExcelPreview()         // Build Excel-like preview grid
    void clearInputFields()           // Reset input form
    void showValidationError(String)  // Error dialog
    void showInfo(String)             // Info dialog
    
    // Inner Class
    class MappingData {               // Helper for processed mapping data
        int sourceCol, startRow, startCol;
        String direction, title;
        List<String> values;
    }
}
```

**Lines**: 560 | **Dependencies**: TxtParser (generateIndexes), Jackson (ObjectMapper), Apache POI (CellReference)

---

### RunningModeController.java (Automated Processing)

```java
public class RunningModeController implements Initializable {
    // FXML UI Fields
    private TextField mappingFileField;
    private TextField watchFolderField;
    private TextField outputFolderField;
    private TextField filePatternField;
    private TextField intervalField;
    private TextArea logArea;
    private Button startButton, stopButton;
    private Label statusLabel;
    
    // Data
    private Stage stage;
    private RunningModeConfig config;
    private FolderWatcher watcher;
    
    // Lifecycle
    void initialize(URL location, ResourceBundle resources)
    void setStage(Stage stage)
    
    // UI Handlers
    void handleBrowseMapping(ActionEvent event)
    void handleBrowseWatchFolder(ActionEvent event)
    void handleBrowseOutputFolder(ActionEvent event)
    void handleStart(ActionEvent event)         // Start folder watching
    void handleStop(ActionEvent event)          // Stop folder watching
    void handleOpenOutputFolder(ActionEvent event)
    void handleClose(ActionEvent event)
    
    // Private Methods
    void loadConfigToUI()                       // Load saved config
    void saveConfig()                           // Persist config to JSON
    boolean validateConfig()                    // Validate before start
    void setConfigFieldsDisabled(boolean)       // Toggle UI state
    void logMessage(String message)             // Append to log area
    void showAlert(String, String)              // Error dialog
}
```

**Lines**: 270 | **Dependencies**: FolderWatcher, RunningModeConfig

---

### TxtParser.java (File Parsing)

```java
public class TxtParser {
    // Strategy Entry Point
    static List<List<String>> parseFile(File file)
    // → Auto-detects format:
    //   - CMM block → parseMultiLineGroupedBlock()
    //   - Fixed-width → parseFixedColumnTable()
    //   - Default → parseFlatTable()
    
    // Parsing Strategies
    static List<List<String>> parseFixedColumnTable(File file)
    static List<List<String>> parseMultiLineGroupedBlock(File file)
    static List<List<String>> parseFlatTable(File file)
    
    // Configuration
    private static Map<String, Integer> loadColumnConfig()  // Load column_config.json
    
    // Row Index Generation
    static List<Integer> generateIndexes(int totalRows, String patternType, int startIndex)
    // → "odd"  → Row 1, 3, 5... (1-based display)
    // → "even" → Row 2, 4, 6...
    // → "all"  → All rows from startIndex
}
```

**Lines**: 189 | **Dependencies**: Jackson (for column_config.json)

**Detection Patterns**:
```java
isBlockHeader = line.matches("(?i)(Circle|Line|Plane|Point|Distance|Angle).*\\(ID:.*\\).*");
isFixedColumn = line.matches("\\s*\\d+\\s+N\\d+\\s+.*\\s+\\*+.*");
```

---

### ExcelWriter.java (Excel Generation)

```java
public class ExcelWriter {
    static void writeAdvancedMappedFile(File txtFile, File mappingFile, File outputFile)
    // Steps:
    // 1. Parse txtFile using TxtParser.parseFile()
    // 2. Load mappings from JSON
    // 3. Create XSSFWorkbook
    // 4. For each mapping:
    //    a. Parse startCell using CellReference
    //    b. Determine row indexes (rowPattern or rowIndexes)
    //    c. Write title if specified
    //    d. Write data vertically or horizontally
    // 5. Save to outputFile
}
```

**Lines**: 107 | **Dependencies**: TxtParser, Apache POI (XSSFWorkbook, CellReference), Jackson

---

### FileChooserBuilder.java (File Dialogs)

```java
public class FileChooserBuilder {
    // Static State
    private static File lastDirectory;  // Remembered across all dialogs
    
    // Instance Fields
    private Stage stage;
    private FileChooser chooser;
    
    // Constructor
    FileChooserBuilder(Stage stage)
    
    // Fluent API
    FileChooserBuilder withTitle(String title)
    FileChooserBuilder withExtension(String label, String... extensions)
    FileChooserBuilder withInitialDirectory(File directory)
    
    // Actions
    File open()                      // Show open dialog
    File save(String defaultFileName) // Show save dialog
    
    // Static
    static void resetLastDirectory()
}
```

**Lines**: ~88 | **Dependencies**: JavaFX FileChooser

---

### FolderWatcher.java (Background Processing)

```java
public class FolderWatcher {
    // Configuration
    private Path watchFolder;
    private Set<String> fileExtensions;
    private File mappingFile;
    private Path outputFolder;
    private int intervalSeconds;
    private Consumer<String> logCallback;
    
    // State
    private ScheduledExecutorService scheduler;
    private volatile boolean running;
    private final Set<String> processingFiles;  // Prevent duplicate processing
    
    // Constructor
    FolderWatcher(RunningModeConfig config, Consumer<String> logCallback)
    
    // Lifecycle
    void start()      // Start scheduled scanning
    void stop()       // Stop with graceful shutdown
    boolean isRunning()
    
    // Processing
    private void scanFolder()           // Iterate folder, match extensions
    private void processFile(File)      // Convert to Excel, archive original
    // Output structure: outputFolder/mappingName/timestamp/file.xlsx
    //                   outputFolder/mappingName/timestamp/archive/original.txt
    
    private void log(String message)    // Callback with timestamp
}
```

**Lines**: 188 | **Dependencies**: ExcelWriter, RunningModeConfig, Java NIO

---

### RunningModeConfig.java (Configuration POJO)

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunningModeConfig {
    // Fields
    private String mappingFile;
    private String watchFolder;
    private String outputFolder;
    private String filePattern = "*.txt,*.asc";
    private int intervalSeconds = 1;
    
    // Constants
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.smarttemplatefiller";
    private static final String CONFIG_FILE = CONFIG_DIR + "/running_mode_config.json";
    
    // Persistence
    void save() throws IOException      // Write to CONFIG_FILE
    static RunningModeConfig load()     // Read from CONFIG_FILE or return default
    
    // Getters and Setters (standard POJO)
}
```

**Lines**: 103 | **Dependencies**: Jackson (JSON persistence)

---

## Class Dependency Graph

```
                    ┌─────────────┐
                    │   MainApp   │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │MainController│
                    └──────┬──────┘
           ┌───────────────┼───────────────┬───────────────┐
           │               │               │               │
           ▼               ▼               ▼               ▼
    ┌────────────┐  ┌───────────┐  ┌────────────┐  ┌──────────────────┐
    │ TxtParser  │  │ExcelWriter│  │TeachMode   │  │RunningMode       │
    │            │  │           │  │Controller  │  │Controller        │
    └────────────┘  └─────┬─────┘  └──────┬─────┘  └────────┬─────────┘
           ▲              │               │                 │
           └──────────────┘               │                 │
                                          │                 │
                                          ▼                 ▼
                              ┌────────────────┐   ┌───────────────┐
                              │FileChooser     │   │FolderWatcher  │
                              │Builder         │   │               │
                              └────────────────┘   └───────┬───────┘
                                                           │
                                                  ┌────────▼────────┐
                                                  │RunningModeConfig│
                                                  └─────────────────┘
```

---

## FXML to Controller Mapping

| FXML File | Controller | Window Type |
|-----------|------------|-------------|
| `main.fxml` | `MainController` | Primary Stage |
| `teach_mode.fxml` | `TeachModeController` | Modal Dialog |
| `running_mode.fxml` | `RunningModeController` | Non-modal Window |

---

## Key Algorithms

### Row Pattern Generation (TxtParser.generateIndexes)
```
Input: totalRows=10, patternType="odd", startIndex=0
Process: For i from startIndex to totalRows-1:
         rowNum = i + 1 (convert to 1-based)
         if "odd" and rowNum % 2 == 1 → include
Output: [0, 2, 4, 6, 8] (0-based indexes for rows 1, 3, 5, 7, 9)
```

### File Format Detection (TxtParser.parseFile)
```
1. Read first 10 lines as preview
2. Check for CMM block pattern (Circle, Line, Plane...)
3. Check for fixed-column pattern (digits + N-prefix)
4. Default to flat table parsing
```

### Folder Watching (FolderWatcher)
```
1. ScheduledExecutorService runs scanFolder() every N seconds
2. scanFolder() lists files matching extension pattern
3. processFile() skips if file is in processingFiles set
4. On success: move original to archive subfolder
```
