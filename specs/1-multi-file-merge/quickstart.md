# Quickstart: Multi-File Merge Export

**Feature**: 1-multi-file-merge

## Overview

This feature allows combining 2-10 input TXT/ASC files into a single Excel output, with automatic folder watching for production use.

## Getting Started

### 1. Multi-File Merge (Manual Mode)

1. Launch SmartTemplateFiller
2. Click **"Load TXT"** and select multiple files (2-10)
3. Click **"Teach Mode"**
4. For each mapping:
   - Select **Source File** from dropdown (File 1, File 2, etc.)
   - Select **Source Column** (A, B, C...)
   - Enter **Target Cell** (e.g., A1, B5)
   - Choose **Direction** (Vertical/Horizontal)
   - Click **Add Mapping**
5. Click **"Save Mapping"** to export as JSON
6. Click **"Export to Excel"** to generate output

### 2. Multi-File Template JSON

Create a mapping JSON that references multiple files:

```json
{
  "schemaVersion": "2.0",
  "mappings": [
    {
      "sourceFileSlot": 1,
      "sourceColumn": "A",
      "targetCell": "A1",
      "direction": "VERTICAL"
    },
    {
      "sourceFileSlot": 2,
      "sourceColumn": "B",
      "targetCell": "B1",
      "direction": "VERTICAL"
    }
  ],
  "fileSlots": [
    { "slot": 1, "description": "Machine 1 Data" },
    { "slot": 2, "description": "Machine 2 Data" }
  ]
}
```

### 3. Folder Watching (Automated Mode)

1. Click **"Running Mode"**
2. Load a multi-file mapping JSON
3. Configure watched folders:
   - Click **"Add Watch Folder"**
   - Select folder for File Slot 1
   - Repeat for each file slot
4. Click **"Start Watching"**
5. When files with matching prefixes appear in all folders, processing starts automatically

### File Matching Rules

Files are matched across folders by **prefix** (text before first underscore):

| Folder 1 | Folder 2 | Match? |
|----------|----------|--------|
| `PART001_001.txt` | `PART001_002.txt` | ✅ Yes (prefix: PART001) |
| `REPORT.txt` | `REPORT.txt` | ✅ Yes (exact basename) |
| `JOB_A.txt` | `JOB_B.txt` | ❌ No (different prefix) |

### Archive Structure

After processing, files are archived:

```text
output/
└── 2026-01-17_143022/
    ├── PART001.xlsx           # Generated output
    └── inputs/
        ├── PART001_001.txt    # Archived source 1
        └── PART001_002.txt    # Archived source 2
```

## Common Scenarios

### Scenario 1: Two CMM Machines

Two measurement machines output files to separate folders. Combine into one report.

**Setup**:
- Watch Folder 1: `C:\CMM\Machine1\output`
- Watch Folder 2: `C:\CMM\Machine2\output`
- Output Folder: `C:\Reports`

**Files**:
- Machine1: `PART_XYZ_001.txt`
- Machine2: `PART_XYZ_002.txt`

**Result**: When both files exist, generates `PART.xlsx` with combined data.

### Scenario 2: Before/After Comparison

Compare initial and final measurements in one report.

**Mapping**:
- File 1 (Initial): Column A → Output Column A
- File 2 (Final): Column A → Output Column B
- Difference calculated in Excel formula
