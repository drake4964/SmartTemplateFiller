# SmartTemplateFiller - AI Context Document

> **Purpose**: Single entry point for AI agents to quickly understand this codebase.  
> **Last Updated**: 2026-01-28

## Quick Summary

**SmartTemplateFiller** is a JavaFX desktop application that transforms raw measurement data from TXT/ASC files into structured Excel reports using configurable JSON mappings.

| Attribute | Value |
|-----------|-------|
| Language | Java 17 |
| UI Framework | JavaFX 17.0.15 |
| Build Tool | Gradle 8.5+ |
| Excel Library | Apache POI 5.2.3 |
| JSON Library | Jackson 2.15.3 |
| Architecture | MVC (Model-View-Controller) |

---

## Project Structure

```
SmartTemplateFiller/
├── src/main/java/com/example/smarttemplatefiller/
│   ├── MainApp.java                 # JavaFX Application entry point
│   ├── MainController.java          # Main window controller (185 lines)
│   ├── TeachModeController.java     # Mapping configuration UI (560 lines)
│   ├── RunningModeController.java   # Automated folder watching UI (270 lines)
│   ├── TxtParser.java               # Multi-strategy file parser (189 lines)
│   ├── ExcelWriter.java             # Excel generation via Apache POI (107 lines)
│   ├── FileChooserBuilder.java      # Fluent file dialog API (88 lines)
│   ├── FolderWatcher.java           # Background file monitoring service (188 lines)
│   ├── RunningModeConfig.java       # Running mode configuration POJO (103 lines)
│   └── license/                      # License verification module
│       ├── LicenseValidator.java     # Interface for license validation
│       ├── DefaultLicenseValidator.java # Main implementation
│       ├── EncryptionValidator.java  # AES-256 + HMAC cryptography
│       └── LicenseErrorDialog.java   # Error dialog UI
│
├── src/main/resources/
│   ├── fxml/
│   │   ├── main.fxml                # Main window layout
│   │   ├── teach_mode.fxml          # Teach Mode dialog layout
│   │   └── running_mode.fxml        # Running Mode window layout
│   ├── column_config.json           # Fixed-column parsing configuration
│   └── advanced_mapping_example.json
│
├── docs/                            # Documentation (see docs/README.md)
│   ├── overview/                    # Architecture, project overview
│   ├── features/                    # Feature documentation
│   ├── diagrams/                    # Mermaid diagrams (.mmd)
│   ├── decisions/                   # ADRs (Architectural Decision Records)
│   └── workflows/                   # User workflow documentation
│
├── .specify/memory/
│   └── constitution.md              # Project principles and standards
│
└── build.gradle                     # Gradle build configuration
```

---

## Core Classes Reference

### Controllers (UI Layer)

| Class | Purpose | Key Methods |
|-------|---------|-------------|
| `MainApp` | JavaFX Application bootstrap | `start()`, `main()` |
| `MainController` | Main window: load files, launch dialogs, export | `handleLoadTxt()`, `handleTeachMode()`, `handleExportToExcel()`, `handleRunningMode()` |
| `TeachModeController` | Mapping configuration with drag-drop and Excel preview | `handleAddMapping()`, `handleSaveMapping()`, `handleLoadMapping()`, `updateExcelPreview()` |
| `RunningModeController` | Running Mode UI: folder watching configuration | `handleStart()`, `handleStop()`, `loadConfigToUI()`, `saveConfig()` |

### Utilities (Logic Layer)

| Class | Purpose | Key Methods |
|-------|---------|-------------|
| `TxtParser` | Multi-strategy file parsing | `parseFile(File)` → auto-detects format, `parseFixedColumnTable()`, `parseMultiLineGroupedBlock()`, `parseFlatTable()`, `generateIndexes()` |
| `ExcelWriter` | Excel generation with Apache POI | `writeAdvancedMappedFile(txtFile, mappingFile, outputFile)` |
| `FileChooserBuilder` | Fluent file dialog with directory memory | `withTitle()`, `withExtension()`, `open()`, `save()` |
| `FolderWatcher` | Background scheduled folder scanning | `start()`, `stop()`, `scanFolder()`, `processFile()` |
| `RunningModeConfig` | Configuration POJO with JSON persistence | `save()`, `load()` (stored at `~/.smarttemplatefiller/`) |

---

## Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER WORKFLOWS                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  [Manual Export Flow]                                                       │
│  User → Load TXT → (optional) Teach Mode → Export to Excel                  │
│         ↓              ↓                    ↓                               │
│    TxtParser    TeachModeController    ExcelWriter                          │
│         ↓              ↓                    ↓                               │
│  List<List<String>>  mapping.json       output.xlsx                         │
│                                                                             │
│  [Automated Flow - Running Mode]                                            │
│  User → Configure → Start Watching → Auto-process files                     │
│              ↓           ↓                  ↓                               │
│    RunningModeConfig  FolderWatcher   ExcelWriter                           │
│              ↓           ↓                  ↓                               │
│    config.json     (background)      timestamped output folders             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Mapping Configuration JSON Schema

