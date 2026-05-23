# Data Model: Semicolon Delimiter Column Split (Simplified)

**Branch**: `004-semicolon-delimiter-split` | **Date**: 2026-05-23

---

## Entity Relationship Overview

```
TxtParser.parseFile()
    └── Detects if the file is semicolon-delimited (contains ";" on multiple lines)
    └── Splits each line by ";" into multiple columns
    └── Returns List<List<String>> to populate standard TableView

ColumnMapping (standard)
    └── private Boolean fixed;  ← New property: true/false. Ticked in standard mapping UI.

ExcelWriter.writeAdvancedMappedFile()
    └── Detects if the txt file contains ";" and "@101"
    └── Divides the data into cavity blocks using "@101" boundaries
    └── Groups non-fixed mappings targeting the same Excel row to calculate groupWidth
    └── Writes fixed mappings to static cells, and shifts data mappings column-by-column across cavities
```

---

## Class Definitions

### `ColumnMapping` (MODIFY — `mapping/ColumnMapping.java`)

We extend `ColumnMapping` to support the standard `fixed` property.

```java
public class ColumnMapping {
    private Integer sourceColumn;
    private String startCell;
    private String direction;
    private String title;
    
    // Legacy / Flex fields
    private Map<String, Object> rowPattern;
    private List<Integer> rowIndexes;
    private Integer startField;
    private Integer fillField;
    private Integer spaceField;

    // Added property
    private Boolean fixed; // true = Fixed mapping (written once); false/null = Data mapping
}
```

---

## JSON Schema (Standard Mappings with Fixed Property)

### Example Mappings File

```json
[
  {
    "sourceColumn": 1,
    "startCell": "B3",
    "direction": "vertical",
    "fixed": true,
    "startField": 1,
    "fillField": 1,
    "spaceField": 41
  },
  {
    "sourceColumn": 3,
    "startCell": "C3",
    "direction": "vertical",
    "fixed": false,
    "startField": 1,
    "fillField": 1,
    "spaceField": 41
  },
  {
    "sourceColumn": 4,
    "startCell": "D3",
    "direction": "vertical",
    "fixed": false,
    "startField": 1,
    "fillField": 1,
    "spaceField": 41
  }
]
```

---

## Group Width and Cavity Shifting Calculations

When `ExcelWriter` processes a semicolon-delimited file:

1. **Group mappings by Excel target row**:
   - `B3` (fixed)
   - `C3` (data)
   - `D3` (data)
2. **Calculate Group Width**:
   - The data mappings targeting Row 3 are `C3` and `D3`.
   - The total number of data mappings for Row 3 is 2. Thus, `groupWidth = 2`.
   - Sort data mappings by their start column: `C` (index 2) comes first, `D` (index 3) comes second.
3. **Calculate target cell for cavity `N` (0-indexed)**:
   - For `C3` (data mapping, index offset 0 within the group):
     - `targetCol = startCol + (N × groupWidth) + offset = C + (N × 2) + 0`.
     - Cavity 0: `C3`
     - Cavity 1: `E3`
     - Cavity 2: `G3`
   - For `D3` (data mapping, index offset 1 within the group):
     - `targetCol = startCol + (N × groupWidth) + offset = C + (N × 2) + 1` (relative to the group start `C`).
     - Cavity 0: `D3`
     - Cavity 1: `F3`
     - Cavity 2: `H3`
   - For `B3` (fixed mapping):
     - Written exactly to `B3` without any cavity shifting.
