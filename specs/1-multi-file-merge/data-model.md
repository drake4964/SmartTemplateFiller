# Data Model: Multi-File Merge Export

**Feature**: 1-multi-file-merge
**Date**: 2026-01-17

## Entities

### MultiFileMapping

Extends existing `Mapping` entity to support multi-file sources.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| id | String | Unique identifier | Auto-generated UUID |
| sourceFileSlot | Integer | File slot (1-10) | Required, 1-10 |
| sourceColumn | String | Column identifier (A, B, C...) | Required, valid column |
| targetCell | String | Excel cell reference | Required, valid cell ref |
| direction | Enum | VERTICAL or HORIZONTAL | Required |
| includeTitle | Boolean | Whether to include column title | Default: false |

**Relationships**:
- Belongs to `MappingConfiguration` (many-to-one)

### MappingConfiguration

Extended configuration container with schema versioning.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| schemaVersion | String | Schema version | Required, e.g., "2.0" |
| mappings | List<MultiFileMapping> | All mappings | Required, non-empty |
| fileSlots | List<FileSlot> | File slot definitions | Required for multi-file |
| watchConfig | WatchConfiguration | Folder watching config | Optional |
| archiveConfig | ArchiveConfiguration | Archive settings | Optional |

### FileSlot

Represents a file input slot in a multi-file configuration.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| slot | Integer | Slot number (1-10) | Required, unique |
| description | String | User-friendly name | Optional |
| expectedPattern | String | File name pattern | Optional |

### WatchFolder

Folder to monitor for automatic processing.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| id | String | Unique identifier | Auto-generated |
| folderPath | Path | Absolute folder path | Required, must exist |
| linkedSlot | Integer | Associated file slot | Required, 1-10 |
| isActive | Boolean | Currently watching | Runtime state |
| lastFileDetected | Path | Most recent file found | Runtime state |

**Validation**:
- `folderPath` must be a valid directory
- `folderPath` must not be a system directory
- No two WatchFolders can have the same `folderPath`

### WatchConfiguration

Configuration for folder watching behavior.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| stabilityCheckSeconds | Integer | File stability delay | Default: 2, min: 1, max: 30 |
| matchingStrategy | Enum | PREFIX or EXACT_BASENAME | Default: PREFIX |

### ArchiveConfiguration

Configuration for post-processing archiving.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| outputFolder | Path | Base output directory | Required |
| timestampFormat | Enum | DATE_ONLY or DATETIME | Default: DATETIME |
| archiveInputFiles | Boolean | Move input files to archive | Default: true |

### ProcessingJob

Represents an auto-processing task.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| id | String | Unique identifier | Auto-generated UUID |
| status | Enum | PENDING, PROCESSING, COMPLETED, FAILED | Required |
| matchedPrefix | String | File prefix that triggered job | Required |
| inputFiles | Map<Integer, Path> | Slot → file path | Required |
| outputFile | Path | Generated Excel file | Set on completion |
| archiveFolder | Path | Archive location | Set on completion |
| startTime | Instant | Processing start time | Auto-set |
| endTime | Instant | Processing end time | Auto-set |
| errorMessage | String | Error details if failed | Optional |

**State Transitions**:
```text
PENDING → PROCESSING → COMPLETED
    │           │
    └───────────┴─→ FAILED
```

## Enumerations

### Direction
- `VERTICAL`: Data fills down rows
- `HORIZONTAL`: Data fills across columns

### JobStatus
- `PENDING`: Files matched, waiting to process
- `PROCESSING`: Currently generating output
- `COMPLETED`: Successfully created output
- `FAILED`: Error occurred during processing

### MatchingStrategy
- `PREFIX`: Match by text before first underscore
- `EXACT_BASENAME`: Match by exact filename (minus extension)

### TimestampFormat
- `DATE_ONLY`: `2026-01-17`
- `DATETIME`: `2026-01-17_143022`

## JSON Schema v2.0

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["schemaVersion", "mappings"],
  "properties": {
    "schemaVersion": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+$"
    },
    "mappings": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["sourceFileSlot", "sourceColumn", "targetCell", "direction"],
        "properties": {
          "sourceFileSlot": { "type": "integer", "minimum": 1, "maximum": 10 },
          "sourceColumn": { "type": "string" },
          "targetCell": { "type": "string", "pattern": "^[A-Z]+[0-9]+$" },
          "direction": { "enum": ["VERTICAL", "HORIZONTAL"] },
          "includeTitle": { "type": "boolean", "default": false }
        }
      }
    },
    "fileSlots": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["slot"],
        "properties": {
          "slot": { "type": "integer", "minimum": 1, "maximum": 10 },
          "description": { "type": "string" }
        }
      }
    },
    "watchConfig": {
      "type": "object",
      "properties": {
        "stabilityCheckSeconds": { "type": "integer", "default": 2 },
        "matchingStrategy": { "enum": ["PREFIX", "EXACT_BASENAME"], "default": "PREFIX" }
      }
    },
    "archiveConfig": {
      "type": "object",
      "properties": {
        "outputFolder": { "type": "string" },
        "timestampFormat": { "enum": ["DATE_ONLY", "DATETIME"], "default": "DATETIME" },
        "archiveInputFiles": { "type": "boolean", "default": true }
      }
    }
  }
}
```

## Backward Compatibility

- Files without `schemaVersion` treated as v1.0 (single-file mode)
- v1.0 mappings auto-upgraded: `sourceFileSlot` defaults to 1
- No data loss on upgrade
