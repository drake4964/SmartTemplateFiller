# Feature Specification: Row Pattern Flex

**Feature Branch**: `003-row-pattern-flex`
**Created**: 2026-04-01
**Status**: Draft
**Input**: User description: "Enhance Teach Mode Row Pattern and manual row controls with start field, fill field,
and space field for flexible source-to-Excel cell mapping."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Configure Fill-and-Space Row Pattern (Priority: P1)

A user is mapping a source column to an Excel template that uses a repeating block
structure: values appear in groups of N consecutive rows, separated by a fixed number
of blank rows. The user sets **Start Field**, **Fill Field**, and **Space Field**
in the Teach Mode mapping panel to describe this layout precisely.

After confirming the mapping, the exported Excel file reflects the pattern automatically —
no manual row-index entry required.

**Why this priority**: This is the core capability. Without it the feature delivers no value.

**Independent Test**: Load a source file, configure a mapping with fill=3 and space=1,
export, and verify the resulting Excel cell range alternates 3 filled / 1 skipped groups.

**Acceptance Scenarios**:

1. **Given** a source column with rows `[9, '', 10, 9, '', 11, …]` and settings
   Start=1 / Fill=3 / Space=1 / Direction=Vertical / Excel cell=A2,
   **When** the user exports,
   **Then** A2=9, A3='', A4=10, A5=9, A6='', A7=11 (three filled rows, one skipped, repeat).

2. **Given** Start=1 / Fill=1 / Space=1 (reads every alternate source row),
   **When** the user exports,
   **Then** A2=9, A3=10, A4=9, A5=11, A6=12, A7=11 (equivalent to current "odd" pattern).

3. **Given** Start=1 / Fill=1 / Space=0 (no source rows skipped),
   **When** the user exports,
   **Then** A2=9, A3='', A4=10, A5='', A6=9, A7='' (equivalent to current "all" pattern).

4. **Given** Start=4 / Fill=1 / Space=1 (begins reading at source row 4, which is empty),
   **When** the user exports,
   **Then** all exported cells are '' because source rows 4, 6, 8… are empty.

---

### User Story 2 — Use Start Field to Offset the Reading Position (Priority: P2)

A user's source file has a header or preamble occupying the first few source rows.
The meaningful data begins at row N. The user specifies **Start Field = N** so that
the pattern begins from that source row and the earlier rows are ignored.

**Why this priority**: Start Field enables all flexible offset cases; it completes the
practical utility of the feature without requiring any other change.

**Independent Test**: Set Start Field to 4 and verify the first exported cell contains
the value from source row 4, not source row 1.

**Acceptance Scenarios**:

1. **Given** Start=3 / Fill=1 / Space=0,
   **When** the user exports,
   **Then** the first exported cell contains the value of source row 3, and subsequent
   cells contain source rows 4, 5, 6… in order.

2. **Given** Start=1 (default),
   **When** the user exports,
   **Then** behaviour is identical to the existing row-pattern behaviour (backward compatible).

---

### User Story 3 — Persist and Reload Pattern Settings in JSON Mapping (Priority: P3)

When a user saves their mapping configuration, the Start Field, Fill Field, and
Space Field values are persisted in the mapping JSON. When the mapping is reloaded,
all three values are restored exactly, so the user does not have to re-enter them.

**Why this priority**: Without persistence, the feature loses its value in Running
Mode (folder-watch), where mappings must be reusable across sessions.

**Independent Test**: Save a mapping with Fill=3, Space=1, Start=2. Close and reopen
the application. Load the same mapping file. Verify the UI shows Fill=3, Space=1, Start=2.

**Acceptance Scenarios**:

1. **Given** a mapping saved with custom Start/Fill/Space values,
   **When** the mapping file is loaded in a new session,
   **Then** those three values are shown in the UI without the user re-entering them.

2. **Given** a legacy mapping JSON that has no Start/Fill/Space fields,
   **When** it is loaded,
   **Then** defaults (Start=1, Fill=1, Space=0) are applied and the export behaves
   like the existing "all" pattern (backward compatible).

---

### Edge Cases

- What happens when **Fill Field = 0**? System MUST prevent saving such a mapping
  (zero fill produces no output and is logically invalid).
- What happens when **Start Field** exceeds the total number of source rows?
  All exported cells MUST be empty / blank; no error is thrown (FR-010).
