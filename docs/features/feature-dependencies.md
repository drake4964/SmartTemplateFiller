# Feature Dependencies

## Dependency Matrix

| Feature | Depends On | Used By |
|---------|------------|---------|
| File Parsing | - | Teach Mode, Excel Export |
| Teach Mode | File Parsing | Excel Export (via JSON) |
| Excel Export | File Parsing, Mapping JSON | - |

## Component Dependencies

```
┌────────────────────────────────────────────────────────────┐
│                    MainController                          │
│  ┌─────────────┐  ┌─────────────────┐  ┌───────────────┐  │
│  │ Load TXT    │  │    Teach Mode   │  │ Export Excel  │  │
│  └──────┬──────┘  └────────┬────────┘  └───────┬───────┘  │
│         │                  │                    │          │
└─────────┼──────────────────┼────────────────────┼──────────┘
          │                  │                    │
          ▼                  ▼                    ▼
   ┌─────────────┐   ┌─────────────────┐   ┌─────────────┐
   │  TxtParser  │◄──│ TeachMode       │──▶│ ExcelWriter │
   │  parseFile  │   │ Controller      │   │             │
   └─────────────┘   └─────────────────┘   └──────┬──────┘
          │                  │                    │
          │                  ▼                    │
          │          ┌─────────────────┐          │
          └─────────▶│  JSON Mapping   │◄─────────┘
                     │  File           │
                     └─────────────────┘
```

## Library Dependencies

| Feature | External Libraries |
|---------|-------------------|
| File Parsing | Jackson (for column_config.json) |
| Teach Mode | Jackson (for mapping save/load), Apache POI (for CellReference) |
| Excel Export | Apache POI (XSSFWorkbook), Jackson (for mapping load) |

## Data Flow Dependencies

### Teach Mode Flow
```
TableView Data (from parsed file)
      │
      ▼
TeachModeController
      │
      ├──▶ columnComboBox (populated from TableView columns)
      │
      └──▶ smartPreview (populated from TableView items)
```

### Export Flow
```
Source TXT File ──▶ TxtParser ──▶ List<List<String>>
                                         │
Mapping JSON File ──▶ Jackson ──────────▶│
                                         ▼
                                   ExcelWriter
                                         │
                                         ▼
                                   XLSX Output
```

## Critical Integration Points

| Integration | Description | Impact if Broken |
|-------------|-------------|------------------|
| TxtParser ↔ TableView | Parsed data displayed in UI | Cannot view or teach data |
| TeachMode ↔ TableView | Mapping references column indices | Invalid mappings |
| ExcelWriter ↔ TxtParser | Same parsing logic for consistency | Data mismatch |
| JSON Format | Consistent between save and load | Failed mapping load |
