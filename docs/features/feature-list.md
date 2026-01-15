# Feature List

## Overview

SmartTemplateFiller provides a focused set of features for data transformation from text files to Excel reports.

## Feature Matrix

| Feature | Status | Priority | Documentation |
|---------|--------|----------|---------------|
| File Parsing | ✅ Implemented | High | [feature-file-parsing.md](feature-file-parsing.md) |
| Teach Mode | ✅ Implemented | High | [feature-teach-mode.md](feature-teach-mode.md) |
| Excel Export | ✅ Implemented | High | [feature-excel-export.md](feature-excel-export.md) |
| Mapping Persistence | ✅ Implemented | Medium | Included in Teach Mode |
| Data Preview | ✅ Implemented | Medium | Included in Teach Mode |
| Cell Reference Validation | ✅ Implemented | Medium | Included in Teach Mode |
| Last Directory Memory | ✅ Implemented | Medium | [feature-file-chooser.md](feature-file-chooser.md) |
| Mapping Reordering | ✅ Implemented | Medium | Included in Teach Mode |

## Feature Descriptions

### File Parsing
Automatically parse TXT/ASC files with intelligent format detection supporting three parsing strategies.

### Teach Mode
Interactive UI for configuring column-to-cell mappings with live preview of mapped values. Includes:
- **Drag-and-drop reordering** of mappings
- TitledPane sections for organized layout
- ListView with visual drag feedback
- Enhanced Smart Preview with headers and value limits
- Input validation with clear error messages
- Move up/down buttons as fallback for keyboard users
- Clear all with confirmation prompt


### Excel Export
Generate XLSX files by applying mapping rules to parsed data rows. Supports columns beyond Z (AA, AB, etc.).

### Mapping Persistence
Save and load mapping configurations as JSON files for reuse across sessions.

### Data Preview
Real-time preview of mapped data before export in the Teach Mode interface.

### Cell Reference Validation
Validates Excel cell references (e.g., A1, B2, AA123) before adding mappings.

### Last Directory Memory
File dialogs automatically remember the last used directory within a session.

### Mapping Reordering
Move mappings up/down in the list to control output order.

## Feature Roadmap

| Planned Feature | Priority | Status |
|-----------------|----------|--------|
| Template-based Excel export | Medium | Not started |
| Batch file processing | Low | Not started |
| Custom parsing rules | Low | Not started |

