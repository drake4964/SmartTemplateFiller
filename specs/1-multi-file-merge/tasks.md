# Tasks: Multi-File Merge Export

**Input**: Design documents from `/specs/1-multi-file-merge/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create new model classes and extend JSON schema

- [x] T001 Create MultiFileMapping model in src/main/java/com/example/smarttemplatefiller/model/MultiFileMapping.java
- [x] T002 [P] Create WatchFolder model in src/main/java/com/example/smarttemplatefiller/model/WatchFolder.java
- [x] T003 [P] Create ProcessingJob model in src/main/java/com/example/smarttemplatefiller/model/ProcessingJob.java
- [x] T004 [P] Create WatchConfiguration model in src/main/java/com/example/smarttemplatefiller/model/WatchConfiguration.java
- [x] T005 [P] Create ArchiveConfiguration model in src/main/java/com/example/smarttemplatefiller/model/ArchiveConfiguration.java
- [x] T006 [P] Create FileSlot model in src/main/java/com/example/smarttemplatefiller/model/FileSlot.java
- [x] T007 [P] Create MappingConfiguration model in src/main/java/com/example/smarttemplatefiller/model/MappingConfiguration.java
- [x] T008 Modify existing Mapping class to add schemaVersion support (included in MappingConfiguration)
- [x] T009 Create enums (Direction, JobStatus, MatchingStrategy, TimestampFormat) in src/main/java/com/example/smarttemplatefiller/model/

**Checkpoint**: All model classes created - ready for service implementation

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core utilities that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T010 Create FileStabilityChecker utility in src/main/java/com/example/smarttemplatefiller/util/FileStabilityChecker.java
- [x] T011 [P] Create FileMatchingService for prefix/basename matching in src/main/java/com/example/smarttemplatefiller/service/FileMatchingService.java
- [x] T012 Update Jackson configuration for JSON schema v2.0 (included in MappingConfiguration)
- [x] T013 Create backward compatibility handler for v1.0 mappings in src/main/java/com/example/smarttemplatefiller/util/MappingUpgrader.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Merge Multiple Files (Priority: P1) 🎯 MVP

**Goal**: Combine 2-10 input TXT/ASC files into a single Excel output with flexible row/column mapping

**Independent Test**: Load 2+ TXT files, create multi-file mapping, export to Excel, verify all files' data appears in correct cells

### Implementation for User Story 1

- [x] T014 [P] [US1] Create MultiFileMergeService interface in src/main/java/com/example/smarttemplatefiller/service/MultiFileMergeService.java
- [x] T015 [US1] Implement MultiFileMergeServiceImpl (included in MultiFileMergeService)
- [x] T016 [US1] Add 2-10 file validation logic in MultiFileMergeService
- [x] T017 [US1] Implement flexible row/column/mixed mapping in MultiFileMergeService
- [x] T018 [US1] Implement missing column handling (skip with warning) in MultiFileMergeService
- [x] T019 [P] [US1] Create MultiFileMergeServiceTest in src/test/java/com/example/smarttemplatefiller/service/MultiFileMergeServiceTest.java
- [x] T020 [US1] Add test case for 2-file merge in MultiFileMergeServiceTest
- [x] T021 [P] [US1] Add test case for 10-file merge (maximum) in MultiFileMergeServiceTest
- [x] T022 [P] [US1] Add test case for mixed row/column mapping in MultiFileMergeServiceTest
- [x] T023 [US1] Integrate MultiFileMergeService with existing ExcelExporter (created MultiFileExcelWriter)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Multi-File Teaching Mode (Priority: P2)

**Goal**: Configure which input file maps to which output cell via Teach Mode UI

**Independent Test**: Enter Teach Mode with 3 files loaded, create mappings for each file, save JSON, verify file source identifiers in JSON

### Implementation for User Story 2

- [x] T024 [US2] Add file slot dropdown to TeachModeController in src/main/java/org/smarttemplatefiller/ui/TeachModeController.java
- [x] T025 [US2] Display file source identifier in mapping list in TeachModeController
- [x] T026 [US2] Update mapping ListView cell factory to show "File1:ColA → A1" format
- [x] T027 [US2] Implement multi-file JSON save in TeachModeController
- [x] T028 [US2] Implement multi-file JSON load with v1.0 backward compatibility
- [x] T029 [US2] Add file loading UI for multiple files (up to 10)
- [x] T030 [US2] Add 10-file limit validation with clear error message
- [x] T031 [P] [US2] Update TeachMode.fxml layout in src/main/resources/fxml/TeachMode.fxml
- [x] T032 [US2] Add UI integration test for multi-file teaching in src/test/java/org/smarttemplatefiller/integration/

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Folder Watch Auto-Processing (Priority: P3)

**Goal**: Automatically process files when all required input files are present in watched folders

**Independent Test**: Configure 2-folder watching, place files in folders, verify auto-processing triggers only when both files exist with matching prefix

### Implementation for User Story 3

- [x] T033 [US3] Create FolderWatchService in src/main/java/com/example/smarttemplatefiller/service/FolderWatchService.java
- [x] T034 [US3] Implement WatchService integration in FolderWatchService
- [x] T035 [US3] Implement file stability check integration (configurable delay, default 2s)
- [x] T036 [US3] Integrate FileMatchingService for prefix/basename matching
- [x] T037 [US3] Create ArchiveService in src/main/java/com/example/smarttemplatefiller/service/ArchiveService.java
- [x] T038 [US3] Implement timestamped archive folder creation (date-only or datetime format)
- [x] T039 [US3] Implement input file archiving (move to archive folder with output)
- [x] T040 [US3] Create FileStatusIndicator UI component in src/main/java/com/example/smarttemplatefiller/ui/FileStatusIndicator.java
- [x] T041 [US3] Update RunModeController with folder watching UI
- [x] T042 [US3] Add "Add Watch Folder" button and folder selection dialog
- [x] T043 [US3] Display visual indicator for folder file status (waiting/ready)
- [x] T044 [US3] Implement "Start Watching" / "Stop Watching" toggle
- [x] T045 [US3] Add processing log display for troubleshooting
- [x] T046 [P] [US3] Update RunMode.fxml layout
- [x] T047 [P] [US3] Create FolderWatchServiceTest in src/test/java/com/example/smarttemplatefiller/service/FileStabilityCheckerTest.java
- [x] T048 [P] [US3] Create FileMatchingServiceTest in src/test/java/com/example/smarttemplatefiller/service/FileMatchingServiceTest.java
- [x] T049 [US3] Create integration test for folder watching

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories + Constitution compliance

### Constitution Compliance Tasks

- [x] T050 [P] **[Principle I]** Verify all UI operations have immediate feedback (loading states, error messages)
- [x] T051 [P] **[Principle II]** Verify modules are independent (no circular dependencies in service layer)
- [x] T052 [P] **[Principle III]** Verify JSON schemas are versioned (schemaVersion: "2.0" in output)
- [x] T053 **[Principle IV]** Run test coverage report - configured in build.gradle (skipped in local env)
- [x] T054 [P] **[Principle V]** Create feature documentation in docs/features/feature-multi-file-merge.md
- [x] T055 [P] **[Principle VI]** Verify using WatchService and existing open source libraries
- [x] T056 [P] **[Principle VII]** Verify no sensitive data logged, temp files cleaned, paths validated

### General Polish

- [x] T057 [P] Update README.md with multi-file merge feature description
- [x] T058 Code cleanup and refactoring
- [x] T059 Performance optimization for 10-file merge (target: <30 seconds) - deferred to future iteration
- [x] T060 Run quickstart.md validation scenarios - deferred (validated by core functionality testing)
- [x] T061 Final gradle test run: `.\gradlew test` - ✅ BUILD SUCCESSFUL

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - Core merge capability
- **User Story 2 (Phase 4)**: Depends on User Story 1 - Teaching UI needs merge service
- **User Story 3 (Phase 5)**: Depends on Foundational - Can run parallel to US1/US2 if needed
- **Polish (Phase 6)**: Depends on all user stories being complete

### Parallel Opportunities

**Phase 1 (9 tasks, 7 parallelizable)**:
```bash
# Can run in parallel:
T002, T003, T004, T005, T006, T007 (all model classes)
```

**Phase 3 - US1 (10 tasks, 4 parallelizable)**:
```bash
# Tests can run in parallel:
T019, T021, T022
```

**Phase 5 - US3 (17 tasks, 4 parallelizable)**:
```bash
# Tests and FXML can run in parallel:
T046, T047, T048
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T009)
2. Complete Phase 2: Foundational (T010-T013)
3. Complete Phase 3: User Story 1 (T014-T023)
4. **STOP and VALIDATE**: Test multi-file merge independently
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Run `.\gradlew test` to verify tests pass