```json
// Schema Version: 1.0 (implicit, see ADR-002)
[
  {
    "sourceColumn": 0,           // 0-based column index from parsed data
    "startCell": "A2",           // Excel cell reference (supports AA, AB, etc.)
    "direction": "vertical",     // "vertical" | "horizontal"
    "title": "Diameter",         // Optional column header
    // Row selection (one of the following):
    "rowPattern": {              // Pattern-based selection
      "type": "odd",             // "odd" | "even" | "all"
      "start": 0                 // Starting index (0-based)
    },
    // OR
    "rowIndexes": [1, 3, 5, 7]   // Explicit row indexes (0-based)
  }
]
```

---

## File Parsing Strategies

`TxtParser.parseFile()` auto-detects format based on file content:

| Strategy | Detection Pattern | Example Use Case |
|----------|-------------------|------------------|
| `parseMultiLineGroupedBlock` | Lines matching `(?i)(Circle|Line|Plane|Point|Distance|Angle).*\(ID:.*\).*` | CMM output files |
| `parseFixedColumnTable` | Lines matching `\s*\d+\s+N\d+\s+.*\s+\*+.*` | Fixed-width measurement tables |
| `parseFlatTable` | Default fallback | Generic space-delimited data |

---

## Key Configuration Files

| File | Purpose | Location |
|------|---------|----------|
| `column_config.json` | Fixed-column widths for parsing | Project root or `src/main/resources/` |
| `mapping.json` | User-created mapping rules | User-defined location |
| `running_mode_config.json` | Running Mode settings | `~/.smarttemplatefiller/` |

---

## Important ADRs (Architectural Decisions)

| ADR | Decision | Rationale |
|-----|----------|-----------|
| ADR-001 | JavaFX for UI | Native Java, FXML separation, rich controls |
| ADR-002 | JSON for mappings | Human-readable, shareable, flexible |
| ADR-003 | Multi-strategy parser | Auto-detection reduces user burden |
| ADR-004 | Apache POI for Excel | Industry standard, CellReference for multi-letter columns |
| ADR-008 | Drag-drop mapping reorder | More intuitive UX |
| ADR-009 | Excel-like preview | Grid mimics actual Excel output |

Full ADR documentation: [docs/decisions/architectural-decisions.md](decisions/architectural-decisions.md)

---

## Constitution Principles (Summary)

From `.specify/memory/constitution.md` (v1.3.0):

1. **User Experience First** - Immediate feedback, clear errors, responsive UI
2. **Modular Design** - Independent, testable modules
3. **Configuration-Driven** - Portable JSON mappings
4. **Quality Testing** - ≥80% coverage on mapping logic
5. **Documentation Excellence** - Maintained for devs and users
6. **Reusable Components** - Prefer open-source libraries
7. **Security & Data Handling** - No sensitive data logging

---

## Common Development Tasks

### Run the Application
```powershell
.\gradlew.bat run
```

### Build Only
```powershell
.\gradlew.bat build
```

### Clean Build
```powershell
.\gradlew.bat clean build
```

---

## Related Documentation

| Document | Path |
|----------|------|
| Project Overview | [docs/overview/project-overview.md](overview/project-overview.md) |
| Architecture | [docs/overview/architecture-overview.md](overview/architecture-overview.md) |
| Feature List | [docs/features/feature-list.md](features/feature-list.md) |
| Teach Mode | [docs/features/feature-teach-mode.md](features/feature-teach-mode.md) |
| Running Mode | [docs/features/feature-running-mode.md](features/feature-running-mode.md) |
| License Verification | [docs/features/feature-license-verification.md](features/feature-license-verification.md) |
| Component Diagram | [docs/diagrams/component-diagram.mmd](diagrams/component-diagram.mmd) |
| Constitution | [.specify/memory/constitution.md](../.specify/memory/constitution.md) |

---

## AI Agent Instructions

When working on this codebase:

1. **Check this file first** - Use as entry point to understand project context
2. **Consult constitution** - Follow principles in `.specify/memory/constitution.md`
3. **Reference diagrams** - Mermaid files in `docs/diagrams/` for visual context
4. **Follow existing patterns** - Match code style of existing controllers
5. **Update documentation** - Keep this file and `/docs` updated with changes
6. **Test coverage** - Maintain ≥80% unit test coverage for mapping logic (per constitution)
