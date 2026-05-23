# Feature Specification: Semicolon Delimiter Column Split

**Feature Branch**: `004-semicolon-delimiter-split`
**Created**: 2026-05-23
**Status**: Draft
**Input**: User description: "Semicolon-delimiter split happen on Load TXT File. The feature is not supposed to be inside Teach Mode."

## Overview

CMM/measurement output files (`.txt`) contain data rows where multiple measurement values are packed into a single line separated by semicolons (`;`). The file is divided into **measurement blocks** (one per cavity), each terminated by an `@101` line.

This feature simplifies the integration of semicolon-delimited files by moving the splitting logic to the very beginning of the data pipeline: **on Load TXT File**. Semicolon splitting is performed automatically during file loading, transforming the raw rows into standard rows with multiple columns.

This design completely eliminates Semicolon-specific modes, previews, and spinners inside **Teach Mode**. Teach Mode remains unchanged and utilizes standard mapping panels (such as Flex Pattern and Manual Rows) over the already split columns.

To support multiple cavity blocks separated by `@101` boundaries, the standard mapping panel gains a **Fixed mapping** checkbox. The export engine automatically processes semicolon-delimited files by partitioning the rows into cavity blocks, writing fixed mappings to static cells, and shifting data mappings column-by-column across cavities.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Automatic Semicolon Split on Load (Priority: P1)

A user loads a CMM file containing semicolon-separated fields. The main screen's `TableView` immediately detects the semicolons and displays the split fields as standard columns (`Col 1`, `Col 2`, etc.), alongside a standard row counter (`Row 1`, `Row 2`, etc.).

**Why this priority**: This is the core expectation. The split must happen on file load so that the loaded data is immediately available as structured columns.

**Independent Test**: Load a semicolon-delimited text file. Verify that the Main screen's preview table displays multiple columns, each containing one of the semicolon-split values, and that lines without semicolons (such as `@101`) are loaded as single-field rows.

**Acceptance Scenarios**:

1. **Given** a text file containing semicolon-delimited rows is selected, **When** loaded, **Then** the main table automatically displays multiple columns with split values.
2. **Given** a line has no semicolons, **When** loaded, **Then** it appears as a single-field row in column 1, and other columns in that row are empty.
3. **Given** consecutive semicolons are present (e.g. `;;`), **When** loaded, **Then** they are treated as empty columns and do not cause parsing to fail.

---

### User Story 2 - Standard Teach Mode Mapping with Fixed Checkbox (Priority: P2)

When entering Teach Mode, the user sees the standard mapping panel. Since the file is already split, they can configure mappings using the standard source columns dropdown, start cell, direction, and row patterns (Flex/Manual).
To prevent row labels from repeating across cavities, the user ticks the **Fixed** checkbox on the label mapping, while leaving it unticked for actual measurement data.

**Why this priority**: The user must be able to teach mappings without any specialized "Semicolon Mode" panels or UI toggles. The "Fixed" checkbox is the only addition needed to support multi-cavity data.

**Independent Test**: Enter Teach Mode for a loaded semicolon-split file. Verify that the UI is the standard Teach Mode UI with a "Fixed" checkbox in the mapping panel, and that no Semicolon-specific preview panel or toggles are visible.

**Acceptance Scenarios**:

1. **Given** a semicolon-split file is active, **When** Teach Mode is opened, **Then** only the standard column mapping panel (with an added Fixed checkbox) is visible.
2. **Given** the user selects `Col 2` (containing measurement labels) and checks "Fixed", **When** mapping is added, **Then** the mapping is saved with `fixed: true`.
3. **Given** the user selects `Col 4` (containing nominal values) and leaves "Fixed" unchecked, **When** mapping is added, **Then** the mapping is saved with `fixed: false`.

---

### User Story 3 - Export with Cavity-Block Offsets (Priority: P2)

When the user exports or appends data for a semicolon-delimited file containing multiple cavity blocks (separated by `@101` boundaries):
- Fixed mappings write their values once to static cells.
- Data (non-fixed) mappings shift right column-by-column across cavities.
- The system automatically groups data mappings targeting the same Excel row to calculate the group width and consecutive columns for interleaving.

**Why this priority**: Allows exporting multi-cavity files correctly to Excel while maintaining standard mapping files.

