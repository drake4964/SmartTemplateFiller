# Tasks: Excel Append Mode

**Input**: Design documents from `/specs/002-excel-append-mode/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Test tasks are included per spec requirement (IV. Quality Testing ≥80% coverage target).

**Organization**: Tasks are grouped by user story with sub-phases to reduce AI confusion during implementation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project structure for append feature - minimal changes needed since extending existing codebase

- [x] T001 Verify branch `002-excel-append-mode` is checked out and up-to-date with main
- [x] T002 Review existing `ExcelWriter.java` and `FolderWatcher.java` for integration points

**Checkpoint**: Setup complete

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core classes that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 [P] Create `AppendResult.java` data class in `src/main/java/com/example/smarttemplatefiller/AppendResult.java`
  - Fields: `success` (boolean), `rowsAdded` (int), `rowOffset` (int), `targetFilePath` (String), `warnings` (List<String>), `errorMessage` (String)
  - Add constructor, getters, and static factory methods for success/failure

- [x] T004 [P] Create `ExportConfiguration.java` POJO in `src/main/java/com/example/smarttemplatefiller/ExportConfiguration.java`
  - Fields: `appendMode` (boolean), `appendTargetPath` (String)
  - Add getters/setters

- [x] T005 Add `calculateRowOffset()` helper method to `ExcelWriter.java` in `src/main/java/com/example/smarttemplatefiller/ExcelWriter.java`
  - Signature: `private static int calculateRowOffset(Sheet sheet)`
  - Returns `sheet.getLastRowNum() + 1` (or 0 if empty)

- [x] T006 Add `appendToMappedFile()` method to `ExcelWriter.java` in `src/main/java/com/example/smarttemplatefiller/ExcelWriter.java`
  - Signature: `public static AppendResult appendToMappedFile(File txtFile, File mappingFile, File existingExcelFile)`
  - Implementation: Open existing file, calculate offset, apply mappings with offset, write back

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3A: User Story 1 - Tests (Priority: P1) 🎯 MVP

**Purpose**: Write failing tests FIRST for append logic

**Files**: `ExcelWriterAppendTest.java` only

- [x] T007 [P] [US1] Create `ExcelWriterAppendTest.java` test class in `src/test/java/com/example/smarttemplatefiller/ExcelWriterAppendTest.java`
  - Test: `testAppendToEmptyFile()` - offset should be 0
  - Test: `testAppendToFileWithData()` - offset calculated correctly (e.g., 3 rows → offset 3)
  - Test: `testAppendPreservesExistingData()` - existing rows unchanged after append
  - Test: `testCalculateRowOffsetEdgeCases()` - gaps, empty rows
  - Test: `testAppendDoesNotDuplicateHeaders()` - verify headers not duplicated when appending

- [x] T008 [P] [US1] Add locked file test to `ExcelWriterAppendTest.java`
  - Test: `testAppendWithLockedFile()` - appropriate error returned in AppendResult

**Checkpoint**: US1 tests written and failing

---

## Phase 3B: User Story 1 - Core Implementation (Priority: P1)

**Purpose**: Implement append UI flow in MainController

**Files**: `MainController.java` only

- [x] T009 [US1] Modify `handleExportToExcel()` in `MainController.java` in `src/main/java/com/example/smarttemplatefiller/MainController.java`
  - After selecting mapping file, show dialog with mode choice:
    - Radio button: "Create New File" (default)
    - Radio button: "Append to Existing File"
  - If append selected, open file browser for existing .xlsx file

- [x] T010 [US1] Add append branch logic in `handleExportToExcel()` in `MainController.java`
  - If append mode: Call `ExcelWriter.appendToMappedFile()`
  - Show result dialog with rows added and offset applied
  - Handle errors with clear user message

**Checkpoint**: Basic append flow works

---

## Phase 3C: User Story 1 - Error Handling & Polish (Priority: P1)

**Purpose**: Add error handling and logging

**Files**: `MainController.java`, `ExcelWriter.java`

- [x] T011 [US1] Add file locked error handling in `MainController.java`
  - Catch IOException for locked files
  - Display: "Cannot access file: It may be open in another application. Please close the file and try again."

- [x] T011B [US1] Add corrupted file fallback handling in `MainController.java`
  - Detect corrupted/unreadable Excel file in append mode
  - Show dialog: "File appears corrupted. Would you like to create a new file instead?"
  - Provide Yes (create new) / No (cancel) options

- [x] T011C [US1] Add row limit warning in `ExcelWriter.appendToMappedFile()`
  - Check if resulting row count approaches Excel limit (1,048,576)
  - Warn user when exceeding 1,000,000 rows or within 5% of limit
  - Add warning to AppendResult.warnings list

- [x] T012 [US1] Log append operations in `ExcelWriter.appendToMappedFile()`
  - Log: source file name, target file name, rows added, offset applied

**Checkpoint**: User Story 1 complete - manual export with append option works ✅

---

## Phase 4A: User Story 2 - Tests (Priority: P2)

**Purpose**: Write failing integration tests for run mode append

**Files**: `AppendIntegrationTest.java` only

- [x] T013 [P] [US2] Create `AppendIntegrationTest.java` test class in `src/test/java/com/example/smarttemplatefiller/AppendIntegrationTest.java`
  - Test: `testRunModeAppendMultipleFiles()` - 3 sequential files → single Excel with 3× data

- [x] T014 [P] [US2] Add file deleted edge case test to `AppendIntegrationTest.java`
  - Test: `testRunModeAppendWhenFileDeleted()` - creates new file and warns

**Checkpoint**: US2 tests written and failing

---

## Phase 4B: User Story 2 - Config & UI (Priority: P2)

**Purpose**: Add append toggle to run mode config and UI

**Files**: `RunningModeConfig.java`, `running_mode.fxml`, `RunningModeController.java`

- [x] T015 [US2] Extend `RunningModeConfig.java` in `src/main/java/com/example/smarttemplatefiller/RunningModeConfig.java`
  - Add field: `private boolean appendModeEnabled = false;`
  - Add field: `@JsonIgnore private transient String lastGeneratedFilePath;`
  - Add getters/setters for both fields

- [x] T016 [US2] Add CheckBox to `running_mode.fxml` in `src/main/resources/fxml/running_mode.fxml`
  - Add after File Pattern row: `<CheckBox fx:id="appendModeCheckBox" text="Append Mode (accumulate to single file)"/>`

- [x] T017 [US2] Bind CheckBox in `RunningModeController.java` in `src/main/java/com/example/smarttemplatefiller/RunningModeController.java`
  - Add FXML field: `@FXML private CheckBox appendModeCheckBox;`
  - Update `loadConfigToUI()`: set checkbox from config
  - Update `saveConfig()`: save checkbox to config

**Checkpoint**: Run mode append toggle visible in UI

---

## Phase 4C: User Story 2 - FolderWatcher Logic (Priority: P2)

**Purpose**: Implement append logic in FolderWatcher

**Files**: `FolderWatcher.java`, `RunningModeController.java`

- [ ] T018 [US2] Extend `FolderWatcher.java` constructor in `src/main/java/com/example/smarttemplatefiller/FolderWatcher.java`
  - Add field: `private boolean appendModeEnabled;`
  - Add field: `private String lastGeneratedFilePath;`
  - Read `appendModeEnabled` from `RunningModeConfig` in constructor

- [ ] T019 [US2] Modify `processFile()` in `FolderWatcher.java` for append logic
  - If appendModeEnabled AND lastGeneratedFilePath exists AND file exists:
    - Call `ExcelWriter.appendToMappedFile()` instead of `writeAdvancedMappedFile()`
    - Log: "Appended X rows to {filename}"
  - Else:
    - Create new file as before
    - Store path in `lastGeneratedFilePath`
    - Log: "Created new file: {filename}"

- [ ] T020 [US2] Add file deleted handling in `FolderWatcher.processFile()`
  - Check if `lastGeneratedFilePath` file exists before append
  - If deleted: Create new file, warn in log, update `lastGeneratedFilePath`

- [ ] T021 [US2] Add restart prompt in `RunningModeController.handleStart()`
  - If appendModeEnabled AND lastGeneratedFilePath exists from previous session:
    - Show dialog: "Continue appending to {filename}?" with Yes/No/Cancel
    - Yes: keep lastGeneratedFilePath
    - No: clear lastGeneratedFilePath (start fresh)
    - Cancel: abort start

**Checkpoint**: User Story 2 complete - run mode append works ✅

---

## Phase 5: User Story 3 - Append Mode Configuration Persistence (Priority: P3)

**Purpose**: Remember append mode preference between sessions

**Independent Test**: Enable append in run mode, close app, reopen, verify checkbox is still checked

- [ ] T022 [US3] Verify `appendModeEnabled` persists in `RunningModeConfig.save()` and `load()`
  - Already handled by Jackson serialization since field is not transient
  - Add test to verify persistence

- [ ] T022B [US3] Add export dialog append preference persistence
  - Create or extend user preferences storage for export dialog settings
  - Persist: last selected export mode ("Create New" vs "Append")
  - Restore preference on next export dialog open
  - Store in user config JSON (e.g., `~/.smarttemplatefiller/export_preferences.json`)

- [ ] T023 [P] [US3] Add persistence test to `AppendIntegrationTest.java`
  - Test: `testAppendModePreferencePersistence()` - save config, reload, verify appendModeEnabled preserved
  - Test: `testExportDialogPreferencePersistence()` - export with append, reopen dialog, verify append mode selected

**Checkpoint**: User Story 3 complete - settings persist ✅

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories + Constitution compliance

### Constitution Compliance Tasks

- [ ] T024 [P] **[Principle I]** Verify all append operations show progress feedback (loading states)
- [ ] T025 [P] **[Principle II]** Verify `ExcelWriter.appendToMappedFile()` is self-contained with no circular dependencies
- [ ] T026 [P] **[Principle III]** Verify `RunningModeConfig` JSON schema includes `appendModeEnabled` correctly
- [ ] T027 **[Principle IV]** Run test coverage report - verify ≥80% coverage for append logic: `./gradlew test jacocoTestReport`
- [ ] T028 [P] **[Principle V]** Create user documentation in `docs/features/excel-append-mode.md`
- [ ] T029 [P] **[Principle VI]** Confirm no new libraries added - using existing Apache POI
- [ ] T030 [P] **[Principle VII]** Verify no sensitive data logged in append operations; temp files cleaned

### General Polish

- [ ] T031 [P] Run all existing tests to ensure no regressions: `./gradlew test`
- [ ] T032 Code cleanup and remove any debug statements
- [ ] T033 Run quickstart.md validation - test manual and run mode append workflows

**Checkpoint**: Feature complete and polished ✅

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) ← BLOCKS all user stories
    ↓
┌───────────────────────────────────────────┐
│  Phase 3A → Phase 3B → Phase 3C (US1)    │ ← Complete US1 first
└───────────────────────────────────────────┘
    ↓
┌───────────────────────────────────────────┐
│  Phase 4A → Phase 4B → Phase 4C (US2)    │ ← Depends on US1
└───────────────────────────────────────────┘
    ↓
Phase 5 (US3) ← Can start after Phase 2, but typically after US1
    ↓
Phase 6 (Polish) ← After all user stories
```

