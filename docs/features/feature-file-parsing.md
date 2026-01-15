# Feature: File Parsing

## Summary

Intelligent multi-strategy file parser that automatically detects and parses TXT/ASC measurement data files.

## Entry Point

- **Class**: `TxtParser`
- **Method**: `parseFile(File file)`

## Supported Formats

| Format | Detection Pattern | Parser Method |
|--------|-------------------|---------------|
| Multi-line Block | Contains `(ID:...)` patterns like `Circle (ID:1)` | `parseMultiLineGroupedBlock()` |
| Fixed Column | Matches `\d+\s+N\d+\s+...` pattern | `parseFixedColumnTable()` |
| Flat Table | Default fallback | `parseFlatTable()` |

## Parsing Strategies

### 1. Multi-line Grouped Block
**Use case**: CMM measurement output files  
**Format**: Header lines followed by key-value pairs

```
Circle (ID:1) Distance =  25.400
  X      =   10.000   10.050   -0.050
  Y      =   20.000   20.100   -0.100
```

**Output**: Each key-value becomes a row with 7 columns: Element, Actual, Nominal, Deviat., Up Tol., Low Tol., Pass/Fail

### 2. Fixed Column Table
**Use case**: Fixed-width column data  
**Configuration**: `column_config.json` defines column widths

```json
{
  "Col1": 3,
  "Col2": 6,
  "Col3": 9
}
```

### 3. Flat Table
**Use case**: Generic whitespace-delimited data  
**Delimiter**: Two or more consecutive whitespace characters

## Output Format

All parsers return `List<List<String>>` - a 2D list of string values where:
- Outer list = rows
- Inner list = column values

## Configuration

### column_config.json
Located in project root, defines fixed-column widths for `parseFixedColumnTable()`:

```json
{
  "Col1": 3,
  "Col2": 6,
  "Col3": 9,
  "Col4": 4,
  "Col5": 11
}
```

## Dependencies

- Java BufferedReader for file I/O
- Jackson for loading column configuration
