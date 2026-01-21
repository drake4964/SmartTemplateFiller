# Multi-File Merge - Remaining Implementation Guide

## Overview

This guide covers the remaining 15 tasks to complete the Multi-File Merge feature implementation.

## Completed ✅
- All model classes (Phase 1)
- Core services: MultiFileMergeService, FolderWatchService, ArchiveService (Phase 2-3)
- Tests for core services
- Documentation

## Remaining Tasks

### Priority 1: Core Integration (T023)
**T023: Integrate MultiFileMergeService with ExcelExporter**

The existing code uses direct POI calls. We need to:
1. Update ExcelWriter.java to support multi-file input
2. OR create a wrapper that uses MultiFileMergeService

**Recommended Approach:**
- Keep existing single-file ExcelWriter for backward compatibility
- Create MultiFileExcelWriter that uses MultiFileMergeService
- Update MainController to detect multi-file mode

### Priority 2: Teaching Mode Multi-File UI (T024-T032)

**Current State Analysis:**
- TeachModeController uses simple List<Map<String, Object>> for mappings
- No concept of source file slots
- Saves to simple JSON array format

**Required Changes:**

1. **T024: Add File Slot Dropdown**
   - Add ComboBox for selecting source file (1-10)
   - Default to slot 1 for backward compatibility
   
2. **T025-T026: Display File Source in Mapping**
   - Update mapping display format to "File1:ColA → A1"
   - Modify updateMappingListView() method

3. **T027-T028: Extended JSON Support**
   - Migrate from List<Map> to MappingConfiguration object
   - Save with schemaVersion: "2.0"
   - Load with backward compatibility via MappingUpgrader

4. **T029-T030: Multiple File Loading**
   - Add "Load Files" button to load 2-10 input files
   - Show file list with slot assignments
   - Validate 2-10 file limit

**UI Changes Needed:**
```xml
<!-- Add to teach_mode.fxml -->
<ComboBox fx:id="fileSlotComboBox" promptText="Source File"/>
<VBox fx:id="loadedFilesPanel">
    <Label text="Loaded Files:"/>
    <ListView fx:id="fileListView" prefHeight="100"/>
</VBox>
```

### Priority 3: Run Mode Folder Watching UI (T041-T046)

**Integration Points:**
1. **T041-T045: RunModeController Updates**
   - Replace existing FolderWatcher with new FolderWatchService
   - Add FileStatusIndicator components (already created)
   - Add "Configure Watch Folders" UI for multiple folder setup
   - Implement Start/Stop watching toggle

2. **T046: FXML Layout Updates**
   - Add folder status indicators panel
   - Add configuration for file matching strategy
   - Add stability check duration spinner

### Priority 4: Constitution Verification (T050-T056)

**Verification Checklist:**

- [ ] **T050: UI Feedback** - All operations show loading states
- [ ] **T051: Module Independence** - No circular dependencies
- [ ] **T052: JSON Versioning** - schemaVersion in all outputs
- [ ] **T053: Test Coverage** - Run jacocoTestReport
- [ ] **T056: Security** - No sensitive data logged, paths validated

## Implementation Strategy

### Phase A: Core Integration (1-2 hours)
1. Create MultiFileExcelWriter wrapper
2. Update MainController to detect multi-file mappings
3. Test with existing multi-file services

### Phase B: Teaching Mode (2-4 hours)
1. Add file slot dropdown to UI
2. Implement multi-file loading
3. Update JSON save/load to use MappingConfiguration
4. Update mapping display format

### Phase C: Run Mode Watch UI (2-3 hours)
1. Replace FolderWatcher with FolderWatchService
2. Add FileStatusIndicator components
3. Wire up folder configuration UI
4. Test auto-processing workflow

### Phase D: Verification (1 hour)
1. Run test suite
2. Verify constitution compliance
3. Update documentation

## Quick Start Commands

```bash
# Compile
gradle compileJava

# Run tests
gradle test

# Run application
gradle run

# Generate coverage report
gradle jacocoTestReport
```

## Key Files to Modify

1. **ExcelWriter.java** - Add multi-file support
2. **TeachModeController.java** - Add file slot UI
3. **RunModeController.java** - Integrate FolderWatchService
4. **MainController.java** - Detect multi-file mode

## Notes

- Maintain backward compatibility with v1.0 JSON format
- File slot 1 is default for single-file mode
- FolderWatchService is session-only (no persistence)
- Archive timestamp format is configurable
