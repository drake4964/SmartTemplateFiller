# Implementation Plan: Row Pattern Flex

**Branch**: `003-row-pattern-flex` | **Date**: 2026-04-01 | **Spec**: [specs/003-row-pattern-flex/spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-row-pattern-flex/spec.md`

## Summary

Enhance Teach Mode Row Pattern and manual row controls with start field, fill field, and space field for flexible source-to-Excel cell mapping. The new flex fields completely replace the legacy odd/even/all controls.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: JavaFX 17.0.15, Apache POI 5.2.3, Jackson 2.15.3
**Storage**: JSON files (ColumnMapping schema)
**Testing**: JUnit 5.10.0, Mockito 5.7.0
**Target Platform**: Desktop (Windows/Mac/Linux)
**Project Type**: Single project desktop application
**Performance Goals**: UI preview updates <200ms
**Constraints**: Zero data loss for legacy mappings, fully offline capable
**Scale/Scope**: Teach Mode and Running Mode (headless export) updates mapping components.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. User Experience First | All UI operations provide immediate feedback? Error messages clear & actionable? | ✅ Pass |
| II. Modular Design | Parsers, mappers, exporters are standalone modules? No circular dependencies? | ✅ Pass |
| III. Configuration-Driven | Mappings stored as JSON? Schema versioned? No hardcoded logic? | ✅ Pass |
| IV. Quality Testing | Parser tests for all formats? ≥80% coverage target? Edge cases covered? | ✅ Pass |
| V. Documentation | Feature docs in `/docs/features/`? Architecture decisions recorded? | ✅ Pass |
| VI. Reusable Components & Open Source | Using proven libraries (Lombok, Commons)? Components designed for reuse? | ✅ Pass |
| VII. Security & Data Handling | No sensitive data logged? Temp files cleaned? Input validated? | ✅ Pass |

**Status Legend**: ⬜ Not Checked | ✅ Pass | ❌ Fail (requires justification in Complexity Tracking)

## Project Structure

### Documentation (this feature)

```text
specs/003-row-pattern-flex/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/
│   │   └── com/smarttemplatefiller/
│   │       ├── model/
│   │       │   ├── mapping/ColumnMapping.java
│   │       │   └── mapping/RowPatternDescriptor.java
│   │       ├── engine/
│   │       │   └── MappingPathResolver.java
│   │       └── ui/
│   │           ├── controllers/TeachModeController.java
│   │           └── components/FlexPatternPanel.java
│   └── resources/
│       └── views/
│           ├── TeachMode.fxml
│           └── FlexPatternPanel.fxml
tests/
└── test/
    └── java/
        └── com/smarttemplatefiller/
            ├── model/
            │   └── mapping/RowPatternDescriptorTest.java
            └── engine/
                └── MappingPathResolverTest.java
```

**Structure Decision**: Monolithic JavaFX desktop project structure, locating model, engine, and UI components in existing packages.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
