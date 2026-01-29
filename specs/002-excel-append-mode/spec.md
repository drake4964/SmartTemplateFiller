# Feature Specification: Excel Append Mode

**Feature Branch**: `002-excel-append-mode`  
**Created**: 2026-01-29  
**Status**: Draft  
**Input**: User description: "Option to append on top of existing Excel files using the existing mapping.json from teach mode. In export mode, choose existing file to append. In run mode, auto-append to last generated file when new source files arrive."

## Core Concept: Mapping-Based Append

The append feature **reuses the existing mapping.json** from teach mode. The mapping defines source-to-target cell relationships, and append mode applies a **row offset** to write new data after existing records.

**Example Flow**:
1. **First Export**: Source file values from A2, B2, C3 (per mapping) → Written to target cells A1, A2, A3
2. **Append Mode**: User selects the generated file (A1, A2, A3 already filled)
3. **Second Export**: Source file values from A2, B2, C3 (per mapping) → Written to A4, A5, A6 (offset by 3 rows)

The system calculates the row offset by finding the last occupied row in the target file.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manual Export with Append Option (Priority: P1)

When manually exporting data to Excel, the user wants the option to append data to an existing Excel file instead of always creating a new file. The system uses the same mapping.json to determine source-to-target relationships and applies a row offset.

**Why this priority**: This is the core functionality that enables users to consolidate data from multiple source files into a single Excel workbook without losing existing records. It provides immediate value for users who process data incrementally.

**Independent Test**: Can be fully tested by loading a source file, selecting an existing Excel file as target, and verifying that new data rows appear after existing rows using the same mapping with row offset applied.

**Acceptance Scenarios**:

1. **Given** user has loaded a source file and a mapping.json, **When** user selects "Append to Existing File" and chooses an existing Excel file, **Then** export applies the mapping with a row offset to write data after existing records
2. **Given** target file has 3 rows of data from a previous export, **When** user appends with mapping that writes to A1, A2, A3, **Then** new data is written to A4, A5, A6 (offset = 3)
3. **Given** user selects an existing Excel file that has headers, **When** user exports with append mode, **Then** only data rows are added (headers are not duplicated)
4. **Given** user selects "Create New File" option (default behavior), **When** user exports, **Then** a new Excel file is created (existing behavior unchanged)
5. **Given** user selects an existing Excel file, **When** the file is currently open in another application, **Then** system displays a clear error message and does not corrupt the file

---

### User Story 2 - Run Mode with Append Behavior (Priority: P2)

When using automated "Run Mode" with a watch folder, the user wants the option to append data from incoming files to the previously generated Excel file instead of creating a new file for each source file. The same mapping.json is applied with progressive row offsets.

**Why this priority**: Extends the append capability to automated workflows. Depends on P1 core append logic being implemented first.

**Independent Test**: Enable run mode with append option, drop multiple source files sequentially into watch folder, verify single Excel output grows with each file processed using the same mapping with increasing row offsets.

**Acceptance Scenarios**:

1. **Given** run mode is enabled with "Append Mode" checked and a mapping.json is loaded, **When** the first source file appears in watch folder, **Then** system generates a new Excel file using the mapping
2. **Given** run mode append is active and an Excel file exists with 3 rows from previous processing, **When** a new source file appears in watch folder, **Then** the mapping is applied with row offset 3 to append new data
3. **Given** run mode append processes 5 files sequentially, **When** export completes, **Then** the single Excel file contains 5 sets of data rows without duplicated headers
4. **Given** run mode append is active, **When** user stops and restarts run mode, **Then** system prompts whether to continue appending to the last file or start fresh
5. **Given** run mode append is active, **When** the target Excel file is deleted or moved, **Then** system creates a new Excel file and logs a warning

---

### User Story 3 - Append Mode Configuration Persistence (Priority: P3)

User preferences for append mode should be remembered between sessions so users don't have to reconfigure each time.

**Why this priority**: Quality-of-life enhancement. Core append functionality must work first (P1, P2) before optimizing user experience.

