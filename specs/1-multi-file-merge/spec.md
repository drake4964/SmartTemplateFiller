# Feature Specification: Multi-File Merge Export

**Feature Branch**: `1-multi-file-merge`  
**Created**: 2026-01-16  
**Status**: Clarified  
**Input**: User description: "Multi-file merge with folder watching"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Merge Multiple Files to Single Output (Priority: P1)

A QA engineer needs to combine measurement data from multiple input files (e.g., 2-10 TXT files from different machines) into a single Excel report where each file's data goes to specific cells.

**Why this priority**: This is the core value proposition - without merging multiple files, users must manually copy-paste data between reports.

**Independent Test**: Can be fully tested by loading 2+ TXT files, creating a multi-file mapping, and verifying the output Excel contains data from all input files in the correct cells.

**Acceptance Scenarios**:

1. **Given** two TXT files are loaded, **When** user creates mappings for File1→A1 and File2→A2, **Then** the exported Excel shows File1 data in A1 and File2 data in A2
2. **Given** 10 TXT files are loaded (maximum), **When** user exports, **Then** all 10 files' data appears in the configured cells
3. **Given** user tries to load 11+ files, **When** the limit is reached, **Then** the system shows a clear message about the 10-file limit

---

### User Story 2 - Multi-File Teaching Mode (Priority: P2)

A user needs to configure which input file maps to which output cell, with visual feedback showing the file-to-cell relationship.

**Why this priority**: Teaching mode is how users configure the multi-file mapping - it's essential but depends on the core merge capability.

**Independent Test**: Can be tested by entering Teach Mode with multiple files loaded and verifying the UI allows file-specific cell assignments.

**Acceptance Scenarios**:

1. **Given** user is in Teach Mode with 3 files loaded, **When** user selects File1 and maps column to cell A1, **Then** the mapping shows "File1:Column → A1"
2. **Given** multiple file mappings exist, **When** user views the mapping list, **Then** each mapping clearly shows which source file it belongs to
3. **Given** user saves the multi-file mapping, **When** the JSON is created, **Then** the JSON includes file source identifiers for each mapping

---

### User Story 3 - Folder Watch Auto-Processing (Priority: P3)

A production operator wants the system to automatically process files when all required input files are present in watched folders.

**Why this priority**: Automation is valuable but requires the core merge and teaching features to be complete first.

**Independent Test**: Can be tested by configuring a 2-file template, placing files in watched folders, and verifying automatic export triggers only when both files exist.

**Acceptance Scenarios**:

1. **Given** a template requires 2 input files (Folder1, Folder2), **When** only Folder1 has a file, **Then** the system waits and does not process
2. **Given** a template requires 2 input files, **When** both folders have matching files, **Then** the system automatically processes and creates output
3. **Given** processing completes successfully, **When** the output is created, **Then** source files are handled according to user preference (keep/move/delete)
4. **Given** a template requires 1 input file, **When** that file appears in the watched folder, **Then** the system processes immediately without waiting for other folders

---

### Edge Cases (Resolved)

- **Missing column**: If a mapped column doesn't exist in an input file → Skip with warning in log
- **File name conflict**: Watched folders never have duplicate file names - new files auto-replace existing (OS behavior)
- **File being written**: Stability check (default 2 seconds) ensures file is complete before processing
- **Corrupted input file**: Abort processing with clear error message, log details for troubleshooting

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support combining 2 to 10 input files into a single output file
- **FR-002**: System MUST allow users to specify flexible mappings: row-based, column-based, or mixed - fully customizable via Teach Mode JSON template
- **FR-003**: System MUST display file source identifier in Teach Mode mapping list
- **FR-004**: System MUST save file source information in the JSON mapping configuration
- **FR-005**: System MUST support watching multiple folders (one per expected input file) - session only, does not persist after app close
- **FR-006**: System MUST wait for all required files before auto-processing, matching files by prefix (text before first underscore) or exact basename
- **FR-007**: System MUST provide visual indicator showing which watched folders have files ready
- **FR-008**: System MUST archive source files and output file together in a timestamped subfolder of the output directory (format configurable: date-only or date+time) for traceability
- **FR-009**: System MUST detect when a file is completely written before processing (configurable stability check, default 2 seconds)
- **FR-010**: System MUST log all auto-processing events for troubleshooting

### Key Entities

- **MultiFileMapping**: Extends existing Mapping to include source file identifier (1-10)
- **WatchFolder**: Represents a folder to monitor, linked to a source file slot
- **ProcessingJob**: Represents a pending or completed auto-processing task

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can combine up to 10 files into a single output within 30 seconds
- **SC-002**: Teach Mode multi-file configuration takes no more than 2 minutes for a 3-file template
- **SC-003**: Auto-processing triggers within 5 seconds of all required files being available
- **SC-004**: 95% of auto-processing jobs complete without user intervention
- **SC-005**: Users can configure folder watching in under 1 minute per folder

## Constitution Alignment *(mandatory)*

| Principle | Alignment Check | Notes |
|-----------|-----------------|-------|
| I. User Experience First | Immediate feedback when files are loaded; clear status for folder watching | Loading progress indicator needed |
| II. Modular Design | File merger as separate module from existing single-file exporter | |
| III. Configuration-Driven | Multi-file mappings stored in extended JSON schema | Schema version bump needed |
| IV. Quality Testing | Test cases for 2, 5, 10 file merges; folder watching tests | |
| V. Documentation | User guide for multi-file mode needed in /docs/features/ | |
| VI. Reusable Components & Open Source | Use java.nio.file.WatchService for folder watching | |
| VII. Security & Data Handling | Validate watched folder paths; no sensitive data in logs | |

## Clarifications

### Session 2026-01-17

- Q: How should the system match files across watched folders? → A: Prefix-based name matching (text before first underscore) OR exact basename matching. Examples: `PART001_001.txt` and `PART001_002.txt` match (same prefix "PART001"); `PART001.txt` and `PART001.txt` match (exact basename).
- Q: How long should the system wait for file stability check? → A: Configurable by user, default 2 seconds.
- Q: How are multiple files merged - by rows or columns? → A: Flexible, user-defined via Teach Mode. Can be row-based, column-based, or mixed mapping. Each mapping specifies: Source File + Source Data → Output Location. Fully customizable per JSON template.
- Q: What happens to source files after processing? → A: Move to archive subfolder in output directory with configurable timestamp format (date-only or date+time). Output file and archived input files stored together for traceability.
- Q: Does folder watching persist after app restart? → A: No, session only. User must reconfigure folder watching each time app is opened.
