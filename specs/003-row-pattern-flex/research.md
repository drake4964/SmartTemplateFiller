# Research: Row Pattern Flex

## Needs Clarification Resolution

All constraints were adequately detailed in the feature specification, Constitution (`.specify/memory/constitution.md`), and previous context. No `NEEDS CLARIFICATION` points were left unresolved.

## Technical Decisions

### 1. UI Reactivity

*   **Decision**: Use JavaFX bindings (`Bindings` and `ChangeListener`) combined with a custom debouncing structure to satisfy the < 200ms UI update latency (FR-009).
*   **Rationale**: The user typing rapidly in the numeric fields shouldn't trigger expensive Excel cell string generations for each keystroke; standardizing around a 50-100ms debounce achieves the < 200ms constraint.
*   **Alternatives considered**: Immediate non-debounced refresh (risk of jank if calculating UI addresses takes time on a very slow machine).

### 2. Backward Compatibility

*   **Decision**: Implement `MappingPathResolver` using standard conditionals that check if `fillField` is non-null/present. If so, logic ignores `rowIndexes`.
*   **Rationale**: Follows the explicit FR-007 boundary and simplifies Jackson deserialization.
*   **Alternatives considered**: Version numbering JSON (JSON Schema Version), but the current `ColumnMapping` implementation inherently supports null optional checks and we don't want to enforce a global version migration.

### 3. Model Isolation

*   **Decision**: `RowPatternDescriptor` will function as a pure Value Object mapping `Stream<Pair<Integer, Boolean>>`.
*   **Rationale**: Separates index selection logic completely from export POI code, maintaining *Modular Design* (Constitution Principle II).
