# Implementation Plan: Semicolon Delimiter Column Split (Simplified)

**Branch**: `004-semicolon-delimiter-split` | **Date**: 2026-05-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-semicolon-delimiter-split/spec.md`

## Summary

This plan transitions the semicolon splitting of files to **Load TXT File** inside `TxtParser`. The loaded file is automatically partitioned into columns, eliminating any Semicolon-specific modes, preview tables, toggles, or loaders inside **Teach Mode**. A **Fixed** checkbox is added to the standard Teach Mode mapping panel to allow users to flag labels (written once) vs repeating multi-cavity measurement data. The export engine (`ExcelWriter`) automatically handles multiple cavity blocks separated by `@101` lines by partitioning rows and applying column offsets during writing or appending.

## Technical Context

**Language/Version**: Java 17 (LTS)
**Primary Dependencies**: JavaFX 17.0.15 (UI), Apache POI 5.2.3 (Excel), Jackson 2.15.3 (JSON), JUnit 5.10.0 + Mockito 5.7.0 (testing)
**Storage**: JSON mapping files (extended with the standard `fixed` flag in `ColumnMapping`).
**Testing**: `./gradlew.bat test` (JUnit 5); new parser and export-engine unit tests.
**Target Platform**: Windows desktop (JavaFX)
**Performance Goals**: Semicolon splitting renders on main screen within 1 second for files ≤500 lines; export completes in <5 seconds.
**Constraints**: Clean separation, backward compatibility for mappings without the `fixed` flag.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. User Experience First | Splitting is completely automatic on load. Standard Teach Mode is kept clean and unified. | ✅ Pass |
| II. Modular Design | Integrated cleanly into TxtParser and ExcelWriter without duplicate or convoluted structures. | ✅ Pass |
| III. Configuration-Driven | Ported using standard `colMappings` saved with standard `fixed` property. | ✅ Pass |
| IV. Quality Testing | Unit tests covers auto-splitting, cavity block parsing, fixed vs data mappings. | ✅ Pass |
| V. Documentation | Documentation updated to reflect the simplified architecture. | ✅ Pass |
| VI. Reusable Components & Open Source | Reuses standard POI and Jackson libraries. | ✅ Pass |
| VII. Security & Data Handling | Graceful boundary validation and empty cells on index out-of-bounds. | ✅ Pass |

## Project Structure

### Documentation (this feature)

```text
specs/004-semicolon-delimiter-split/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code

```text
src/main/java/com/example/smarttemplatefiller/
├── Launcher.java
├── MainApp.java
├── MainController.java
├── TeachModeController.java
├── TxtParser.java
├── ExcelWriter.java
├── mapping/
│   └── ColumnMapping.java
```

**Structure Decision**: Single Java/JavaFX project using Gradle.

## Complexity Tracking

*No Constitution violations.*
