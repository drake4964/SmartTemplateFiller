# Multi-File Merge Export

## Overview

SmartTemplateFiller now supports combining 2-10 input TXT/ASC files into a single Excel output, with flexible row/column-based mappings and optional folder watching for automated processing.

## Features

### Multi-File Merge (User Story 1)
- Load 2-10 input files simultaneously
- Flexible mapping: row-based, column-based, or mixed
- Each mapping specifies: Source File + Source Column → Output Cell
- JSON schema v2.0 with `schemaVersion` field for versioning

### Multi-File Teaching Mode (User Story 2)
- Select source file from dropdown (File 1, File 2, etc.)
- Visual display: "File1:ColA → A1" format
- Save/load extended JSON format with file source identifiers
- Backward compatible with v1.0 single-file mappings

### Folder Watch Auto-Processing (User Story 3)
- Configure watched folders (one per file slot)
- Automatic file matching by prefix (text before underscore) or exact basename
- File stability check (configurable, default 2 seconds)
- Timestamped archive folders for traceability
- Session-only watching (does not persist after app close)

## File Matching Rules

Files are matched across folders by **prefix** (text before first underscore):

| Folder 1 | Folder 2 | Match? |
|----------|----------|--------|
| `PART001_001.txt` | `PART001_002.txt` | ✅ Yes (prefix: PART001) |
| `REPORT.txt` | `REPORT.txt` | ✅ Yes (exact basename) |
| `JOB_A.txt` | `JOB_B.txt` | ❌ No (different prefix) |

## Archive Structure

After processing, files are archived together:

```
output/
└── 2026-01-17_143022/
    ├── PART001.xlsx           # Generated output
    └── inputs/
        ├── PART001_001.txt    # Archived source 1
        └── PART001_002.txt    # Archived source 2
```

## JSON Schema v2.0

```json
{
  "schemaVersion": "2.0",
  "mappings": [
    {
      "sourceFileSlot": 1,
      "sourceColumn": "A",
      "targetCell": "A1",
      "direction": "VERTICAL"
    }
  ],
  "fileSlots": [
    { "slot": 1, "description": "Machine 1 Data" }
  ]
}
```

## New Classes

### Models
- `MultiFileMapping` - Extended mapping with file source (1-10)
- `MappingConfiguration` - JSON schema v2.0 container
- `WatchFolder` - Folder monitoring config
- `ProcessingJob` - Auto-processing task tracking
- Enums: `Direction`, `JobStatus`, `MatchingStrategy`, `TimestampFormat`

### Services
- `MultiFileMergeService` - Core merge logic
- `FolderWatchService` - Folder monitoring with WatchService
- `FileMatchingService` - Prefix/basename file matching
- `ArchiveService` - Timestamped archiving

### Utilities
- `FileStabilityChecker` - Configurable file stability check
- `MappingUpgrader` - v1.0 to v2.0 backward compatibility

### UI
- `FileStatusIndicator` - Visual folder status indicator