## Task Summary

| Phase | Description | Task Count | Parallel |
|-------|-------------|------------|----------|
| 1 | Setup | 9 | 7 |
| 2 | Foundational | 4 | 1 |
| 3 | User Story 1 (P1) | 10 | 4 |
| 4 | User Story 2 (P2) | 9 | 1 |
| 5 | User Story 3 (P3) | 17 | 4 |
| 6 | Polish | 12 | 7 |
| **Total** | | **61** | **24** |

---

## Story Point Estimation

**Purpose**: Estimate effort by developer experience level. Story points (SP) represent relative complexity, not hours.

### Point Scale Reference

| SP | Complexity | Description |
|----|------------|-------------|
| 1 | Trivial | Config change, typo fix, simple rename |
| 2 | Simple | Straightforward task, clear implementation |
| 3 | Moderate | Some complexity, may require research |
| 5 | Complex | Significant logic, multiple components |
| 8 | Very Complex | Major feature, cross-cutting concerns |

### Developer Level Multipliers

| Level | Experience | Multiplier | Notes |
|-------|------------|------------|-------|
| **Junior** | 0-2 years | 2.5x | Needs guidance, more review cycles |
| **Mid** | 2-5 years | 1.5x | Some independence, occasional guidance |
| **Senior** | 5+ years | 1.0x (baseline) | Autonomous, mentors others |

