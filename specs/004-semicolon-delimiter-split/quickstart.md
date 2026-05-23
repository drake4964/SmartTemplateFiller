# Quickstart: Semicolon Delimiter Column Split (Simplified)

**Branch**: `004-semicolon-delimiter-split` | **Date**: 2026-05-23

---

## What This Feature Does

Lets you load a CMM measurement file where data is separated by `;` and map each field into Excel — with fixed label columns and data columns that automatically expand rightward for each cavity block (`@101` boundary).

Splitting happens automatically when the file is loaded. Semicolon-specific modes are removed from Teach Mode, allowing you to use standard mapping configurations for all file types.

---

## How to Use (Teach Mode)

### Step 1 — Load the file
1. Open the application and click **Load TXT File**.
2. Select your `.txt` or `.asc` measurement file.
3. The main preview table immediately detects the semicolons and displays the split fields as standard columns (`Col 1`, `Col 2`, `Col 3`, etc.).

### Step 2 — Configure standard mappings in Teach Mode
1. Click **Teach Mode** to open the mapping editor.
2. In the standard mapping panel, select the source column, target cell, and direction.
3. For row labels or static fields (e.g. `Distance X` in Col 2):
   - **Target Cell**: `B3`
   - **Fixed mapping**: ✅ check the checkbox
4. For repeating measured values (e.g. nominal in Col 4):
   - **Target Cell**: `C3`
   - **Fixed mapping**: ☐ leave unchecked (Data mapping)
5. Click **+ Add Mapping**.

Repeat for each row you want to map.

### Step 3 — Save and export
1. Click **Save Mapping** to save to a JSON mapping file.
2. Go back to the main screen, click **Export to Excel**, select your text file and mapping JSON.
3. The output Excel file will contain:
   - Column B: fixed labels ("Distance X", "Distance Y", etc.)
   - Columns C, D, E, …: data values from each cavity block

---

## Expected Output Layout

For a 3-cavity file with 1 fixed + 2 data fields per row:

```
     B           C          D          E          F          G          H
3  Distance X  22.800     0.250      22.665     0.250      22.397     0.250
5  Distance Y  22.800     0.250      23.268     0.250      23.045     0.250
```

Where:
- Col B = fixed label (written once)
- Cols C–D = cavity 1 values (nominal, upper_tol)
- Cols E–F = cavity 2 values
- Cols G–H = cavity 3 values

---

## Running Mode (Folder Watch)

No extra configuration needed. Save your mapping JSON from Teach Mode, then configure Running Mode to use that mapping file. The export engine automatically detects that the text file is semicolon-delimited with `@101` boundaries and applies the cavity-aware export logic.
