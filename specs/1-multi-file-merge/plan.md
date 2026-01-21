# Implementation Plan: Multi-File Merge Export

**Branch**: `1-multi-file-merge` | **Date**: 2026-01-17 | **Spec**: [spec.md](file:///c:/Users/user/Desktop/FTX/workspace/SmartTemplateFiller/specs/1-multi-file-merge/spec.md)
**Input**: Feature specification from `/specs/1-multi-file-merge/spec.md`

## Summary

Extend SmartTemplateFiller to support combining 2-10 input TXT/ASC files into a single Excel output, with flexible row/column-based mappings defined via Teach Mode. Add folder watching capability for automated processing when all required input files are detected.

## Technical Context

**Language/Version**: Java 17 (existing)
**Primary Dependencies**: JavaFX 17.0.15, Apache POI 5.2.3, Jackson 2.15.3 (existing)
**New Dependencies**: java.nio.file.WatchService (built-in JDK for folder watching)
**Storage**: JSON mapping files (extended schema with file source identifiers)
**Testing**: JUnit 5 (existing), TestFX for UI tests
**Target Platform**: Windows Desktop (primary), cross-platform compatible
**Performance Goals**: Process 10 files within 30 seconds, auto-process within 5 seconds of file detection
**Constraints**: Session-only folder watching (no persistence), 2-10 files limit

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. User Experience First | All UI operations provide immediate feedback? Error messages clear & actionable? | ✅ Pass - Loading indicators, clear file status |
| II. Modular Design | Parsers, mappers, exporters are standalone modules? No circular dependencies? | ✅ Pass - MultiFileMerger as new module |
| III. Configuration-Driven | Mappings stored as JSON? Schema versioned? No hardcoded logic? | ✅ Pass - Extended JSON schema v2.0 |
| IV. Quality Testing | Parser tests for all formats? ≥80% coverage target? Edge cases covered? | ✅ Pass - Test plan includes 2/5/10 file tests |
| V. Documentation | Feature docs in `/docs/features/`? Architecture decisions recorded? | ✅ Pass - User guide planned |
| VI. Reusable Components & Open Source | Using proven libraries? Components designed for reuse? | ✅ Pass - WatchService, existing libs |
| VII. Security & Data Handling | No sensitive data logged? Temp files cleaned? Input validated? | ✅ Pass - Path validation, log safety |

## Project Structure

### Documentation (this feature)

```text
specs/1-multi-file-merge/
├── spec.md              # Feature specification (completed)
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── checklists/
    └── requirements.md  # Quality checklist (completed)
```

### Source Code (repository root)

```text
src/main/java/org/smarttemplatefiller/
├── model/
│   ├── MultiFileMapping.java       # [NEW] Extended mapping with file source
│   ├── WatchFolder.java            # [NEW] Folder monitoring config
│   ├── ProcessingJob.java          # [NEW] Auto-processing job tracking
│   └── MappingSchema.java          # [MODIFY] Add schemaVersion field
├── service/
│   ├── MultiFileMergeService.java  # [NEW] Core merge logic
│   ├── FolderWatchService.java     # [NEW] Folder monitoring
│   ├── FileMatchingService.java    # [NEW] Prefix/basename matching
│   └── ArchiveService.java         # [NEW] Timestamped archiving
├── ui/
│   ├── TeachModeController.java    # [MODIFY] Multi-file UI
│   ├── RunModeController.java      # [MODIFY] Folder watching UI
│   └── FileStatusIndicator.java    # [NEW] Visual folder status
└── util/
    └── FileStabilityChecker.java   # [NEW] Configurable stability check

src/test/java/org/smarttemplatefiller/
├── service/
│   ├── MultiFileMergeServiceTest.java  # [NEW]
│   ├── FolderWatchServiceTest.java     # [NEW]
│   └── FileMatchingServiceTest.java    # [NEW]
└── integration/
    └── MultiFileExportIntegrationTest.java  # [NEW]
```

## Phases

### Phase 1: Setup (Foundation)
- Create new model classes
- Extend JSON schema to v2.0 with schemaVersion field
- Add file source identifier to mapping structure

### Phase 2: Core Merge (User Story 1 - P1)
- Implement MultiFileMergeService
- Support 2-10 input files
- Flexible row/column/mixed mapping
- Unit tests for merge logic

### Phase 3: Multi-File Teaching (User Story 2 - P2)
- Extend TeachModeController for file selection
- Display file source in mapping list
- Save/load extended JSON format
- UI integration tests

### Phase 4: Folder Watching (User Story 3 - P3)
- Implement FolderWatchService with WatchService
- Implement FileMatchingService (prefix/basename)
- Implement FileStabilityChecker (configurable delay)
- Implement ArchiveService (timestamped folders)
- Integration with RunModeController

### Phase 5: Polish
- Documentation in /docs/features/
- Performance optimization
- Edge case handling
- Final testing

## Verification Plan

### Automated Tests

**Unit Tests** (existing Gradle test task):
```powershell
.\gradlew test
```

Test cases to implement:
- `MultiFileMergeServiceTest`: 2-file merge, 10-file merge, mixed mapping
- `FileMatchingServiceTest`: Prefix matching, exact basename, edge cases
- `FileStabilityCheckerTest`: File locked, file complete, timeout

### Manual Verification

1. **Multi-File Merge Test**:
   - Load 2 TXT files in the app
   - Enter Teach Mode
   - Create mapping: File1 Col A → Output A1, File2 Col B → Output B1
   - Export to Excel
   - Verify both files' data appears in correct cells

2. **Folder Watching Test**:
   - Configure 2-folder watching in Run Mode
   - Place file `PART001_001.txt` in Folder1
   - Verify no processing occurs (waiting for Folder2)
   - Place file `PART001_002.txt` in Folder2
   - Verify auto-processing triggers and output created
   - Verify archive folder contains both input files + output

## Complexity Tracking

No constitution violations. All gates passed.

## Dependencies

```text
Phase 1 (Setup) → Phase 2 (Core Merge) → Phase 3 (Teaching) → Phase 4 (Watching) → Phase 5 (Polish)
```

All phases are sequential as each builds on the previous.
