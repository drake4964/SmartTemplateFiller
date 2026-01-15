# Sequence: Mapping Creation

## Overview

This sequence describes how users create column-to-cell mappings in Teach Mode.

## Actors

- **User**: Configures mappings via UI
- **TeachModeController**: Manages mapping state and UI updates
- **TableView**: Source data reference (from MainController)
- **File System**: JSON persistence

## Sequence Diagram

See [sequence-teach-mode.mmd](../diagrams/sequence-teach-mode.mmd) for the visual diagram.

## Step-by-Step Flow

### Initialization

| Step | Actor | Action |
|------|-------|--------|
| 1 | MainController | Calls `setTableView()` with data reference |
| 2 | TeachModeController | Extracts column count |
| 3 | TeachModeController | Populates columnComboBox with "Col 1", "Col 2", etc. |
| 4 | TeachModeController | Sets default direction to "vertical" |
| 5 | TeachModeController | Populates row pattern options |

### Add Mapping Loop

| Step | Actor | Action |
|------|-------|--------|
| 6 | User | Selects source column from dropdown |
| 7 | User | Enters target cell (e.g., "B2") |
| 8 | User | Selects direction (vertical/horizontal) |
| 9 | User | Chooses row pattern OR enters manual rows |
| 10 | User | Optionally enters title |
| 11 | User | Clicks "Add Mapping" |
| 12 | TeachModeController | Validates required fields |
| 13 | TeachModeController | Creates mapping Map object |
| 14 | TeachModeController | Adds to colMappings list |
| 15 | TeachModeController | Calls `updatePreview()` |
| 16 | TeachModeController | Calls `updateSmartPreview()` |

### Preview Updates

**Mapping Preview** shows:
```
Col 4 → A2 [vertical, pattern: {start=1, type=odd}]
Col 5 → B2 [vertical, rows: [1, 3, 5, 7]]
```

**Smart Preview** shows actual values from data that will be exported.

### Persistence

#### Save Mapping
| Step | Actor | Action |
|------|-------|--------|
| 17 | User | Clicks "Save Mapping" |
| 18 | TeachModeController | Opens FileChooser (save mode) |
| 19 | User | Chooses file location |
| 20 | TeachModeController | Serializes colMappings via Jackson |
| 21 | TeachModeController | Writes JSON with pretty printing |

#### Load Mapping
| Step | Actor | Action |
|------|-------|--------|
| 22 | User | Clicks "Load Mapping" |
| 23 | TeachModeController | Opens FileChooser (open mode) |
| 24 | User | Selects JSON file |
| 25 | TeachModeController | Deserializes via Jackson |
| 26 | TeachModeController | Replaces colMappings list |
| 27 | TeachModeController | Updates both preview panels |

### Delete Mapping

| Step | Actor | Action |
|------|-------|--------|
| 28 | User | Clicks "Delete Mapping" |
| 29 | TeachModeController | Removes last mapping from list |
| 30 | TeachModeController | Updates preview panels |
