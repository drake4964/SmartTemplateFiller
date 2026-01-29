# Quickstart: Excel Append Mode

**Feature**: 002-excel-append-mode

## Manual Export with Append

1. Open SmartTemplateFiller
2. Click **Export to Excel**
3. Select source TXT file
4. Select mapping JSON file
5. **NEW**: Choose export mode:
   - **Create New File** (default): Creates new Excel file
   - **Append to Existing**: Opens file browser to select existing Excel
6. Complete export

## Run Mode with Append

1. Open SmartTemplateFiller → **Running Mode**
2. Configure:
   - Mapping file
   - Watch folder
   - Output folder
3. **NEW**: Check **Append Mode** checkbox
4. Click **Start Watching**
5. First file: Creates new Excel file
6. Subsequent files: Appends to that file
7. Stop/restart: Prompts whether to continue appending or start fresh

## How Append Works

- Reuses the same mapping.json
- Calculates row offset from last occupied row
- Applies offset to all target cell positions
- Example: If mapping writes to A1,A2,A3 and file has 3 rows, new data writes to A4,A5,A6
