# Research: Semicolon Delimiter Column Split (Simplified)

**Branch**: `004-semicolon-delimiter-split` | **Date**: 2026-05-23
**Purpose**: Document research and design decisions for the simplified load-time semicolon splitting.

---

## R-001 — Semicolon Splitting on File Load

**Decision**: Auto-detect and split semicolon-delimited files during the load pipeline in `TxtParser.parseFile()`.

**Detection Rule**:
If any of the first 10 preview lines contain at least one `;` character:
`boolean isSemicolon = previewLines.stream().anyMatch(l -> l.contains(";"));`

**Splitting Strategy**:
- Use `line.split(";", -1)` to split lines with `;` (preserving empty trailing values).
- Lines without `;` (such as `@101` or header labels) are treated as single-element lists containing the raw line.
- Trim each field to clean up white space.
- The resulting rows are loaded as standard tabular data.
- The Main controller's `TableView` displays these automatically.

---

## R-002 — Teach Mode Simplification

**Decision**: Completely remove any custom Semicolon Mode UI, preview tables, lists, and toggles from Teach Mode.

**Rationale**: Since the semicolon splitting happens on load, the data is already structured into columns. The user can teach mappings using standard Teach Mode (Flex Pattern / Manual Rows). This simplifies the codebase and provides a unified, zero-special-cases user experience.

---

## R-003 — Standard Mapping Extension with Fixed Checkbox

**Decision**: Add a "Fixed mapping" checkbox to the standard column mapping panel in Teach Mode.

**Schema Property**:
- Extend the `ColumnMapping` JSON schema with a nullable `fixed` boolean field.
- Default to `false` (meaning the mapping is a data mapping and shifts across cavities for semicolon files).
- Saved as `"fixed": true` for label/static cells.

**Group Width Calculation**:
- During export, the system automatically groups data mappings targeting the same Excel row.
- `groupWidth` is computed as the total count of data mappings targeting that row.
- Mappings within the group are ordered by the column of their target cell (e.g. C3, D3).

---

## R-004 — Multi-Cavity Export Engine

**Decision**: `ExcelWriter` automatically applies cavity-block offsets for semicolon-delimited files.

**Algorithm**:
1. Partition the parsed text rows by `@101` boundaries into cavity blocks.
2. Group all `ColumnMapping` configurations by target Excel row.
3. For each cavity block `N` (0-indexed):
   - Apply each mapping to get the value for cavity `N`.
   - If `fixed: true`: Write to target cell once.
   - If `fixed: false`: Write to target cell shifted by `N × groupWidth` columns.
