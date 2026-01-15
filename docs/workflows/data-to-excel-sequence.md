# Sequence: Data to Excel Export

## Overview

This sequence diagram describes the complete workflow from loading a data file to exporting an Excel report.

## Actors

- **User**: Initiates actions via UI
- **MainController**: Central coordinator for main window
- **TxtParser**: File parsing service
- **TeachModeController**: Mapping configuration dialog
- **ExcelWriter**: Excel file generation

## Sequence Diagram

See [sequence-export-workflow.mmd](../diagrams/sequence-export-workflow.mmd) for the visual diagram.

## Step-by-Step Flow

### Phase 1: Load Data

| Step | Actor | Action |
|------|-------|--------|
| 1 | User | Clicks "Load TXT File" |
| 2 | MainController | Opens FileChooser dialog |
| 3 | User | Selects TXT/ASC file |
| 4 | MainController | Calls `TxtParser.parseFile()` |
| 5 | TxtParser | Detects format, parses data |
| 6 | TxtParser | Returns `List<List<String>>` |
| 7 | MainController | Populates TableView with columns and rows |

### Phase 2: Configure Mapping (Optional)

| Step | Actor | Action |
|------|-------|--------|
| 8 | User | Clicks "Teach Mode" |
| 9 | MainController | Validates data exists |
| 10 | MainController | Opens teach_mode.fxml dialog |
| 11 | TeachModeController | Receives TableView reference |
| 12 | User | Configures column mappings |
| 13 | User | Clicks "Save Mapping" |
| 14 | TeachModeController | Saves JSON to file |
| 15 | User | Closes dialog |

### Phase 3: Export

| Step | Actor | Action |
|------|-------|--------|
| 16 | User | Clicks "Export to Excel" |
| 17 | MainController | Opens FileChooser for TXT file |
| 18 | MainController | Opens FileChooser for mapping JSON |
| 19 | MainController | Opens FileChooser for output path |
| 20 | MainController | Calls `ExcelWriter.writeAdvancedMappedFile()` |
| 21 | ExcelWriter | Re-parses TXT file |
| 22 | ExcelWriter | Loads mapping JSON |
| 23 | ExcelWriter | Creates XSSFWorkbook |
| 24 | ExcelWriter | Writes data to cells per mapping |
| 25 | ExcelWriter | Saves XLSX file |
| 26 | MainController | Shows success alert |

## Error Paths

| Condition | Handler | Response |
|-----------|---------|----------|
| No file selected (any dialog) | MainController | Abort export silently |
| Parse exception | TxtParser | Returns empty list |
| Write exception | ExcelWriter | Prints stack trace |
| No data for teach mode | MainController | Shows warning alert |
