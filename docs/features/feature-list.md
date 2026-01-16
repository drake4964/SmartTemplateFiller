# Feature List

## Overview

SmartTemplateFiller provides a focused set of features for data transformation from text files to Excel reports.

## Feature Matrix

| Feature | Status | Priority | Documentation |
|---------|--------|----------|---------------|
| File Parsing | Implemented | High | [feature-file-parsing.md](feature-file-parsing.md) |
| Teach Mode | Implemented | High | [feature-teach-mode.md](feature-teach-mode.md) |
| Excel Export | Implemented | High | [feature-excel-export.md](feature-excel-export.md) |
| Running Mode | Implemented | High | [feature-running-mode.md](feature-running-mode.md) |
| Excel Preview | Implemented | High | Included in Teach Mode |
| Mapping Persistence | Implemented | Medium | Included in Teach Mode |
| Drag-Drop Reorder | Implemented | Medium | Included in Teach Mode |
| Cell Reference Validation | Implemented | Medium | Included in Teach Mode |
| Last Directory Memory | Implemented | Medium | [feature-file-chooser.md](feature-file-chooser.md) |

## Feature Descriptions

### File Parsing
Automatically parse TXT/ASC files with intelligent format detection supporting three parsing strategies:
- Multi-line grouped block (CMM output)
- Fixed-column table
- Flat table (generic)

### Teach Mode
Interactive UI for configuring column-to-cell mappings with:
- **Excel-like preview** showing data as it will appear in output
- **Drag-and-drop reordering** of mappings
- Organized layout with TitledPane sections
- Input validation with clear error messages
- Move up/down buttons as keyboard fallback
- Clear all with confirmation prompt

### Excel Export
Generate XLSX files by applying mapping rules to parsed data rows:
- Supports columns beyond Z (AA, AB, etc.) using `CellReference`
- Both vertical and horizontal data directions
- Optional title rows
- Proper resource management with try-with-resources

### Excel Preview
Real-time TableView preview in Teach Mode:
- Column headers with Excel letters (A, B, C...)
- Row numbers matching Excel output
- Grid layout shows exact output structure
- Titles displayed in brackets
- Status bar shows preview range

### Mapping Persistence
Save and load mapping configurations as JSON files for reuse across sessions.

### Drag-Drop Reorder
Drag mappings in ListView to change order. Visual feedback during drag operation.

### Cell Reference Validation
Validates Excel cell references before adding mappings:
- Valid: A1, B2, AA123, ZZ999
- Invalid: 1A, A, 123

### Last Directory Memory
File dialogs automatically remember the last used directory within a session.

## Feature Roadmap

| Planned Feature | Priority | Status |
|-----------------|----------|--------|
| Template-based Excel export | Medium | Not started |
| Batch file processing | Low | Not started |
| Custom parsing rules | Low | Not started |
