# Feature: Excel Export

## Summary

Generates XLSX Excel files by applying mapping rules to parsed text data.

## Entry Point

- **Class**: `ExcelWriter`
- **Method**: `writeAdvancedMappedFile(File txtFile, File mappingFile, File outputFile)`
- **Trigger**: "Export to Excel" button in main window

## Process Flow

```
┌─────────────┐    ┌──────────────┐    ┌────────────────┐    ┌─────────────┐
│  TXT File   │───▶│  TxtParser   │───▶│  Apply Mapping │───▶│  XLSX File  │
└─────────────┘    │  parseFile() │    │  rules         │    └─────────────┘
                   └──────────────┘    └────────────────┘
                          │                    │
                   ┌──────────────┐    ┌────────────────┐
                   │ JSON Mapping │    │  Apache POI    │
                   │    File      │    │  XSSFWorkbook  │
                   └──────────────┘    └────────────────┘
```

## Export Steps

1. **Parse source file** using `TxtParser.parseFile()`
2. **Load mapping JSON** using Jackson
3. **Create workbook** with sheet named "Result"
4. **For each mapping rule**:
   - Parse cell reference using `CellReference` (supports AA, AB, etc.)
   - Determine source row indices from `rowPattern` or `rowIndexes`
   - Write optional title (if row > 0 for vertical)
   - Write data values in specified direction

## Cell Reference Support

Uses Apache POI's `CellReference` class for proper parsing:
- Supports single-letter columns: A-Z
- Supports multi-letter columns: AA, AB, ..., ZZ, AAA, etc.
- Examples: A1, B2, AA123, ZZ999

## Writing Directions

### Vertical (default)
Data flows down rows:
```
   A      B
1  Title
2  Val1
3  Val2
4  Val3
```

### Horizontal
Data flows across columns:
```
   A      B      C      D
1  Title  Val1   Val2   Val3
```

## Input Requirements

| Input | Description |
|-------|-------------|
| TXT File | Source data file (TXT or ASC format) |
| Mapping JSON | Column mapping configuration |
| Output Path | Destination XLSX file path |

## Output

- Single XLSX file with one sheet named "Result"
- Data placed according to mapping rules
- Title cells placed one row above (vertical, if row > 0) or same row (horizontal)

## Error Handling

- Uses try-with-resources for proper resource cleanup
- Validates row indices before accessing data
- Skips invalid row indices gracefully
- Empty values for out-of-range column indices

## Dependencies

- Apache POI `XSSFWorkbook` for XLSX generation
- Apache POI `CellReference` for cell parsing
- Jackson for JSON mapping deserialization
- `TxtParser` for source file parsing
