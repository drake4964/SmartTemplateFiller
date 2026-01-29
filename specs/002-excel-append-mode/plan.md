# Implementation Plan: Excel Append Mode

**Branch**: `002-excel-append-mode` | **Date**: 2026-01-29 | **Spec**: [spec.md](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/specs/002-excel-append-mode/spec.md)
**Input**: Feature specification from `/specs/002-excel-append-mode/spec.md`

## Summary

Add Excel append functionality that allows users to add data to existing Excel files instead of creating new ones. Uses the existing mapping.json with row offset calculation. Extends both manual export and run mode workflows.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: JavaFX 17.0.15, Apache POI 5.2.3, Jackson 2.15.3  
**Storage**: JSON configuration files, Excel files  
**Testing**: JUnit 5 + TestFX (existing)  
**Target Platform**: Windows/macOS/Linux desktop  
**Project Type**: Single-project JavaFX desktop application  
**Performance Goals**: Append 10K rows in <10 seconds  
**Constraints**: Handle files up to 100K rows; graceful error on locked files  
**Scale/Scope**: Single-user desktop application

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. User Experience First | All UI operations provide immediate feedback? Error messages clear & actionable? | ✅ Append dialog with progress; clear errors for locked/corrupted files |
| II. Modular Design | Parsers, mappers, exporters are standalone modules? No circular dependencies? | ✅ `ExcelWriter.appendToMappedFile()` is self-contained; reuses existing parser |
| III. Configuration-Driven | Mappings stored as JSON? Schema versioned? No hardcoded logic? | ✅ Extends `RunningModeConfig`; uses existing mapping.json |
| IV. Quality Testing | Parser tests for all formats? ≥80% coverage target? Edge cases covered? | ✅ Unit tests for offset calc; integration tests for append flow |
| V. Documentation | Feature docs in `/docs/features/`? Architecture decisions recorded? | ✅ Will create `/docs/features/excel-append-mode.md` |
| VI. Reusable Components & Open Source | Using proven libraries (Lombok, Commons)? Components designed for reuse? | ✅ Uses existing Apache POI; no new libraries |
| VII. Security & Data Handling | No sensitive data logged? Temp files cleaned? Input validated? | ✅ Validates Excel file before append; no sensitive logging |

## Project Structure

### Documentation (this feature)

```text
specs/002-excel-append-mode/
├── spec.md              ✅ Complete
├── plan.md              ✅ This file
├── research.md          ✅ Complete
├── data-model.md        ✅ Complete
├── quickstart.md        ✅ Complete
└── tasks.md             (Next: /speckit-tasks)
```

### Source Code (repository root)

```text
src/main/java/com/example/smarttemplatefiller/
├── ExcelWriter.java           # [MODIFY] Add appendToMappedFile method
├── MainController.java        # [MODIFY] Add append option to export dialog
├── RunningModeConfig.java     # [MODIFY] Add appendModeEnabled field
├── RunningModeController.java # [MODIFY] Add append toggle UI binding
├── FolderWatcher.java         # [MODIFY] Add append logic in processFile
├── AppendResult.java          # [NEW] Result class for append operation
└── ExportConfiguration.java   # [NEW] Config class for export dialog

src/main/resources/fxml/
└── running_mode.fxml          # [MODIFY] Add CheckBox for append mode

src/test/java/com/example/smarttemplatefiller/
├── ExcelWriterAppendTest.java # [NEW] Unit tests for append logic
└── AppendIntegrationTest.java # [NEW] Integration tests for full flow

docs/features/
└── excel-append-mode.md       # [NEW] User documentation
```

---

## Proposed Changes

### Phase 1: Core Append Logic (P1)

#### [NEW] [AppendResult.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/AppendResult.java)

Data class to hold append operation results:
- `success` (boolean)
- `rowsAdded` (int)
- `rowOffset` (int)
- `targetFilePath` (String)
- `warnings` (List<String>)
- `errorMessage` (String)

---

#### [MODIFY] [ExcelWriter.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/ExcelWriter.java)

Add new method:
```java
public static AppendResult appendToMappedFile(
    File txtFile, File mappingFile, File existingExcelFile)
```

Implementation:
1. Open existing Excel file with `XSSFWorkbook(FileInputStream)`
2. Get sheet "Result" (or first sheet)
3. Calculate offset: `sheet.getLastRowNum() + 1`
4. Parse source file with `TxtParser.parseFile()`
5. Load mappings from JSON
6. Apply each mapping with row offset added to target cell row
7. Write back to file
8. Return `AppendResult` with details

Add helper:
```java
private static int calculateRowOffset(Sheet sheet)
```

---

