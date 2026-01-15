# Architectural Decisions

## ADR-001: JavaFX for Desktop UI

**Status**: Accepted

**Context**: Need a cross-platform desktop GUI framework for Java.

**Decision**: Use JavaFX with FXML for UI definition.

**Rationale**:
- Native Java integration
- FXML separates UI from logic
- Rich control library (TableView, ComboBox, ListView)
- Cross-platform support

**Consequences**:
- Requires JavaFX module path configuration
- Limited to desktop deployment

---

## ADR-002: JSON for Mapping Configuration

**Status**: Accepted

**Context**: Need a format to persist column-to-cell mappings.

**Decision**: Use JSON files with Jackson for serialization.

**Rationale**:
- Human-readable and editable
- Easy to share between users
- Jackson provides robust serialization
- Flexible schema for future extensions

**Consequences**:
- No schema validation (relies on code)
- Users can manually edit (error-prone)

---

## ADR-003: Multi-Strategy Parser

**Status**: Accepted

**Context**: Input files come in multiple formats (CMM output, fixed-width, generic tables).

**Decision**: Implement auto-detection with three parsing strategies.

**Rationale**:
- Reduces user configuration burden
- Extensible for new formats
- Single entry point (`parseFile`)

**Consequences**:
- Detection logic may misidentify edge cases
- Adding new formats requires code changes

---

## ADR-004: Apache POI for Excel

**Status**: Accepted

**Context**: Need to generate XLSX files.

**Decision**: Use Apache POI (`XSSFWorkbook`) with `CellReference` for cell parsing.

**Rationale**:
- Industry-standard Java Excel library
- Full XLSX format support
- `CellReference` handles multi-letter columns (AA, AB, etc.)
- Active maintenance

**Consequences**:
- Large dependency footprint
- Memory usage scales with file size

---

## ADR-005: Row Pattern vs Explicit Indexes

**Status**: Accepted

**Context**: Users need to select which rows to export.

**Decision**: Support both pattern-based (odd/even/all) and explicit row indexes.

**Rationale**:
- Patterns cover common use cases (alternating data)
- Explicit indexes provide full flexibility
- Both stored in same mapping format

**Consequences**:
- UI complexity for two selection modes
- Mapping JSON has conditional structure

---

## ADR-006: FileChooserBuilder with Directory Memory

**Status**: Accepted (Added during enhancement phase)

**Context**: Users frequently work with files in the same directory.

**Decision**: Implement fluent `FileChooserBuilder` with static last-directory memory.

**Rationale**:
- Reduces repetitive navigation
- Fluent API improves code readability
- Static field persists across all dialogs in session

**Consequences**:
- Directory not persisted between sessions
- All dialogs share same directory state

---

## ADR-007: ListView for Mapping Management

**Status**: Accepted (Added during enhancement phase)

**Context**: Users needed to edit and reorder mappings, not just add/delete last.

**Decision**: Replace TextArea preview with selectable ListView and add move/delete controls.

**Rationale**:
- Enables selection of any mapping
- Move up/down controls ordering
- Delete selected is more intuitive than delete last
- Clear all with confirmation prevents accidents

**Consequences**:
- More complex UI
- Requires maintaining ListView in sync with data

---

## ADR-008: Drag-and-Drop for Mapping Reorder

**Status**: Accepted (Added during UX enhancement phase)

**Context**: Button-based move up/down was functional but cumbersome for reordering multiple items.

**Decision**: Implement drag-and-drop reordering in the mappings ListView.

**Rationale**:
- More intuitive for users
- Visual feedback during drag
- Faster for multi-position moves
- Keep buttons as keyboard-accessible fallback

**Consequences**:
- More complex ListCell implementation
- Custom DataFormat for drag data transfer
- Need to handle edge cases (drag to empty, drag to same position)

