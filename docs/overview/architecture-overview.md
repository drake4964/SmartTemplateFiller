# Architecture Overview

## Architectural Pattern

SmartTemplateFiller follows the **Model-View-Controller (MVC)** pattern as implemented by JavaFX:

- **View**: FXML files define UI structure
- **Controller**: Java controller classes handle user interactions
- **Model**: Data structures (Lists, Maps) managed by controllers

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         JavaFX Application                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────────┐   │
│  │   MainApp     │  │ Controllers   │  │   Utilities       │   │
│  │   (Entry)     │  │               │  │                   │   │
│  │               │  │ MainController│  │ TxtParser         │   │
│  │ start()       │─▶│ TeachMode     │─▶│ ExcelWriter       │   │
│  │ main()        │  │ Controller    │  │ FileChooserBuilder│   │
│  └───────────────┘  └───────────────┘  └───────────────────┘   │
│         │                  │                    │               │
│         ▼                  ▼                    ▼               │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                      Resources                          │    │
│  │  • main.fxml        • teach_mode.fxml                   │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
         │                                        │
         ▼                                        ▼
┌─────────────────┐                    ┌─────────────────────┐
│   File System   │                    │   External Libraries│
│ • TXT/ASC files │                    │ • Apache POI        │
│ • JSON mappings │                    │ • Jackson           │
│ • XLSX outputs  │                    │ • JavaFX            │
└─────────────────┘                    └─────────────────────┘
```

## Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| `MainApp` | Application bootstrap, loads main FXML, injects primary stage |
| `MainController` | Main window actions: load file, launch teach mode, export Excel, launch running mode |
| `TeachModeController` | Mapping configuration UI: add/remove/save/load mappings |
| `RunningModeController` | Running mode UI: configure/start/stop automated folder watching |
| `TxtParser` | File parsing with 3 strategies based on content detection |
| `ExcelWriter` | Applies mappings to parsed data, writes XLSX via Apache POI |
| `FileChooserBuilder` | Fluent API for configuring file dialogs with directory memory |
| `FolderWatcher` | Background service for scheduled folder scanning and auto-conversion |
| `RunningModeConfig` | Configuration POJO with JSON persistence for running mode settings |

## Data Flow

```
User Action          Component                  Data Transformation
───────────          ─────────                  ───────────────────
Load TXT      ──▶   MainController    ──▶    TxtParser.parseFile()
                          │                         │
                          ▼                         ▼
              Display in TableView        List<List<String>> rows
                          
Teach Mode    ──▶   TeachModeController ──▶  Build mapping JSON
                          │                         │
                          ▼                         ▼
              Save as .json file         List<Map<String,Object>>

Export Excel  ──▶   MainController    ──▶    ExcelWriter.write()
                          │                         │
                          ▼                         ▼
              Save as .xlsx file         Apache POI Workbook

Running Mode  ──▶   RunningModeController ──▶  FolderWatcher.start()
                          │                         │
                          ▼                         ▼
              Auto-process files          Timestamped output folders
              in watch folder             with archived originals
```

## Related Diagrams

- [System Context Diagram](../diagrams/system-context.mmd)
- [Container Diagram](../diagrams/container-diagram.mmd)
- [Component Diagram](../diagrams/component-diagram.mmd)
- [Source Code Map](SOURCE_CODE_MAP.md) (detailed class reference)

