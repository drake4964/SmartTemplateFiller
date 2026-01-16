# Running Mode

## Overview

Running Mode provides automated file watching and conversion. It monitors a configured folder for incoming files and automatically converts them to Excel using a pre-configured mapping.

## How It Works

1. Configure a **mapping file** (JSON from Teach Mode)
2. Set a **watch folder** to monitor
3. Set an **output folder** for converted files
4. Click **Start Watching**
5. Drop files into the watch folder - they're automatically processed

## Output Structure

Files are organized by mapping name and timestamp:

```
Output Folder/
└── measurement/                    # Mapping name
    └── 2026-01-16_211501/          # Timestamp
        ├── data001.xlsx            # Converted output
        └── archive/
            └── data001.txt         # Original file (moved)
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
- **Configuration persistence**: Settings are saved and restored between sessions

## Configuration File Location

`{user.home}/.smarttemplatefiller/running_mode_config.json`