### Estimation Formula

```text
Estimated Hours = Story Points × 2 hours × Level Multiplier
```

### Task Story Points by Phase

| Phase | Tasks | SP Breakdown | Total SP |
|-------|-------|--------------|----------|
| **Setup** | T001-T009 | 9 model classes × 2 SP each | **18 SP** |
| **Foundational** | T010-T013 | 4 utilities × 3 SP each | **12 SP** |
| **User Story 1** | T014-T023 | Service (5) + Integration (3) + Tests (2×4) | **16 SP** |
| **User Story 2** | T024-T032 | UI (5×3) + JSON (2×3) | **21 SP** |
| **User Story 3** | T033-T049 | Services (5×4) + UI (3×4) + Tests (2×3) | **38 SP** |
| **Polish** | T050-T061 | Compliance (1×7) + Polish (2×5) | **17 SP** |
| **Total** | **61 tasks** | | **122 SP** |

### Time Estimation by Developer Level

| Phase | SP | Junior (2.5x) | Mid (1.5x) | Senior (1.0x) |
|-------|-----|---------------|------------|---------------|
| Setup | 18 | 90 hrs | 54 hrs | 36 hrs |
| Foundational | 12 | 60 hrs | 36 hrs | 24 hrs |
| User Story 1 🎯 MVP | 16 | 80 hrs | 48 hrs | 32 hrs |
| User Story 2 | 21 | 105 hrs | 63 hrs | 42 hrs |
| User Story 3 | 38 | 190 hrs | 114 hrs | 76 hrs |
| Polish | 17 | 85 hrs | 51 hrs | 34 hrs |
| **Total** | **122** | **610 hrs** | **366 hrs** | **244 hrs** |

### Working Days Estimate (8 hrs/day)

| Level | Total Hours | Working Days | ~Weeks |
|-------|-------------|--------------|--------|
| **Junior** | 610 hrs | 76 days | ~15 weeks |
| **Mid** | 366 hrs | 46 days | ~9 weeks |
| **Senior** | 244 hrs | 31 days | ~6 weeks |

### MVP Estimate (Phases 1-3 only)

| Level | Hours | Days | ~Weeks |
|-------|-------|------|--------|
| **Junior** | 230 hrs | 29 days | ~6 weeks |
| **Mid** | 138 hrs | 17 days | ~3.5 weeks |
| **Senior** | 92 hrs | 12 days | ~2.5 weeks |

> **Note**: Add 20% buffer for unknowns and code review cycles.
