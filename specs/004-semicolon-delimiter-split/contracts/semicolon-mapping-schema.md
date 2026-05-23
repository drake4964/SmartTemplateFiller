# Contract: Semicolon Mapping JSON Schema

**Branch**: `004-semicolon-delimiter-split` | **Date**: 2026-05-02

---

## ColumnMapping Object (Extended)

New fields are optional, null-excluded from serialization (`@JsonInclude(NON_NULL)`).

```json
{
  "delimiterMode"    : "semicolon",
  "blockRelativeRow" : 2,
  "fieldIndex"       : 1,
  "fixed"            : true,
  "startCell"        : "B3",
  "groupWidth"       : 2
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `delimiterMode` | string | Yes | `"semicolon"` activates this mode |
| `blockRelativeRow` | integer ≥0 | Yes | Zero-based line index within the cavity block |
| `fieldIndex` | integer ≥0 | Yes | Zero-based `;`-split field index to read |
| `fixed` | boolean | Yes | `true` = write once to `startCell`; `false` = shift right per cavity |
| `startCell` | string | Yes | Target Excel cell for cavity 0 (e.g. `"C3"`) |
| `groupWidth` | integer ≥1 | Yes | Count of data mappings sharing this `blockRelativeRow`; drives column offset |

---

## Column Offset Formula

```
Fixed mapping:   targetCell = startCell  (written once, cavity 0)

Data mapping:    targetColumn = startCell.column + (cavityIndex × groupWidth) + fieldOffsetInGroup
                 targetRow    = startCell.row (unchanged)
```

---

## Example: 3 cavities, groupWidth=2

```
     B           C          D          E          F          G          H
3  Distance X  22.800     0.250      22.665     0.250      22.397     0.250
5  Distance Y  22.800     0.250      23.268     0.250      23.045     0.250
```

---

## Backward Compatibility

Mapping files without `delimiterMode` use the existing ASC/CMM path unchanged. `@JsonIgnoreProperties(ignoreUnknown = true)` on `ColumnMapping` silently ignores any unrecognized fields.
