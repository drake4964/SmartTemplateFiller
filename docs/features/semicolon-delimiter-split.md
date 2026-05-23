# Feature: Semicolon Delimiter Split (004)

## Overview
The Semicolon Delimiter Split feature introduces support for parsing text files where data columns are semicolon-delimited, rather than the legacy ASC/CMM fixed-width/tab-delimited formats. This feature is particularly useful for extracting measurement data from multi-cavity machines that output semicolon-separated values grouped into distinct blocks separated by `@101` boundary markers.

Under the new unified design, semicolon files are **automatically detected and split on load** by `TxtParser`. Semicolon Mode toggles, separate preview tables, and loaders are completely removed from Teach Mode, merging semicolon files seamlessly with standard, flex, and manual mapping workflows.

## Step-by-Step Usage
1. **Load a Semicolon File:** Click "Load TXT File" on the main screen and select a semicolon-delimited text file. The application automatically detects the semicolon structure, splits it into proper columns, and renders it in the main table view.
2. **Enter Teach Mode:** Click "Teach Mode" to configure column-to-cell mappings.
3. **Configure Standard Mappings:**
   - Select your desired source column from the dropdown.
   - Enter your target starting cell in Excel (e.g., `B3`).
   - **Fixed mapping:** Check the "Fixed mapping" checkbox in the standard column mapping panel if the value represents a static label or header (written only once for the first block). Uncheck it if the value is data that should shift horizontally across cavity blocks.
4. **Save Configuration:** Click "Save Mapping" to serialize standard configurations to JSON.

## Expected Excel Layout
During Excel Export or Append operations, semicolon-delimited files are automatically partitioned into cavities by `@101` block boundaries:
- If a file has 3 cavities (delimited by `@101`), and you map a non-fixed column to starting cell `B3` (with group width = 1):
  - Cavity 0 (Block 0) data appears in column `B` (`B3`).
  - Cavity 1 (Block 1) data automatically shifts to column `C` (`C3`).
  - Cavity 2 (Block 2) data automatically shifts to column `D` (`D3`).
- If you map a **fixed** value to starting cell `B2`:
  - The value is written once for Cavity 0 in `B2`.
  - Cavities 1 and 2 are skipped for this mapping.

## JSON Schema Reference
Semicolon mapping configuration is fully unified with standard JSON mapping schemas. To differentiate fixed values from repeating cavity values, mappings are extended with the standard `fixed` flag:
```json
[
  {
    "sourceColumn": 0,
    "startCell": "B3",
    "direction": "vertical",
    "fixed": false,
    "startField": 1,
    "fillField": 1,
    "spaceField": 0
  },
  {
    "sourceColumn": 1,
    "startCell": "B2",
    "direction": "vertical",
    "fixed": true,
    "rowIndexes": [0]
  }
]
```

## Troubleshooting
| Issue | Cause | Solution |
|-------|-------|----------|
| Table preview shows single-column un-split rows | The file lacks semicolon separators | Verify the file format is indeed semicolon-separated. |
| Fields do not shift horizontally for multiple cavities | The mapping has the "Fixed mapping" checkbox checked | Uncheck the "Fixed mapping" checkbox when configuring the mapping in Teach Mode. |