**Independent Test**: Enable append mode, close application, reopen and verify append mode setting is preserved.

**Acceptance Scenarios**:

1. **Given** user enables append mode in export dialog, **When** user closes and reopens the application, **Then** the append mode preference is restored
2. **Given** user enables append mode in run mode settings, **When** user closes and reopens the application, **Then** the run mode append preference is restored

---

### Edge Cases

- What happens when the target Excel file is corrupted or unreadable?
  - System should display error, offer to create new file instead
- What happens when target Excel file was created with a different mapping than currently loaded?
  - System should warn user about potential column mismatch but allow append (user's responsibility to ensure compatibility)
- What happens when target Excel file has protected sheets?
  - System should display error about locked sheets
- How does system handle very large Excel files (approaching Excel row limits)?
  - System should warn user when approaching 1,048,576 row limit
- What happens if source file has no data rows?
  - No rows are appended, operation completes successfully with log message
- How is the row offset calculated when the target file has gaps (empty rows)?
  - System finds the last row with any data and uses that as the offset base

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an "Append to Existing File" option in the export dialog
- **FR-002**: System MUST provide a file browser to select an existing Excel file as append target
- **FR-003**: System MUST reuse the currently loaded mapping.json when performing append operations
- **FR-004**: System MUST calculate a row offset based on the last occupied row in the target Excel file
- **FR-005**: System MUST apply the row offset to all target cell references defined in the mapping
- **FR-006**: System MUST NOT duplicate header rows when appending to a file that already has headers
- **FR-007**: System MUST preserve existing data in the target Excel file (read-modify-write pattern)
- **FR-008**: System MUST provide a "Run Mode Append" toggle in run mode configuration
- **FR-009**: System MUST track the last generated/appended Excel file path during run mode session
- **FR-010**: System MUST persist append mode preferences to user configuration
- **FR-011**: System MUST display clear error messages when append operation fails (file locked, corrupted, etc.)
- **FR-012**: System MUST log all append operations with source file name, target file name, rows added, and offset applied

### Key Entities

- **ExportConfiguration**: Extended to include `appendMode` (boolean) and `appendTargetPath` (string, optional)
- **RunModeConfiguration**: Extended to include `appendModeEnabled` (boolean) and `lastGeneratedFilePath` (string, transient)
- **AppendResult**: Represents outcome of append operation (success/failure, rows added, row offset applied, target file path, any warnings)
- **RowOffset**: The calculated number of rows to shift target cell references (derived from last occupied row in target file)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete an append-to-existing-file export in under 10 seconds for files with less than 10,000 rows
- **SC-002**: Append operation preserves 100% of existing data in target Excel file (no data loss)
- **SC-003**: Row offset is correctly calculated and applied in 100% of append operations
- **SC-004**: 90% of users successfully complete their first append operation without encountering unexpected errors
- **SC-005**: Run mode append processes files at the same speed as create-new-file mode (no more than 5% performance regression)

## Constitution Alignment *(mandatory)*

| Principle | Alignment Check | Notes |
|-----------|-----------------|-------|
| I. User Experience First | ✅ Append option clearly visible in export dialog; clear feedback on success/failure; progress indicator for append operations | |
| II. Modular Design | ✅ Append logic implemented in ExcelExporter module; row offset calculation is a reusable utility; reusable across manual and run mode | |
| III. Configuration-Driven | ✅ Append preferences stored in existing JSON configuration schema; reuses existing mapping.json | |
| IV. Quality Testing | ✅ Unit tests for row offset calculation; unit tests for append logic; integration tests for full workflow; edge cases defined | |
| V. Documentation | ✅ User-facing docs will be created in `/docs/features/excel-append-mode.md` | |
| VI. Reusable Components & Open Source | ✅ Uses existing Apache POI for Excel operations; no new libraries required | |
| VII. Security & Data Handling | ✅ No sensitive data exposed; temp files cleaned up; file validation before operations | |