### Sub-Phase Dependencies

| Phase | Depends On | Can Parallelize |
|-------|------------|-----------------|
| 3A (US1 Tests) | Phase 2 | T007, T008 parallel |
| 3B (US1 Core) | Phase 3A | Sequential |
| 3C (US1 Error) | Phase 3B | Sequential |
| 4A (US2 Tests) | Phase 3C | T013, T014 parallel |
| 4B (US2 Config) | Phase 4A | Sequential |
| 4C (US2 Watcher) | Phase 4B | Sequential |
| 5 (US3) | Phase 2 (min) | Sequential |
| 6 (Polish) | Phase 5 | Most [P] parallel |

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (T003-T006)
3. Complete Phase 3A: US1 Tests (T007-T008)
4. Complete Phase 3B: US1 Core (T009-T010)
5. Complete Phase 3C: US1 Error Handling (T011-T012)
6. **STOP and VALIDATE**: Test manual export append independently
7. Deploy/demo if ready - users can manually append to Excel files

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Phase 3A/3B/3C → **MVP available!**
3. Phase 4A/4B/4C → Run mode append available
4. Phase 5 → Preferences persist
5. Phase 6 → Polished release

### Verification Commands

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

**Run Application**:
```powershell
./gradlew run
```

---

## Summary

| Phase | Tasks | Files | Purpose |
|-------|-------|-------|---------|
| 1 Setup | 2 | - | Branch prep |
| 2 Foundational | 4 | 2 | Core classes |
| 3A US1 Tests | 2 | 1 | Test first |
| 3B US1 Core | 2 | 1 | Main flow |
| 3C US1 Error | 4 | 2 | Edge cases |
| 4A US2 Tests | 2 | 1 | Test first |
| 4B US2 Config | 3 | 3 | Config/UI |
| 4C US2 Watcher | 4 | 2 | Logic |
| 5 US3 | 3 | 2 | Persistence |
| 6 Polish | 10 | Various | Finalize |
| **Total** | **36** | | |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each sub-phase focuses on specific files to reduce context switching
- Verify tests fail before implementing
- Commit after each sub-phase
- Stop at any checkpoint to validate independently
