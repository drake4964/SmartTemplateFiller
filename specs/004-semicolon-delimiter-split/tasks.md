# Tasks: Semicolon Delimiter Column Split (Simplified)

**Input**: Design documents from `/specs/004-semicolon-delimiter-split/`
**Prerequisites**: plan.md (required), spec.md (required)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. US1, US2, US3)
- Exact file paths are specified in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project cleanup and synchronization

- [x] T001 Clean up unused custom Semicolon packages and models from previous implementation in src/main/java/com/example/smarttemplatefiller/semicolon/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core model extensions that all user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T002 Update standard ColumnMapping class in src/main/java/com/example/smarttemplatefiller/mapping/ColumnMapping.java to add a standard Boolean `fixed` field with getter and setter
- [x] T003 Implement `parseSemicolonTable(File file)` method inside src/main/java/com/example/smarttemplatefiller/TxtParser.java that splits lines on `;` and handles non-`;` lines as single columns

---

## Phase 3: User Story 1 - Automatic Semicolon Split on Load (Priority: P1) 🎯 MVP

**Goal**: Automatically split and load semicolon-delimited files as standard columns on load

**Independent Test**: Load a semicolon-delimited text file. Verify the main screen's TableView automatically shows split columns without errors.

### Implementation for User Story 1

- [x] T004 [US1] Add auto-detection logic to `TxtParser.parseFile()` in src/main/java/com/example/smarttemplatefiller/TxtParser.java to parse files with semicolons using `parseSemicolonTable`
- [x] T005 [US1] Verify main screen loader updates `tableView` and properly displays split columns in src/main/java/com/example/smarttemplatefiller/MainController.java

---

## Phase 4: User Story 2 - Standard Teach Mode Mapping with Fixed Checkbox (Priority: P2)

**Goal**: Simplify Teach Mode UI by removing semicolon-specific panes and adding a "Fixed" checkbox to the standard mapping panel

**Independent Test**: Enter Teach Mode and verify the interface is the standard column mapping panel with a "Fixed" checkbox, and all old semicolon preview panes are gone.

### Implementation for User Story 2

- [x] T006 [P] [US2] Remove `semicolonModeToggle` and `semicolonPreviewPane` from FXML layout in src/main/resources/fxml/teach_mode.fxml
- [x] T007 [P] [US2] Add the "Fixed mapping" checkbox `semicolonFixedCheckbox` to the standard column mapping section in src/main/resources/fxml/teach_mode.fxml
- [x] T008 [US2] Remove Semicolon-specific preview lists, selection listeners, and toggle handlers in src/main/java/com/example/smarttemplatefiller/TeachModeController.java
- [x] T009 [US2] Bind the standard "Fixed" checkbox to `colMappings` configuration in src/main/java/com/example/smarttemplatefiller/TeachModeController.java
- [x] T010 [US2] Update `handleAddMapping()` in src/main/java/com/example/smarttemplatefiller/TeachModeController.java to include the `fixed` flag in the standard mapping JSON serialization

---

## Phase 5: User Story 3 - Export with Cavity-Block Offsets (Priority: P2)

**Goal**: Automatically partition semicolon files by `@101` boundaries and apply column-shifting to data mappings during Excel export/append

**Independent Test**: Export a multi-cavity semicolon file. Verify fixed labels are written once, and data values shift column-by-column across cavities.

### Implementation for User Story 3

- [x] T011 [US3] Update `writeAdvancedMappedFile` in src/main/java/com/example/smarttemplatefiller/ExcelWriter.java to detect semicolon-delimited files, partition by `@101`, group mappings by row to calculate group width, and apply column offsets
- [x] T012 [US3] Update `appendToMappedFile` in src/main/java/com/example/smarttemplatefiller/ExcelWriter.java to apply identical cavity-shifting logic for append operations

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verify backward compatibility and Constitution compliance

### Constitution Compliance Tasks

- [x] T013 [P] Verify that loading legacy mappings without `fixed` property works without any regressions
- [x] T014 Run JUnit tests to ensure all existing and new tests pass using `./gradlew.bat test`
- [x] T015 Verify modularity of TxtParser and ExcelWriter (no circular dependencies)
- [x] T016 Update feature documentation to reflect simplified load-time split in docs/features/semicolon-delimiter-split.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup completion.
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion.
- **User Story 2 (Phase 4)**: Depends on Phase 3 completion.
- **User Story 3 (Phase 5)**: Depends on Phase 4 completion.
- **Polish (Phase 6)**: Depends on all user stories being complete.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup and Foundational tasks.
2. Implement and verify User Story 1 (auto-split on load).
3. Verify that standard files are still parsed correctly (no regressions).
