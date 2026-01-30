# Excel Append Mode

## Overview
The **Excel Append Mode** allows you to add data to an existing Excel file rather than creating a new file for every operation. This is particularly useful for:
- Consolidating daily logs into a monthly report
- Accumulating data from multiple source files into a single master sheet
- Continuous data collection in Run Mode

## Key Features
- **Smart Appending**: Automatically detects the last used row in an Excel sheet and appends new data immediately after it.
- **Manual Export**: Choose between "Create New File" and "Append to Existing" during manual export.
- **Automated Run Mode**: Toggle "Append Mode" to continuously accumulate data from watched files into a single output file.
- **Row Limit Protection**: Warns you when the file approaches the Excel limit (1,048,576 rows).
- **Safe Handling**: Gracefully handles potential issues like locked files or file corruption.

## How to Use

### 1. Manual Export with Append
1. Load your source text file (`.txt`, `.asc`) as usual.
2. Click **Export to Excel**.
3. Select your **Mapping JSON** file.
4. A dialog will appear asking for the Export Mode:
   - **Create New File**: Standard behavior (creates a new `.xlsx` file).
   - **Append to Existing File**: Select this to add data to an existing sheet.
5. If you choose Append, browse and select the target Excel file.
6. The application will append the new data and display a summary of rows added.

### 2. Run Mode with Append
1. Open **Running Mode**.
2. Configure your specific inputs:
   - **Mapping File**: The JSON defining how to extract data.
   - **Watch Folder**: Where source text files will appear.
   - **Output Folder**: Where the Excel file will be saved.
3. Check the box **Append Mode (accumulate to single file)**.
4. Click **Start Watching**.
5. As new text files appear in the Watch Folder:
   - The first file creates a new Excel file (e.g., `output.xlsx`).
   - Subsequent files will have their data **appended** to that same `output.xlsx` file.
   - The log window will show "Appended X rows to output.xlsx".

## Technical Details

### Row Offset Calculation
The application calculates the starting position for the new data using the formula:
`Row Offset = Last Occupied Row Index + 1`

- If the sheet is empty, data starts at Row 1 (index 0).
- If the sheet has data up to Row 10, the new data starts at Row 11.
- **Note**: Headers/Titles defined in the mapping are **skipped** during append operations to prevent duplication.

### Limitations & Constraints
- **Excel Row Limit**: Excel files support a maximum of 1,048,576 rows. The application tracks usage and warns you when you are within 5% of this limit.
- **File Locking**: If the target Excel file is open in another application (e.g., Microsoft Excel), the append operation will fail. You will see an error message asking you to close the file.
- **Identical Mappings**: For best results, ensure you are appending data using the same (or compatible) mapping configuration used to create the original file.

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| **"Cannot access file" Error** | The target Excel file is open in another program. | Close the file in Excel and try again. |
| **"File appears corrupted"** | The target file may be damaged or not a valid `.xlsx`. | The app will offer to create a new file instead. Select "Yes" to start fresh. |
| **Data overlaps or gaps** | The mapping configuration might differ from the original. | Use the same mapping JSON for all files going into the same Excel sheet. |
