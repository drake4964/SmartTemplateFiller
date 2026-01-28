# Running Mode

## Overview

Running Mode provides automated file watching and conversion. It monitors a configured folder for incoming files and automatically converts them to Excel using a pre-configured mapping.

## Entry Point

- **Controller**: `RunningModeController`
- **View**: `running_mode.fxml`
- **Trigger**: "Running Mode" button in main window

## How It Works

1. Configure a **mapping file** (JSON from Teach Mode)
2. Set a **watch folder** to monitor
3. Set an **output folder** for converted files
4. Click **Start Watching**
5. Drop files into the watch folder - they're automatically processed

## Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                         Running Mode Flow                               │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   User                                                                  │
│     │                                                                   │
│     ▼                                                                   │
│   ┌──────────────────────┐                                             │
│   │ RunningModeController│ ◄── Loads/Saves config from RunningModeConfig│
│   └──────────┬───────────┘                                             │
│              │ Start Button                                             │
│              ▼                                                          │
│   ┌──────────────────────┐                                             │
│   │    FolderWatcher     │ ◄── Background ScheduledExecutorService     │
│   │  (every N seconds)   │                                             │
│   └──────────┬───────────┘                                             │
│              │ For each matching file                                   │
│              ▼                                                          │
│   ┌──────────────────────┐                                             │
│   │     ExcelWriter      │ ◄── Uses TxtParser internally               │
│   │writeAdvancedMappedFile│                                            │
│   └──────────┬───────────┘                                             │
│              │                                                          │
│              ▼                                                          │
│   Output Folder (timestamped)                                          │
│                                                                         │
└────────────────────────────────────────────────────────────────────────┘
```

## Output Structure

Files are organized by mapping name and timestamp:

```
Output Folder/
└── measurement/                    # Mapping name (derived from JSON filename)
    └── 2026-01-16_211501/          # Timestamp (yyyy-MM-dd_HHmmss)
        ├── data001.xlsx            # Converted output
        └── archive/
            └── data001.txt         # Original file (moved)
```

**Example**: If mapping file is `quality_check.json` and source is `report.txt`:
```
C:\Output\
└── quality_check\
    └── 2026-01-28_173045\
        ├── report.xlsx
        └── archive\
            └── report.txt
```

## Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| Mapping File | JSON file from Teach Mode | Required |
| Watch Folder | Folder to monitor for files | Required |
| Output Folder | Base folder for output | Required |
| File Pattern | Extensions to match | `*.txt,*.asc` |
| Scan Interval | How often to scan (seconds) | 1 |

## Behavior

- **Continuous running**: Keeps watching until explicitly stopped
- **Auto-archive**: Source files are **moved** to archive folder after processing
- **No file age limit**: All matching files are processed regardless of age
- **Duplicate prevention**: Files currently being processed are tracked to prevent re-processing
- **Configuration persistence**: Settings are saved and restored between sessions

## Configuration File Location

`{user.home}/.smarttemplatefiller/running_mode_config.json`

Example config content:
```json
{
  "mappingFile": "C:/mappings/quality_check.json",
  "watchFolder": "C:/incoming",
  "outputFolder": "C:/output",
  "filePattern": "*.txt,*.asc",
  "intervalSeconds": 1
}
```

## Classes Involved

| Class | Responsibility |
|-------|----------------|
| `RunningModeController` | UI management, lifecycle (start/stop), config persistence |
| `FolderWatcher` | Background scanning, file processing, archiving |
| `RunningModeConfig` | POJO for configuration, JSON persistence |
| `ExcelWriter` | Called for each file conversion |

## UI Components

| Component | Purpose |
|-----------| --------|
| `mappingFileField` | Path to JSON mapping file |
| `watchFolderField` | Folder to monitor for incoming files |
| `outputFolderField` | Base output folder |
| `filePatternField` | File extension pattern (e.g., `*.txt,*.asc`) |
| `intervalField` | Scan interval in seconds |
| `logArea` | Real-time processing log |
| `startButton` | Start folder watching |
| `stopButton` | Stop folder watching |
| `statusLabel` | Current status indicator |

## Error Handling

- **Invalid config**: Validation before start, user-friendly error messages
- **Processing errors**: Logged to logArea, file skipped, processing continues
- **Shutdown**: Graceful stop with 5-second timeout for running tasks