- What happens when **Fill Field** is larger than the remaining source rows in a group?
  The export writes what is available and leaves remaining destination cells empty;
  no error is thrown. Neither Start nor Fill have an upper bound — they are naturally
  bounded by source data at export time.
- What happens when **Space Field = 0 and Fill Field = 1**?
  Every source row is written consecutively — identical to the existing "all" pattern.
- What happens when source data is shorter than one fill group?
  Remaining destination cells MUST be left empty; export MUST NOT crash.
- What happens when the user switches between "Pattern" mode and "Manual Rows" mode?
  The Start / Fill / Space fields are visible **only** in Pattern mode (replacing the former
  odd/even/all buttons). Manual Rows mode is unaffected and retains its own controls.
  Previously entered Start / Fill / Space values MUST be preserved when switching modes
  back and forth within the same session (FR-012).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Teach Mode column mapping panel MUST replace the existing **odd / even / all**
  pattern buttons with three numeric input fields: **Start Field** (default 1),
  **Fill Field** (default 1), and **Space Field** (default 0). The "Manual Rows" sub-mode
  remains unchanged and is unaffected by this change.
- **FR-012**: A user switching from Pattern mode to Manual Rows mode MUST see the Start /
  Fill / Space fields hidden; switching back to Pattern mode MUST restore their last-entered
  values.
- **FR-002**: The export algorithm MUST apply the pattern: starting at source row
  `Start Field`, read `Fill Field` consecutive source rows and write them to consecutive
  destination cells, then skip `Space Field` source rows (writing nothing), and repeat
  until source rows are exhausted.
- **FR-003**: **Fill Field** MUST be ≥ 1 with no upper bound enforced by the UI.
  When a value < 1 is entered, the field MUST display a red border with an inline
  tooltip (e.g. "Fill must be at least 1").
- **FR-004**: **Start Field** MUST be ≥ 1 with no upper bound enforced by the UI.
  When a value < 1 is entered, the field MUST display a red border with an inline
  tooltip (e.g. "Start must be at least 1").
  If Start Field exceeds the source row count at export time, FR-010 applies.
- **FR-013**: The mapping Save / Confirm button MUST be disabled whenever any of
  FR-003 or FR-004 validation rules are violated. It MUST re-enable as soon as all
  fields return to valid values.
- **FR-005**: **Space Field** MUST be ≥ 0; a value of 0 means no source rows are skipped.
- **FR-006**: The three new fields MUST be persisted as part of the mapping JSON and
  restored correctly when the mapping is reloaded.
- **FR-007**: Legacy mapping files that contain only `rowIndexes` / `patternType` (no
  `startField`/`fillField`/`spaceField`) MUST load successfully using the existing index-list
  path with defaults (Start=1, Fill=1, Space=0). When all three flex fields are present,
  the flex path is used and `rowIndexes` / `patternType` are ignored.
- **FR-011**: New mappings created with the flex fields MUST NOT write `rowIndexes` or
  `patternType` to the JSON. The export layer MUST detect which path to use by checking
  the presence of `fillField` in the mapping JSON.
- **FR-008**: The feature MUST support both Vertical and Horizontal directions.
  Start / Fill / Space ALWAYS index **source data rows** regardless of direction;
  the direction setting only controls the output axis in Excel
  (Vertical → writes down a column; Horizontal → writes across a row).
- **FR-009**: The UI MUST show a live preview of the **first 10 output cells** based
  on the current Start / Fill / Space values whenever **any** of the following change:
  Start Field, Fill Field, Space Field, or the **target Excel start cell**.
  The preview MUST update within 200 ms of each input change.
  Each preview row MUST display the **target Excel cell address alongside the value**
  (e.g. `A2 → 9`, `A3 → —` for a skipped/empty row, `A4 → 10`).
  - When a source file is loaded: the value portion shows **actual source data**.
  - When no source file is loaded: the value portion shows **positional labels**
    (e.g. `A2 → Row 1`, `A3 → —`) so the pattern structure is still visually clear.
- **FR-010**: When **Start Field** exceeds the source row count, the export MUST
  produce only empty cells without raising an error.
- **FR-014**: Running Mode (folder-watch) MUST apply flex mapping fields automatically
  when processing files. **No UI changes to `RunningModeController` are required**;
  the export engine reads `startField`, `fillField`, and `spaceField` directly from
  the saved mapping JSON.

