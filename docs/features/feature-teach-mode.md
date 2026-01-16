# Feature: Teach Mode

## Summary

Interactive UI for creating column-to-cell mappings with drag-and-drop reordering and Excel-like data preview.

## Entry Point

- **Controller**: `TeachModeController`
- **View**: `teach_mode.fxml`
- **Trigger**: "Teach Mode" button in main window

## UI Layout

The Teach Mode window is organized into three collapsible sections:

```
+----------------------------------------------------------+
|  + Add New Mapping                                        |
|  +------------------------------------------------------+ |
|  | Source Column: [v]  ->  Excel Cell: [___]            | |
|  | Direction: [v]  Row Pattern: [v]  Manual: [___]      | |
|  | Title: [___]         [+ Add Mapping]                 | |
|  +------------------------------------------------------+ |
|                                                           |
|  Mappings (drag to reorder)                              |
|  +------------------------------------------------------+ |
|  | 1. Col 1 -> A2 [vertical] (odd rows) - "Diameter"    | |
|  | 2. Col 2 -> B2 [vertical] (all rows)                 | |
|  | 3. Col 3 -> C2 [horizontal] (5 specific rows)        | |
|  +------------------------------------------------------+ |
|  [Delete Selected] [Move Up] [Move Down]    [Clear All]  |
|                                                           |
|  Excel Preview (how data will appear)                    |
|  +------------------------------------------------------+ |
|  |     |    A       |    B       |    C       |         | |
|  |  1  | [Diameter] |            |            |         | |
|  |  2  | 25.001     | 10.5       | 0.002      |         | |
|  |  3  | 25.003     | 10.6       | 0.001      |         | |
|  |  4  | 24.998     | 10.4       | 0.003      |         | |
|  +------------------------------------------------------+ |
|  Showing preview (rows 1-4, columns A-C)                 |
|                                                           |
|         [Save Mapping]  [Load Mapping]                   |
+----------------------------------------------------------+
```

## Excel-Like Preview

The preview shows data exactly as it will appear in Excel:

| Feature | Description |
|---------|-------------|
| Column headers | Excel letters (A, B, C, ..., AA, AB, ...) |
| Row numbers | Actual Excel row numbers |
| Grid layout | Matches exported Excel structure |
| Titles | Shown in brackets `[Title]` in header row |
| Status bar | Shows preview range (e.g., "rows 1-8, columns A-C") |

## Drag-and-Drop Reordering

Mappings can be reordered by dragging:
1. Click and hold on any mapping row
2. Drag to the desired position
3. Release to drop
4. Visual highlight shows drop target

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
| `excelPreviewTable` | Excel-like grid preview |
| `previewStatusLabel` | Shows preview range info |

## Mapping Actions

| Button | Action |
|--------|--------|
| **+ Add Mapping** | Create mapping from current fields |
| **Delete Selected** | Remove selected mapping |
| **Move Up** | Move selected mapping up |
| **Move Down** | Move selected mapping down |
| **Clear All** | Remove all mappings (with confirmation) |
| **Save Mapping** | Export to JSON file |
| **Load Mapping** | Import from JSON file |

## Row Selection Modes

| Mode | Description | Rows Selected |
|------|-------------|---------------|
| **Odd Rows** | Display rows 1, 3, 5, 7... | Row 1, Row 3, Row 5... |
| **Even Rows** | Display rows 2, 4, 6, 8... | Row 2, Row 4, Row 6... |
| **All Rows** | All rows from data | All rows |
| **Manual** | User-specified indices | e.g., "1,3,5,7" |

## Input Validation

| Field | Validation |
|-------|------------|
| Source Column | Must be selected |
| Excel Cell | Required, format: `^[A-Z]+[0-9]+$` |
| Direction | Must be selected |
| Manual Rows | Numbers only, comma-separated |

## Mapping Configuration (JSON)

```json
{
  "sourceColumn": 0,
  "startCell": "A2",
  "direction": "vertical",
  "title": "Diameter",
  "rowPattern": { "type": "odd", "start": 0 }
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
4. Review in Excel Preview
5. Drag to reorder if needed
6. Save mapping for reuse
