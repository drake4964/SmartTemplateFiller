# Research: Multi-File Merge Export

**Feature**: 1-multi-file-merge
**Date**: 2026-01-17

## Research Topics

### 1. Java WatchService for Folder Monitoring

**Decision**: Use `java.nio.file.WatchService` for folder watching

**Rationale**:
- Built into JDK 7+, no external dependencies needed
- Event-driven API (no polling overhead)
- Supports ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE events
- Works cross-platform (Windows, macOS, Linux)

**Alternatives Considered**:
- Apache Commons IO `FileAlterationMonitor`: More features but external dependency
- Spring Integration File: Overkill for desktop app
- Manual polling with `File.lastModified()`: Inefficient, high CPU usage

**Implementation Notes**:
- Use separate thread for watching
- Register for ENTRY_CREATE and ENTRY_MODIFY events
- Apply stability check after event received

### 2. File Stability Detection Pattern

**Decision**: Size-based stability check with configurable delay (default 2 seconds)

**Rationale**:
- Simple to implement
- Works for most file copy scenarios
- Delay is user-configurable

**Algorithm**:
```text
1. On file event, record file size
2. Wait N seconds (configurable)
3. Check file size again
4. If size unchanged AND file not locked → file is stable
5. If size changed → restart stability check
```

**Alternatives Considered**:
- File locking check only: May miss slow network copies
- Hash comparison: Expensive for large files
- Process-based detection: Platform-specific

### 3. File Matching Strategy

**Decision**: Prefix-based matching (text before first underscore) OR exact basename

**Rationale**:
- Matches common CMM file naming conventions (e.g., PART001_001.txt, PART001_002.txt)
- Simple regex: `^([^_]+)` to extract prefix
- Fallback to exact basename for files without underscores

**Algorithm**:
```text
1. For each file in watched folders:
   a. Extract prefix = text before first underscore
   b. If no underscore, prefix = full basename (without extension)
2. Group files by prefix
3. When all folders have file with same prefix → trigger processing
```

### 4. JSON Schema Versioning

**Decision**: Add `"schemaVersion": "2.0"` field to mapping JSON

**Rationale**:
- Backward compatible (missing version → assume 1.0)
- Clear migration path for future changes
- Simple version comparison

**Schema v2.0 Changes**:
```json
{
  "schemaVersion": "2.0",
  "mappings": [
    {
      "sourceFile": 1,          // NEW: file slot (1-10)
      "sourceColumn": "A",
      "targetCell": "A1",
      "direction": "vertical"
    }
  ],
  "fileSlots": [               // NEW: file configuration
    { "slot": 1, "description": "Machine 1 data" },
    { "slot": 2, "description": "Machine 2 data" }
  ]
}
```

### 5. Archive Folder Structure

**Decision**: Timestamped subfolder in output directory with configurable format

**Rationale**:
- Keeps input files with their corresponding output
- Easy traceability and audit
- Date/datetime format is user preference

**Structure**:
```text
output/
├── 2026-01-17/                    # Date-only format
│   ├── PART001.xlsx               # Output file
│   ├── inputs/
│   │   ├── PART001_001.txt        # Archived source file 1
│   │   └── PART001_002.txt        # Archived source file 2

output/
├── 2026-01-17_143022/             # Date+time format
│   └── ...
```

## Open Questions (Resolved)

All questions resolved via `/speckit-clarify`:

| Question | Resolution |
|----------|------------|
| File matching strategy | Prefix-based or exact basename |
| Stability check duration | Configurable, default 2 seconds |
| Merge approach | Flexible row/column/mixed per template |
| Post-processing | Archive together in output folder |
| Watch persistence | Session only |