### Key Entities

- **ColumnMapping**: Extended with `startField` (int ≥ 1), `fillField` (int ≥ 1),
  `spaceField` (int ≥ 0). When these three fields are present they **replace** the
  `rowIndexes` / `patternType` fields at export time; `rowIndexes` is kept only when
  loading legacy files that pre-date this feature.
- **RowPatternDescriptor**: Value object encapsulating startField, fillField, spaceField;
  exposes a method to generate an ordered stream of (sourceIndex, writeToExcel) pairs
  for a given total source row count.
- **MappingPathResolver**: Stateless helper that inspects a loaded ColumnMapping and
  returns whether to use the flex path (fillField present) or the legacy rowIndexes path.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can configure a fill-and-space pattern in Teach Mode in under
  60 seconds without consulting documentation.
- **SC-002**: 100% of the four canonical acceptance scenarios (US1, scenarios 1–4)
  produce the correct Excel cell values on export.
- **SC-003**: Legacy mapping files (without the new fields) open without errors or
  data loss in 100% of cases.
- **SC-004**: The UI live preview shows the correct first 10 output cells within 200 ms
  of the user changing any of Start / Fill / Space values.
- **SC-005**: Running Mode (folder-watch) applies saved flex mappings and produces
  correct export output without any manual reconfiguration or UI changes to Running Mode.

## Constitution Alignment *(mandatory)*

| Principle | Alignment Check | Notes |
|-----------|-----------------|-------|
| I. User Experience First | Live preview gives immediate feedback; error messages block invalid Fill values before save | Preview requirement in FR-009 |
| II. Modular Design | RowPatternDescriptor is a standalone, independently testable value object | Index generation logic decoupled from UI |
| III. Configuration-Driven | New fields persisted in existing JSON mapping format; schema versioned | FR-006, FR-007 |
| IV. Quality Testing | Acceptance scenarios double as unit test cases; edge cases explicitly defined | Four canonical scenarios + 5 edge cases |
| V. Documentation | User-facing docs needed in `/docs/features/`; mapping JSON schema change recorded | Follow-up after implementation |
| VI. Reusable Components & Open Source | RowPatternDescriptor reusable across Teach and Running modes | No new third-party libraries required |
| VII. Security & Data Handling | No sensitive data involved; malformed numeric inputs validated before use | FR-003–FR-005 guard invalid values |

## Clarifications

### Session 2026-04-01

- Q: When the new flex fields are used, what happens to the legacy `rowIndexes` / `patternType` fields in the JSON? → A: New flex fields take precedence; `rowIndexes` is NOT written for new mappings. Legacy files that only have `rowIndexes` continue to work via the existing path. (FR-007, FR-011, MappingPathResolver)
- Q: Should the new Start / Fill / Space fields replace the existing odd/even/all buttons or coexist alongside them? → A: Replace the odd/even/all buttons in Pattern mode entirely. Manual Rows sub-mode remains unchanged. (FR-001, FR-012)
- Q: How many output cells should the live preview (FR-009) display? → A: 10 rows, fixed. (FR-009, SC-004)
- Q: Should the live preview use real source data or placeholder labels? → A: Real source data when a file is loaded; positional labels ("Row N" / "—") as fallback when no file is loaded. (FR-009)
- Q: In Horizontal direction, does Start/Fill/Space index source rows or source columns? → A: Always source data rows; direction only controls the output axis in Excel. (FR-008)
- Q: How/where should validation errors for invalid field values (FR-003/004) be shown? → A: Inline red border + tooltip on the invalid field; Save button disabled until all values are valid. (FR-003, FR-004, FR-013)
- Q: Should the live preview show the target Excel cell address alongside each value? → A: Yes — format is "A2 → 9", "A3 → —" for skipped rows. (FR-009)
- Q: Should Start Field and Fill Field have a maximum value enforced? → A: No upper bound; naturally bounded by source row count at export time. (FR-003, FR-004, FR-010, Edge Cases)
- Q: Does Running Mode need any UI changes, or is this export-engine only? → A: Export-engine only — Running Mode reads flex fields from JSON silently; no UI changes to RunningModeController. (FR-014, SC-005)
- Q: Should changing the Excel start cell also trigger a live preview refresh? → A: Yes — the preview updates on any change to Start Field, Fill Field, Space Field, or the Excel start cell. (FR-009)