### Phase 2: Manual Export Append UI (P1)

#### [NEW] [ExportConfiguration.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/ExportConfiguration.java)

Simple POJO to hold:
- `appendMode` (boolean)
- `appendTargetPath` (String)

---

#### [MODIFY] [MainController.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/MainController.java#L112-L143)

Modify `handleExportToExcel()`:
1. After selecting mapping file, show dialog to choose mode:
   - Radio: "Create New File" (default) or "Append to Existing"
2. If append mode selected, show file browser for existing Excel
3. Call `ExcelWriter.appendToMappedFile()` instead of `writeAdvancedMappedFile()`
4. Show result with rows added

---

### Phase 3: Run Mode Append (P2)

#### [MODIFY] [RunningModeConfig.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/RunningModeConfig.java)

Add fields:
```java
private boolean appendModeEnabled = false;

@JsonIgnore
private transient String lastGeneratedFilePath;
```

Add getters/setters.

---

#### [MODIFY] [running_mode.fxml](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/resources/fxml/running_mode.fxml)

Add CheckBox in Configuration section:
```xml
<CheckBox fx:id="appendModeCheckBox" text="Append Mode (accumulate to single file)"/>
```

---

#### [MODIFY] [RunningModeController.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/RunningModeController.java)

1. Add FXML binding for `appendModeCheckBox`
2. Update `loadConfigToUI()` and `saveConfig()` to include append setting
3. Add logic for "continue or start fresh" prompt on restart

---

#### [MODIFY] [FolderWatcher.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/main/java/com/example/smarttemplatefiller/FolderWatcher.java#L136-L177)

Modify `processFile()`:
1. Add constructor param or setter for `appendModeEnabled`
2. Track `lastGeneratedFilePath` field
3. If append mode ON and file exists:
   - Call `ExcelWriter.appendToMappedFile()`
   - Log offset and rows added
4. Else create new file as before

---

### Phase 4: Configuration Persistence (P3)

Already handled by Phase 3 changes to `RunningModeConfig` - the `save()` and `load()` methods automatically persist `appendModeEnabled`.

---

### Phase 5: Testing & Documentation

#### [NEW] [ExcelWriterAppendTest.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/test/java/com/example/smarttemplatefiller/ExcelWriterAppendTest.java)

Unit tests:
- `testAppendToEmptyFile()` - offset should be 0
- `testAppendToFileWithData()` - offset calculated correctly
- `testAppendPreservesExistingData()` - existing rows unchanged
- `testAppendWithLockedFile()` - appropriate error returned
- `testCalculateRowOffset()` - edge cases (gaps, empty)

#### [NEW] [AppendIntegrationTest.java](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/src/test/java/com/example/smarttemplatefiller/AppendIntegrationTest.java)

Integration tests:
- `testManualExportAppendFlow()` - full manual append workflow
- `testRunModeAppendMultipleFiles()` - run mode with 3 sequential files

#### [NEW] [excel-append-mode.md](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/docs/features/excel-append-mode.md)

User documentation covering:
- How to use append in export
- How to use append in run mode
- How row offset works
- Troubleshooting (locked files, column mismatch)

---

## Verification Plan

### Automated Tests

**Run Unit Tests**:
```powershell
./gradlew test --tests "com.example.smarttemplatefiller.ExcelWriterAppendTest"
```

**Run Integration Tests**:
```powershell
./gradlew test --tests "com.example.smarttemplatefiller.AppendIntegrationTest"
```

**Run All Tests**:
```powershell
./gradlew test
```

### Manual Verification

**Manual Export Append Test**:
1. Launch application: `./gradlew run`
2. Click "Export to Excel"
3. Select any test TXT file from `src/test/resources/` (or create one)
4. Select a mapping JSON file
5. Choose "Create New File" and save as `test_output.xlsx`
6. Repeat steps 2-4 with a different TXT file
7. Choose "Append to Existing" and select `test_output.xlsx`
8. Open `test_output.xlsx` in Excel and verify:
   - Original data is preserved
   - New data appears after original data
   - Headers are not duplicated

**Run Mode Append Test**:
1. Launch application: `./gradlew run`
2. Click "Running Mode"
3. Configure mapping, watch folder, output folder
4. Check "Append Mode" checkbox
5. Click "Start Watching"
6. Copy a TXT file to watch folder
7. Verify Excel file created in output folder
8. Copy another TXT file to watch folder
9. Verify data appended to SAME Excel file (no new file created)
10. Check activity log shows "Appended X rows to {filename}"

---

## Complexity Tracking

No constitution violations. Complexity is well-bounded:
- No new external dependencies
- Follows existing patterns
- UI changes are additive, not breaking
