# Workflow Summary

## Overview

SmartTemplateFiller supports three primary user workflows for transforming measurement data into Excel reports.

## Workflow Catalog

| Workflow | Description | Sequence Diagram |
|----------|-------------|------------------|
| Data to Excel Export | Complete process from file load to export | [data-to-excel-sequence.md](data-to-excel-sequence.md) |
| Mapping Creation | Create and save mapping configuration | [mapping-creation-sequence.md](mapping-creation-sequence.md) |
| Mapping Reuse | Load existing mapping for new data | See Export workflow |

## Primary Workflows

### 1. Data to Excel Export (Main Flow)

**Goal**: Transform a TXT/ASC file into an Excel report

**Steps**:
1. Load TXT/ASC data file
2. Review data in table view
3. Enter Teach Mode to create mappings (or load existing)
4. Export to Excel using mapping rules

**Actors**: User, MainController, TxtParser, TeachModeController, ExcelWriter

---

### 2. Mapping Creation (Teaching Flow)

**Goal**: Create reusable column mappings

**Steps**:
1. Load data to view column structure
2. Open Teach Mode dialog
3. For each column:
   - Select source column
   - Define target cell and direction
   - Configure row pattern
4. Review preview
5. Save mapping to JSON file

**Actors**: User, TeachModeController, File System

---

### 3. Batch Export (Reuse Flow)

**Goal**: Export multiple data files using same mapping

**Steps**:
1. Click "Export to Excel"
2. Select source TXT file
3. Select existing mapping JSON
4. Choose output location
5. Export complete

**Actors**: User, MainController, ExcelWriter

## Workflow Decision Tree

```
Start
  │
  ├─▶ Have mapping file?
  │         │
  │    Yes  │  No
  │         │   │
  │         ▼   ▼
  │    ┌────────────────┐
  │    │ Load Data File │
  │    └────────┬───────┘
  │             │
  │         ┌───┴───┐
  │    Yes  │       │  No
  │    ◄────┤ Teach │────►
  │         │ Mode? │
  │         └───────┘
  │                │
  │    ┌───────────┘
  │    ▼
  │ Create Mappings
  │    │
  │    ▼
  │ Save Mapping
  │    │
  └────┴────────────────────▶ Export to Excel
```
