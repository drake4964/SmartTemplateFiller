# Research: Excel Append Mode

**Feature**: 002-excel-append-mode
**Date**: 2026-01-29

## Research Questions

### 1. How to Read and Append to Existing Excel File with Apache POI

**Decision**: Use `XSSFWorkbook(FileInputStream)` to open existing workbook, modify, and rewrite.

**Rationale**: Apache POI supports reading existing `.xlsx` files via `XSSFWorkbook` constructor with `FileInputStream`. After modifying the workbook, write it back using `FileOutputStream`.

**Key Implementation Pattern**:
```java
// Read existing file
try (FileInputStream fis = new FileInputStream(existingFile);
     Workbook workbook = new XSSFWorkbook(fis)) {
    Sheet sheet = workbook.getSheet("Result");
    // Find last row, add data at offset
    int lastRow = sheet.getLastRowNum();
    // Write new data starting at lastRow + 1
    
    // Write back (must close input stream first)
    try (FileOutputStream fos = new FileOutputStream(existingFile)) {
        workbook.write(fos);
    }
}
```

**Alternatives Considered**:
- Create new workbook and copy data: Rejected due to performance overhead and style loss
- Use POI streaming API (SXSSF): Not suitable for read-modify-write pattern

---

### 2. How to Calculate Row Offset from Existing Excel File

**Decision**: Use `Sheet.getLastRowNum()` to find the last occupied row, then add 1 for the offset.

**Rationale**: Apache POI's `getLastRowNum()` returns 0-indexed last row number. For append, new data starts at `lastRowNum + 1`.

**Edge Cases Handled**:
- Empty file (no rows): `getLastRowNum()` returns -1, offset = 0
- File with only headers: `getLastRowNum()` returns header row, offset = 1
- Gaps in data: `getLastRowNum()` returns last row with any data

---

### 3. How to Handle File Locking on Windows

**Decision**: Attempt to open file, catch `FileNotFoundException` or `IOException`, display user-friendly error.

**Rationale**: Windows locks files when open in Excel. POI will throw exception on locked file. Must handle gracefully.

**Error Message Pattern**:
```
"Cannot access file: It may be open in another application. Please close the file and try again."
```

---

### 4. Existing Export Flow Analysis

**Current Flow** (from `MainController.handleExportToExcel`):
1. User selects TXT file
2. User selects mapping JSON
3. User selects output location (save dialog)
4. `ExcelWriter.writeAdvancedMappedFile()` creates new workbook

**Append Flow** (new):
1. User selects TXT file
2. User selects mapping JSON
3. User chooses **append mode** → selects existing Excel file
4. New `ExcelWriter.appendToMappedFile()` opens existing, calculates offset, writes data

---

### 5. Existing Run Mode Flow Analysis

**Current Flow** (from `FolderWatcher.processFile`):
1. Detects file in watch folder
2. Generates unique output path: `{mapping}/{timestamp}/{filename}.xlsx`
3. Calls `ExcelWriter.writeAdvancedMappedFile()`
4. Archives source file

**Append Flow** (new):
1. Detects file in watch folder
2. If append mode ON and `lastGeneratedFilePath` exists:
   - Call `ExcelWriter.appendToMappedFile(source, mapping, lastFile)`
3. Else:
   - Generate new file as before, store path as `lastGeneratedFilePath`
4. Archive source file

---

## Technology Decisions

| Area | Decision | Reason |
|------|----------|--------|
| Excel Library | Apache POI (existing) | Already in project, supports read-write |
| Offset Calculation | `Sheet.getLastRowNum() + 1` | Standard POI API |
| Config Persistence | Extend `RunningModeConfig` | Follows existing pattern |
| UI Toggle | JavaFX CheckBox | Consistent with current UI style |

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| File corruption on error | Write to temp file first, then replace (atomic) |
| Memory issues with large files | POI handles up to ~1M rows; warn user at 100K |
| Column mismatch (different mapping) | Warn user but allow (their responsibility) |
