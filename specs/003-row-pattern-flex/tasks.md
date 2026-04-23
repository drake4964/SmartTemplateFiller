---
description: "Task list for Row Pattern Flex implementation"
---

# Tasks: Row Pattern Flex

**Input**: Design documents from `/specs/003-row-pattern-flex/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)

## Path Conventions

- Paths shown below assume single project repository matching `plan.md`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure validations

- [x] T001 Verify active Java/JavaFX setup and access to Apache POI/Jackson dependencies from `build.gradle`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure and models that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T002 Update `ColumnMapping` model in `src/main/java/com/smarttemplatefiller/model/mapping/ColumnMapping.java` to add `startField`, `fillField`, and `spaceField`.
- [x] T003 Create `RowPatternDescriptor` value object in `src/main/java/com/smarttemplatefiller/model/mapping/RowPatternDescriptor.java`.
- [x] T004 Create `MappingPathResolver` utility in `src/main/java/com/smarttemplatefiller/engine/MappingPathResolver.java`.

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Configure Fill-and-Space Row Pattern (Priority: P1) 🎯 MVP

**Goal**: A user can set Start, Fill, and Space fields in the Teach Mode UI to export repeating block layouts automatically.

**Independent Test**: Load a source file, configure a mapping with fill=3 and space=1, export, and verify the Excel cells alternate 3 filled / 1 skipped.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T005 [P] [US1] Create logic tests for pattern generation in `tests/test/java/com/smarttemplatefiller/model/mapping/RowPatternDescriptorTest.java`.
- [x] T006 [P] [US1] Create path resolution tests in `tests/test/java/com/smarttemplatefiller/engine/MappingPathResolverTest.java`.

### Implementation for User Story 1

- [x] T007 [US1] Implement pattern generation sequence logic in `RowPatternDescriptor.java`.
- [x] T008 [US1] Implement legacy vs flex decision logic in `MappingPathResolver.java`.
- [x] T009 [US1] Replace odd/even/all buttons with Start/Fill/Space numeric fields in `src/main/resources/views/FlexPatternPanel.fxml` (or equivalent legacy FXML).
- [x] T010 [US1] Implement UI controller for these inputs inside `src/main/java/com/smarttemplatefiller/ui/components/FlexPatternPanel.java`.
- [x] T011 [US1] Implement inline validation (red border, tooltip, disable Save) for <1 limits on Fill/Start and <0 on Space Field within `FlexPatternPanel.java`.
- [x] T012 [US1] Implement debounced (< 200ms) Live Preview logic rendering the first 10 output cell addresses within `FlexPatternPanel.java`, including reactivity to Start Cell and Direction changes. 
- [x] T013 [US1] Update `src/main/java/com/smarttemplatefiller/ui/controllers/TeachModeController.java` to handle Flex mode, including hiding/showing fields naturally on mode-switch while preserving their state.

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Use Start Field to Offset the Reading Position (Priority: P2)

**Goal**: Using the Start Field allows the user to skip preamble headers in source files.

**Independent Test**: Set Start Field to 4 and verify the first exported Excel cell contains source row 4 instead of 1.

### Tests for User Story 2 ⚠️

- [x] T014 [P] [US2] Add test cases validating Start offsets and exhaustion (FR-010) to `RowPatternDescriptorTest.java`.

### Implementation for User Story 2

- [x] T015 [US2] Update the actual export engine (e.g. `src/main/java/com/smarttemplatefiller/engine/ExportEngine.java` or `TxtParser.java`) to respect the pattern sequence and offset given by `RowPatternDescriptor`.

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Persist and Reload Pattern Settings in JSON Mapping (Priority: P3)

**Goal**: Save and restore the flex fields in mapping JSON, seamlessly falling back to legacy paths when loading older configurations.

**Independent Test**: Save a flex mapping, close app, reopen, load mapping -> Start/Fill/Space must match saved state without user re-entering.

### Tests for User Story 3 ⚠️

- [x] T016 [P] [US3] Add JSON Serialization/Deserialization tests in `tests/test/java/com/smarttemplatefiller/model/mapping/ColumnMappingTest.java`.

### Implementation for User Story 3

- [x] T017 [US3] Ensure mapping save/load mechanisms properly fetch/push values to the `FlexPatternPanel.java`.
- [x] T018 [US3] Verify `RunningModeController` successfully reads and exports these JSONs headlessly using `MappingPathResolver` without UI overrides.

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories + Constitution compliance

### Constitution Compliance Tasks

- [x] T019 [P] **[Principle I]** Verify < 200ms immediate preview latency.
- [x] T020 [P] **[Principle II]** Verify `RowPatternDescriptor` is fully standalone with no JavaFX dependencies.
- [x] T021 [P] **[Principle III]** Verify older legacy mappings are perfectly reverse-compatible via `MappingPathResolver`.
- [x] T022 **[Principle IV]** Run test coverage report over `RowPatternDescriptor` and `MappingPathResolver` to ensure ≥80% coverage.
- [x] T023 [P] **[Principle VII]** Verify Start/Fill field malformed inputs (e.g., letters) are rejected gracefully.

### General Polish

- [x] T024 [P] End-user feature documentation written to `docs/features/row-pattern-flex.md`.
- [x] T025 Execute steps outlined in `quickstart.md` manually to confirm end-to-end user satisfaction.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
- **Polish (Final Phase)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2).
- **User Story 2 (P2)**: Integrates with core export engine; depends roughly on US1 models.
- **User Story 3 (P3)**: Depends on US1 UI changes returning proper DTO data.

### Parallel Opportunities

- All tests for a user story marked [P] can run in parallel
- Different user stories can be parallelized logically if different developers own UI vs Export Engine logic.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently in UI.
5. Deploy/preview if ready.

### Incremental Delivery

1. Foundation ready.
2. US1 -> UI logic and preview working.
3. US2 -> Export Engine applies the logic reliably.
4. US3 -> Saving and loading persist the configuration seamlessly.
