# Data Model: Row Pattern Flex

## Entities

### `ColumnMapping`

The core mapping file representation, serialized as JSON via Jackson.

**Fields (New/Modified)**:
- `startField`: `Integer` (Optional, defaults to 1). The 1-indexed start offset.
- `fillField`: `Integer` (Optional, defaults to 1). How many rows to read.
- `spaceField`: `Integer` (Optional, defaults to 0). How many rows to skip.

**Legacy Fields**:
- `rowIndexes`: `List<Integer>`
- `patternType`: `String` (e.g., "odd", "even", "all")

**Validation Rules**:
- `startField >= 1`
- `fillField >= 1`
- `spaceField >= 0`

### `RowPatternDescriptor`

A stateless value object.

**State**:
- `start`: `int`
- `fill`: `int`
- `space`: `int`

**Methods**:
- `boolean shouldProcessSourceRow(int sourceRowIndex)`
- `boolean isExportablePath()`

### `MappingPathResolver`

Stateless static helper class to identify whether to use legacy mode or flex mode.

**Methods**:
- `static boolean shouldUseFlexPath(ColumnMapping mapping)` -> returns true if `mapping.getFillField() != null`.