**Independent Test**: Export a 3-cavity semicolon file with one fixed mapping (Col 2 -> B3) and two data mappings (Col 4 -> C3, Col 5 -> D3). Verify that:
- B3 contains the label once.
- C3, D3 contain nominal/actual for Cavity 1; E3, F3 for Cavity 2; G3, H3 for Cavity 3.

**Acceptance Scenarios**:

1. **Given** a data mapping is configured for a multi-cavity file, **When** exported, **Then** the value for cavity N appears in column `startCol + N * groupWidth`.
2. **Given** two data mappings share the same target row in Excel, **When** exported, **Then** they interleave consecutively for each cavity block.
3. **Given** a fixed mapping is configured, **When** exported, **Then** its value is written once and does not shift across cavities.

---

### Edge Cases

- **Row counts differ per cavity**: Shorter cavity blocks pad with empty values; no errors.
- **Empty cavity blocks (consecutive `@101`)**: Counted as a cavity block; written as empty cells.
- **No `@101` lines in file**: File is treated as a single cavity block; no shifting.
- **Line ending variations (`\r\n` vs `\n`)**: Normalized during load before splitting.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST automatically split any text line containing at least one `;` using semicolon as the delimiter in `TxtParser.parseFile()`.
- **FR-002**: Lines without semicolons (such as headers or `@101`) MUST be parsed as single-column rows containing the raw line.
- **FR-003**: Semicolon-split columns MUST be displayed in the Main screen's `TableView` as standard columns (`Col 1`, `Col 2`, etc.).
- **FR-004**: Semicolon Mode UI (toggles, separate preview table, loaders) MUST be COMPLETELY REMOVED from Teach Mode. Teach Mode MUST use the standard mapping UI.
- **FR-005**: A **Fixed mapping** checkbox MUST be added to the standard column mapping panel in Teach Mode.
- **FR-006**: The `fixed` boolean field MUST be serialized as a standard property in `ColumnMapping` (defaulting to `false`).
- **FR-007**: During export/append, if the text file is semicolon-delimited and contains `@101` boundaries, the system MUST partition the rows into cavity blocks separated by `@101`.
- **FR-008**: For fixed mappings (`fixed: true`), the value MUST be written once to the target Excel cell.
- **FR-009**: For data mappings (`fixed: false`), the value for cavity block `N` (0-indexed) MUST be written to column `startCol + (N * groupWidth) + offsetInGroup`.
- **FR-010**: The `groupWidth` for a row MUST be computed automatically as the count of data (non-fixed) mappings targeting that Excel row. Mappings in the group are ordered by their start cell's column.
- **FR-011**: Legacy mapping files without a `fixed` property MUST load and behave with `fixed` defaulting to `false` (full backward compatibility).

### Key Entities

- **Semicolon-Delimited Row**: A line containing at least one `;`, split into ordered fields.
- **Cavity Block**: A sequence of lines representing one cavity, separated by `@101` boundaries.
- **Fixed Mapping**: A mapping whose target cell does not shift across cavities.
- **Data Mapping**: A mapping whose target cell shifts column-by-column across cavities.
- **Group Width**: The number of data mappings sharing the same Excel row.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of semicolon-delimited files are automatically parsed on load without user intervention.
- **SC-002**: Semicolon Mode toggle and preview tables are completely removed from Teach Mode UI, making the interface 100% consistent with other file formats.
- **SC-003**: Multi-cavity exports write fixed labels once and data values sequentially across cavities with 100% accuracy.
- **SC-004**: Legacy mapping files load and export without any regressions.

---

## Constitution Alignment *(mandatory)*

| Principle | Alignment Check | Notes |
|-----------|-----------------|-------|
| I. User Experience First | Semicolon splitting is completely automatic on load; Teach Mode UI remains clean and unified | Simplifies UI significantly |
| II. Modular Design | Semicolon splitting is integrated into the central TxtParser class; export logic is cleanly decoupled | |
| III. Configuration-Driven | Mappings are saved to standard JSON using the `fixed` property | Backward compatible |
| IV. Quality Testing | High test coverage on auto-split parser and multi-cavity exporter | |
| V. Documentation | User-facing documentation updated to reflect the simplified load-time split | |
| VI. Reusable Components & Open Source | Reuses standard POI and Jackson libraries | |
| VII. Security & Data Handling | Validates and handles all edge cases gracefully | |
