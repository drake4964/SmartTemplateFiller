# Row Pattern Flex Configuration

The "Row Pattern Flex" feature gives you complete control over how data from your source files (like CMM/measurement logs) is exported into your Excel templates. It replaces the older, rigid "all / odd / even" settings with three simple numerical fields.

## The Fields

When mapping a column in Teach Mode, you will see a **Flex Pattern** configuration area instead of the old option buttons. It requires the following inputs:

*   **Start**: The first row (1-based index) in the source data you want to export. This is perfect for skipping unwanted preamble headers. (Minimum: `1`)
*   **Fill**: How many consecutive rows to read and export. (Minimum: `1`)
*   **Space**: How many rows to skip before restarting the fill. (Minimum: `0`)

### Example Use Cases:
1. **Export Everything**: Start=1, Fill=1, Space=0.
2. **Export Odd Rows Only**: Start=1, Fill=1, Space=1.
3. **Export Even Rows Only**: Start=2, Fill=1, Space=1.
4. **Export Blocks**: Imagine a source file with 3 lines of data followed by 1 blank line. To correctly map this, you would set `Fill=3` and `Space=1`.

## Live Preview
As you adjust these values, the application provides a **Live Preview** showing exactly which source rows will be pasted into which Excel cells, for the first 10 outputs. If you select a target cell (e.g., `B5`), the preview will begin at `B5` and flow in the specified export direction (Horizontal or Vertical). 

If a source row doesn't have data at your selected column index, it will still show the expected output cell map, providing confidence before you hit "Export".

## Backward Compatibility
Don't worry about old configurations! Any `.json` mappings you created before we introduced Row Pattern Flex will still perfectly load and work as expected.
