# Feature: Teach Mode

## Summary

Interactive UI for creating column-to-cell mappings with drag-and-drop reordering and live data preview.

## Entry Point

- **Controller**: `TeachModeController`
- **View**: `teach_mode.fxml`
- **Trigger**: "Teach Mode" button in main window

## UI Layout

The Teach Mode window is organized into three collapsible sections:

```
┌─────────────────────────────────────────────────────────┐
│  ➕ Add New Mapping                                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │ Source Column: [▼]  → Excel Cell: [___]           │  │
│  │ Direction: [▼]  Row Pattern: [▼]  Manual: [___]   │  │
│  │ Title: [___]         [➕ Add Mapping]              │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  📋 Mappings (drag to reorder)                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 1. Col 1 → A2 [vertical] — "Diameter"             │  │
│  │ 2. Col 2 → B2 [vertical] (odd rows)               │  │
│  │ 3. Col 3 → C2 [horizontal] (5 specific rows)      │  │
│  └───────────────────────────────────────────────────┘  │
│  [🗑️ Delete] [⬆️ Up] [⬇️ Down]         [🧹 Clear All] │
│                                                         │
│  👁️ Smart Preview (data that will be exported)        │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 📊 Diameter (→ A2):                               │  │
│  │    25.001, 25.003, 24.998, 25.002, 25.000         │  │
│  │                                                   │  │
│  │ 📊 Col 2 (→ B2):                                  │  │
│  │    10.5, 10.6, 10.4 ... (2 more)                  │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│         [💾 Save Mapping]  [📂 Load Mapping]           │
└─────────────────────────────────────────────────────────┘
```

## Drag-and-Drop Reordering

Mappings can be reordered by dragging:
1. Click and hold on any mapping row
2. Drag to the desired position
3. Release to drop
4. Visual highlight shows drop target

**Technical Implementation**:
- Custom `ListCell` factory with drag handlers
- Custom `DataFormat` for mapping index transfer
- Visual feedback via style changes during drag

## UI Components

| Component | Purpose |
|-----------|---------|
| `columnComboBox` | Select source data column |
| `cellField` | Enter target Excel cell (auto-uppercase) |
| `directionComboBox` | Vertical or Horizontal layout |
| `rowPatternComboBox` | Odd/Even/All row filter |
| `manualRowField` | Explicit row indices (e.g., "1,3,5") |
| `titleField` | Column header for Excel output |
| `mappingListView` | Drag-reorderable list of mappings |
| `smartPreview` | Live preview of data to export |

## Mapping Actions

| Button | Action |
|--------|--------|
| ➕ **Add Mapping** | Create mapping from current fields |
| 🗑️ **Delete Selected** | Remove selected mapping |
| ⬆️ **Move Up** | Move selected mapping up |
| ⬇️ **Move Down** | Move selected mapping down |
| 🧹 **Clear All** | Remove all mappings (with confirmation) |
| 💾 **Save Mapping** | Export to JSON file |
| 📂 **Load Mapping** | Import from JSON file |

## Smart Preview

Shows actual data values that will be exported:
- Header with column title and target cell
- First 5 values from each mapping
- "... (N more)" indicator for additional values
- Updates automatically when mappings change

Example:
```
📊 Diameter (→ A2):
   25.001, 25.003, 24.998, 25.002, 25.000

📊 Roundness (→ B2):
   0.002, 0.001, 0.003 ... (7 more)
```

## Input Validation

| Field | Validation |
|-------|------------|
| Source Column | Must be selected |
| Excel Cell | Required, format: `^[A-Z]+[0-9]+$` |
| Direction | Must be selected |
| Manual Rows | Numbers only, comma-separated |

Validation errors shown as alert dialogs with clear messages.

## Mapping Configuration (JSON)

```json
{
  "sourceColumn": 4,
  "startCell": "A2",
  "direction": "vertical",
  "title": "Diameter",
  "rowPattern": { "type": "odd", "start": 1 },
  "rowIndexes": [1, 3, 5, 7, 9]
}
```

## Workflow

1. Load data file in main window
2. Click "Teach Mode"
3. For each column:
   - Select source column
   - Enter target cell (e.g., "A2")
   - Choose direction and row pattern
   - Add optional title
   - Click "Add Mapping"
4. Drag to reorder if needed
5. Review in Smart Preview
6. Save mapping for reuse
