# Assumptions

## Analysis Assumptions

The following assumptions were made during project analysis and documentation generation.

### A-001: Target User Domain
**Assumption**: Primary users are quality control engineers working with CMM measurement data.  
**Basis**: File format examples include CMM output patterns (`Circle (ID:1)`, coordinate data).  
**Impact**: Documentation uses domain terminology (measurement, tolerances).

### A-002: Single User Application
**Assumption**: Application is used by a single user on a local machine.  
**Basis**: No multi-user features, local file system access only.  
**Impact**: No concurrency or access control considerations.

### A-003: Excel as Final Output
**Assumption**: Excel is the required output format for downstream use.  
**Basis**: Only XLSX export is implemented.  
**Impact**: No alternative export formats documented.

### A-004: Mapping Reusability
**Assumption**: Users create mappings once and reuse them for multiple files.  
**Basis**: Mapping save/load functionality and separation of teach mode from export.  
**Impact**: Documentation emphasizes mapping persistence workflow.

### A-005: Column Config Location
**Assumption**: `column_config.json` is expected in the working directory.  
**Basis**: Code loads from `new File("column_config.json")` with no path configuration.  
**Impact**: Users must place config in application root.

### A-006: FileChooserBuilder Now Used
**Assumption**: ~~`FileChooserBuilder` is unused.~~  
**Status**: **RESOLVED** - FileChooserBuilder is now used in MainController for all file dialogs.  
**Impact**: Provides fluent API and last-directory memory.

### A-007: Template Support
**Assumption**: Current version creates new Excel files, not from templates.  
**Basis**: `ExcelWriter` creates `new XSSFWorkbook()` rather than loading existing file.  
**Impact**: Template-based export noted as potential future feature.

## Bug Fixes Applied

The following issues were identified and fixed during code review:

| ID | Issue | Resolution |
|----|-------|------------|
| BUG-001 | Cell refs limited to A-Z | Use `CellReference` class |
| BUG-002 | Negative row crash at row 1 | Boundary check for title row |
| BUG-003 | NumberFormatException on invalid input | Input validation + try-catch |
| BUG-004 | NullPointerException in preview | Null check before casting |
| BUG-005 | ClassCastException (Integer vs Long) | Use `Number.intValue()` |
| BUG-006 | Column index off-by-one | Skip Row column in loop |
| BUG-007 | Resource leak in ExcelWriter | Use try-with-resources |
| BUG-008 | Success alert shows as ERROR | Added `showInfo()` method |

## Enhancements Applied

| ID | Enhancement | Implementation |
|----|-------------|----------------|
| ENH-001 | Use FileChooserBuilder | Refactored MainController |
| ENH-002 | Cell reference validation | Regex validation in TeachModeController |
| ENH-003 | Last directory memory | Static field in FileChooserBuilder |
| ENH-004 | Mapping management | ListView with move/delete/clear |

## Documentation Methodology

- Code was analyzed statically; application was not executed
- All class relationships inferred from import statements and method calls
- Diagrams generated based on code structure, not runtime behavior
- Documentation updated after bug fixes and enhancements
