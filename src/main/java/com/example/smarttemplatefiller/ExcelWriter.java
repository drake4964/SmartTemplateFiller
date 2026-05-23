package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.engine.MappingPathResolver;
import com.example.smarttemplatefiller.mapping.RowPatternDescriptor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    public static void writeAdvancedMappedFile(File txtFile, File mappingFile, File outputFile) {
        try {
            List<List<String>> data = TxtParser.parseFile(txtFile);

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> mappings = mapper.readValue(
                    mappingFile,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Result");

                boolean hasSemicolon = data.stream().anyMatch(row -> row.size() > 1);
                boolean hasAt101 = data.stream().anyMatch(row -> !row.isEmpty() && "@101".equals(row.get(0).trim()));
                boolean isSemicolonFile = hasSemicolon && hasAt101;

                if (isSemicolonFile) {
                    writeSemicolonData(data, mappings, sheet, 0);
                } else {
                    for (Map<String, Object> mapping : mappings) {
                        int sourceColumn = ((Number) mapping.get("sourceColumn")).intValue();
                        String startCell = (String) mapping.get("startCell");
                        String direction = (String) mapping.get("direction");
                        String title = mapping.containsKey("title") ? mapping.get("title").toString() : "";

                        // BUG-001 FIX: Use CellReference for proper parsing (supports AA, AB, etc.)
                        CellReference ref = new CellReference(startCell);
                        int startRow = ref.getRow();
                        int startCol = ref.getCol();

                        // BUG-002 FIX: Only write title if row > 0 and title is not empty
                        if (!title.isEmpty()) {
                            if (direction.equals("vertical")) {
                                if (startRow > 0) {
                                    Row titleRow = sheet.getRow(startRow - 1);
                                    if (titleRow == null)
                                        titleRow = sheet.createRow(startRow - 1);
                                    Cell cell = titleRow.createCell(startCol);
                                    cell.setCellValue(title);
                                }
                            } else {
                                Row row = sheet.getRow(startRow);
                                if (row == null)
                                    row = sheet.createRow(startRow);
                                Cell titleCell = row.createCell(startCol);
                                titleCell.setCellValue(title);
                            }
                        }

                        // T015 [US2]: Determine row indexes — flex path takes priority over legacy paths
                        List<Integer> rowIndexes = new ArrayList<>();
                        if (MappingPathResolver.shouldUseFlexPath(mapping)) {
                            // Flex path: build RowPatternDescriptor and stream output→source pairs
                            int startField = (mapping.containsKey("startField") && mapping.get("startField") != null)
                                    ? ((Number) mapping.get("startField")).intValue() : 1;
                            int fillField  = ((Number) mapping.get("fillField")).intValue();
                            int spaceField = (mapping.containsKey("spaceField") && mapping.get("spaceField") != null)
                                    ? ((Number) mapping.get("spaceField")).intValue() : 0;

                            RowPatternDescriptor descriptor = new RowPatternDescriptor(startField, fillField, spaceField);

                            // Capture loop-locals for lambda
                            final Sheet fSheet    = sheet;
                            final int   fStartRow = startRow;
                            final int   fStartCol = startCol;
                            final int   fSrcCol   = sourceColumn;
                            final String fDir     = direction;

                            descriptor.generateOutputSequence(data.size()).forEach(entry -> {
                                int outputPos    = entry.getKey();
                                int srcRowIndex  = entry.getValue();
                                List<String> rowData = data.get(srcRowIndex);
                                String value = (fSrcCol < rowData.size()) ? rowData.get(fSrcCol) : "";

                                if ("vertical".equals(fDir)) {
                                    Row excelRow = fSheet.getRow(fStartRow + outputPos);
                                    if (excelRow == null) excelRow = fSheet.createRow(fStartRow + outputPos);
                                    excelRow.createCell(fStartCol).setCellValue(value);
                                } else {
                                    Row excelRow = fSheet.getRow(fStartRow);
                                    if (excelRow == null) excelRow = fSheet.createRow(fStartRow);
                                    excelRow.createCell(fStartCol + outputPos + 1).setCellValue(value);
                                }
                            });
                            continue; // flex path handled — skip legacy rowIndexes write loop below
                        } else if (mapping.containsKey("rowPattern")) {
                            Map<String, Object> rowPattern = (Map<String, Object>) mapping.get("rowPattern");
                            // BUG-005 FIX: Use Number.intValue() for safe casting
                            int start = ((Number) rowPattern.get("start")).intValue();
                            String type = (String) rowPattern.get("type");
                            rowIndexes = TxtParser.generateIndexes(data.size(), type, start);
                        } else if (mapping.containsKey("rowIndexes")) {
                            List<Object> rawList = (List<Object>) mapping.get("rowIndexes");
                            for (Object obj : rawList) {
                                rowIndexes.add(((Number) obj).intValue());
                            }
                        }

                        // Write data
                        for (int i = 0; i < rowIndexes.size(); i++) {
                            int rowIndex = rowIndexes.get(i);
                            if (rowIndex >= data.size() || rowIndex < 0)
                                continue;
                            List<String> rowData = data.get(rowIndex);
                            String value = (sourceColumn < rowData.size()) ? rowData.get(sourceColumn) : "";

                            if (direction.equals("vertical")) {
                                Row row = sheet.getRow(startRow + i);
                                if (row == null)
                                    row = sheet.createRow(startRow + i);
                                row.createCell(startCol).setCellValue(value);
                            } else {
                                Row row = sheet.getRow(startRow);
                                if (row == null)
                                    row = sheet.createRow(startRow);
                                row.createCell(startCol + i + 1).setCellValue(value);
                            }
                        }
                    }
                }

                // BUG-007 FIX: Use try-with-resources for FileOutputStream
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    workbook.write(out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate row offset from existing Excel sheet.
     * Returns 0 if sheet is empty, otherwise returns lastRowNum + 1.
     * 
     * @param sheet The Excel sheet to check
     * @return The row offset to use for appending
     */
    private static int calculateRowOffset(Sheet sheet) {
        if (sheet == null) {
            return 0;
        }
        int lastRowNum = sheet.getLastRowNum();
        // getLastRowNum() returns 0 for both empty sheets and sheets with one row
        // Check if row 0 actually exists
        if (lastRowNum == 0 && sheet.getRow(0) == null) {
            return 0;
        }
        return lastRowNum + 1;
    }

    /**
     * Append data from source file to existing Excel file using mapping.
     * Calculates row offset from last occupied row in the target file.
     * 
     * @param txtFile           Source text file
     * @param mappingFile       Mapping JSON file
     * @param existingExcelFile Target Excel file to append to
     * @return AppendResult with operation details
     */
    public static AppendResult appendToMappedFile(File txtFile, File mappingFile, File existingExcelFile) {
        List<String> warnings = new ArrayList<>();

        try {
            // Validate inputs
            if (!existingExcelFile.exists()) {
                return AppendResult.failure("Target file does not exist: " + existingExcelFile.getName());
            }

            // Parse source data
            List<List<String>> data = TxtParser.parseFile(txtFile);
            if (data.isEmpty()) {
                return AppendResult.successWithWarnings(0, 0, existingExcelFile.getAbsolutePath(),
                        List.of("Source file contains no data rows"));
            }

            // Load mappings
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> mappings = mapper.readValue(
                    mappingFile,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            // Open existing workbook
            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(existingExcelFile)) {
                workbook = new XSSFWorkbook(fis);
            }

            try {
                // Get or create the Result sheet
                Sheet sheet = workbook.getSheet("Result");
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                }
                if (sheet == null) {
                    sheet = workbook.createSheet("Result");
                }

                // Calculate row offset
                int rowOffset = calculateRowOffset(sheet);
                int rowsAdded = 0;

                // Check for Excel row limit (1,048,576 rows)
                final int EXCEL_ROW_LIMIT = 1048576;
                final int WARNING_THRESHOLD = (int) (EXCEL_ROW_LIMIT * 0.95); // 5% of limit

                boolean hasSemicolon = data.stream().anyMatch(row -> row.size() > 1);
                boolean hasAt101 = data.stream().anyMatch(row -> !row.isEmpty() && "@101".equals(row.get(0).trim()));
                boolean isSemicolonFile = hasSemicolon && hasAt101;

                if (isSemicolonFile) {
                    writeSemicolonData(data, mappings, sheet, rowOffset);
                    int maxRows = 0;
                    for (Map<String, Object> mapping : mappings) {
                        int mRows = 0;
                        String direction = (String) mapping.get("direction");
                        if ("vertical".equals(direction)) {
                            if (MappingPathResolver.shouldUseFlexPath(mapping)) {
                                mRows = (int) new RowPatternDescriptor(
                                    (mapping.containsKey("startField") && mapping.get("startField") != null) ? ((Number) mapping.get("startField")).intValue() : 1,
                                    ((Number) mapping.get("fillField")).intValue(),
                                    (mapping.containsKey("spaceField") && mapping.get("spaceField") != null) ? ((Number) mapping.get("spaceField")).intValue() : 0
                                ).generateOutputSequence(data.size()).count();
                            } else if (mapping.containsKey("rowPattern")) {
                                Map<String, Object> rowPattern = (Map<String, Object>) mapping.get("rowPattern");
                                int start = ((Number) rowPattern.get("start")).intValue();
                                String type = (String) rowPattern.get("type");
                                mRows = TxtParser.generateIndexes(data.size(), type, start).size();
                            } else if (mapping.containsKey("rowIndexes")) {
                                List<Object> rawList = (List<Object>) mapping.get("rowIndexes");
                                mRows = rawList.size();
                            }
                        } else {
                            mRows = 1;
                        }
                        if (mRows > maxRows) maxRows = mRows;
                    }
                    rowsAdded = maxRows;
                } else {
                    // Apply mappings with offset
                    for (Map<String, Object> mapping : mappings) {
                        int sourceColumn = ((Number) mapping.get("sourceColumn")).intValue();
                        String startCell = (String) mapping.get("startCell");
                        String direction = (String) mapping.get("direction");

                        CellReference ref = new CellReference(startCell);
                        int startRow = ref.getRow();
                        int startCol = ref.getCol();

                        // T015 [US2]: Determine row indexes — flex path takes priority over legacy paths
                        List<Integer> rowIndexes = new ArrayList<>();
                        if (MappingPathResolver.shouldUseFlexPath(mapping)) {
                            // Flex path: build RowPatternDescriptor and stream output→source pairs
                            int startField = (mapping.containsKey("startField") && mapping.get("startField") != null)
                                    ? ((Number) mapping.get("startField")).intValue() : 1;
                            int fillField  = ((Number) mapping.get("fillField")).intValue();
                            int spaceField = (mapping.containsKey("spaceField") && mapping.get("spaceField") != null)
                                    ? ((Number) mapping.get("spaceField")).intValue() : 0;

                            RowPatternDescriptor descriptor = new RowPatternDescriptor(startField, fillField, spaceField);

                            // Capture loop-locals for lambda (rowOffset is effectively final here)
                            final Sheet   fSheet    = sheet;
                            final int     fStartRow = startRow;
                            final int     fStartCol = startCol;
                            final int     fSrcCol   = sourceColumn;
                            final String  fDir      = direction;
                            final int     fOffset   = rowOffset;
                            final int[]   rowsAddedHolder = {rowsAdded};

                            descriptor.generateOutputSequence(data.size()).forEach(entry -> {
                                int outputPos   = entry.getKey();
                                int srcRowIndex = entry.getValue();
                                List<String> rowData = data.get(srcRowIndex);
                                String value = (fSrcCol < rowData.size()) ? rowData.get(fSrcCol) : "";

                                if ("vertical".equals(fDir)) {
                                    int targetRow = Math.max(fStartRow, fOffset) + outputPos;
                                    if (targetRow < EXCEL_ROW_LIMIT) {
                                        Row excelRow = fSheet.getRow(targetRow);
                                        if (excelRow == null) excelRow = fSheet.createRow(targetRow);
                                        excelRow.createCell(fStartCol).setCellValue(value);
                                        rowsAddedHolder[0] = Math.max(rowsAddedHolder[0], outputPos + 1);
                                    }
                                } else {
                                    int targetRow = Math.max(fStartRow, fOffset);
                                    Row excelRow = fSheet.getRow(targetRow);
                                    if (excelRow == null) excelRow = fSheet.createRow(targetRow);
                                    excelRow.createCell(fStartCol + outputPos + 1).setCellValue(value);
                                    rowsAddedHolder[0] = 1;
                                }
                            });
                            rowsAdded = rowsAddedHolder[0];
                            continue; // flex path handled — skip legacy rowIndexes write loop below
                        } else if (mapping.containsKey("rowPattern")) {
                            Map<String, Object> rowPattern = (Map<String, Object>) mapping.get("rowPattern");
                            int start = ((Number) rowPattern.get("start")).intValue();
                            String type = (String) rowPattern.get("type");
                            rowIndexes = TxtParser.generateIndexes(data.size(), type, start);
                        } else if (mapping.containsKey("rowIndexes")) {
                            List<Object> rawList = (List<Object>) mapping.get("rowIndexes");
                            for (Object obj : rawList) {
                                rowIndexes.add(((Number) obj).intValue());
                            }
                        }

                        // Write data with row offset (skip titles for append)
                        for (int i = 0; i < rowIndexes.size(); i++) {
                            int rowIndex = rowIndexes.get(i);
                            if (rowIndex >= data.size() || rowIndex < 0)
                                continue;
                            List<String> rowData = data.get(rowIndex);
                            String value = (sourceColumn < rowData.size()) ? rowData.get(sourceColumn) : "";

                            if (direction.equals("vertical")) {
                                // BUG-FIX: Use Math.max to avoid double-counting startRow when appending
                                // When appending, we want to start at the end of the file (rowOffset),
                                // unless the file is empty and rowOffset < startRow.
                                int targetRow = Math.max(startRow, rowOffset) + i;

                                // Check row limit
                                if (targetRow >= EXCEL_ROW_LIMIT) {
                                    warnings.add("Excel row limit reached. Some data may be truncated.");
                                    break;
                                }

                                Row row = sheet.getRow(targetRow);
                                if (row == null)
                                    row = sheet.createRow(targetRow);
                                row.createCell(startCol).setCellValue(value);
                                rowsAdded = Math.max(rowsAdded, i + 1);
                            } else {
                                // BUG-FIX: Same logic for horizontal
                                int targetRow = Math.max(startRow, rowOffset);
                                Row row = sheet.getRow(targetRow);
                                if (row == null)
                                    row = sheet.createRow(targetRow);
                                row.createCell(startCol + i + 1).setCellValue(value);
                                rowsAdded = 1;
                            }
                        }
                    }
                }

                // Check if approaching row limit
                int finalRowCount = rowOffset + rowsAdded;
                if (finalRowCount > WARNING_THRESHOLD) {
                    warnings.add(String.format("Approaching Excel row limit: %d of %d rows used (%.1f%%)",
                            finalRowCount, EXCEL_ROW_LIMIT, (finalRowCount * 100.0 / EXCEL_ROW_LIMIT)));
                }

                // Write back to file
                try (FileOutputStream out = new FileOutputStream(existingExcelFile)) {
                    workbook.write(out);
                }

                if (warnings.isEmpty()) {
                    return AppendResult.success(rowsAdded, rowOffset, existingExcelFile.getAbsolutePath());
                } else {
                    return AppendResult.successWithWarnings(rowsAdded, rowOffset,
                            existingExcelFile.getAbsolutePath(), warnings);
                }

            } finally {
                workbook.close();
            }

        } catch (java.io.IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("being used by another process")) {
                return AppendResult.failure("Cannot access file: It may be open in another application. " +
                        "Please close the file and try again.", existingExcelFile.getAbsolutePath());
            }
            return AppendResult.failure("Error accessing file: " + e.getMessage(), existingExcelFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return AppendResult.failure("Append failed: " + e.getMessage(), existingExcelFile.getAbsolutePath());
        }
    }

    private static void writeSemicolonData(List<List<String>> data, List<Map<String, Object>> mappings, Sheet sheet, int rowOffset) {
        // 1. Identify start row index for each cavity block
        List<Integer> blockStarts = new ArrayList<>();
        blockStarts.add(0);
        for (int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            if (!row.isEmpty() && "@101".equals(row.get(0).trim())) {
                if (r + 1 < data.size()) {
                    blockStarts.add(r + 1);
                }
            }
        }

        // BUG-FIX: Remove phantom blockStarts caused by trailing @101 markers.
        // A blockStart is phantom if its first row is an @101 row itself, or if
        // the block contains no *real* measurement data rows before the next @101.
        // NOTE: parseMultiLineGroupedBlock injects header rows (starting with "Element")
        // that have size > 1 but are NOT real data — we must skip those too.
        blockStarts.removeIf(bs -> {
            if (bs >= data.size()) return true; // past end of data
            List<String> firstRow = data.get(bs);
            if (!firstRow.isEmpty() && "@101".equals(firstRow.get(0).trim())) return true; // points at @101 row
            // Walk rows in this block; a real measurement data row has size > 1 AND
            // its first column is NOT the static header word "Element".
            for (int r = bs; r < data.size(); r++) {
                List<String> row = data.get(r);
                if (!row.isEmpty() && "@101".equals(row.get(0).trim())) break; // hit next separator
                if (row.size() > 1 && !"Element".equals(row.get(0))) return false; // real data row — keep block
            }
            return true; // no real measurement rows found — phantom block
        });

        // 2. Compute size of Block 0 to generate relative indexes
        int block0Size = 0;
        for (int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            if (!row.isEmpty() && "@101".equals(row.get(0).trim())) {
                break;
            }
            block0Size++;
        }

        // 3. Iterate through each mapping
        for (Map<String, Object> mapping : mappings) {
            // Support backward compatibility with old semicolon fieldIndex mapping
            int sourceColumn = mapping.containsKey("fieldIndex")
                    ? ((Number) mapping.get("fieldIndex")).intValue()
                    : ((Number) mapping.get("sourceColumn")).intValue();

            String startCell = (String) mapping.get("startCell");
            String direction = (String) mapping.get("direction");
            String title = mapping.containsKey("title") ? mapping.get("title").toString() : "";
            boolean isFixed = mapping.containsKey("fixed") && Boolean.TRUE.equals(mapping.get("fixed"));

            CellReference ref = new CellReference(startCell);
            int startRow = ref.getRow();
            int startCol = ref.getCol();

            // When appending, start at Math.max(startRow, rowOffset)
            int baseStartRow = (rowOffset > 0) ? Math.max(startRow, rowOffset) : startRow;

            // Generate block-relative row indexes and output positions
            List<Integer> relRowIndexes = new ArrayList<>();
            List<Integer> outputPositions = new ArrayList<>();

            if (MappingPathResolver.shouldUseFlexPath(mapping)) {
                int startField = (mapping.containsKey("startField") && mapping.get("startField") != null)
                        ? ((Number) mapping.get("startField")).intValue() : 1;
                int fillField  = ((Number) mapping.get("fillField")).intValue();
                int spaceField = (mapping.containsKey("spaceField") && mapping.get("spaceField") != null)
                        ? ((Number) mapping.get("spaceField")).intValue() : 0;

                RowPatternDescriptor descriptor = new RowPatternDescriptor(startField, fillField, spaceField);
                descriptor.generateOutputSequence(block0Size).forEach(entry -> {
                    outputPositions.add(entry.getKey());
                    relRowIndexes.add(entry.getValue());
                });
            } else if (mapping.containsKey("rowPattern")) {
                Map<String, Object> rowPattern = (Map<String, Object>) mapping.get("rowPattern");
                int start = ((Number) rowPattern.get("start")).intValue();
                String type = (String) rowPattern.get("type");
                List<Integer> indexes = TxtParser.generateIndexes(block0Size, type, start);
                for (int i = 0; i < indexes.size(); i++) {
                    outputPositions.add(i);
                    relRowIndexes.add(indexes.get(i));
                }
            } else if (mapping.containsKey("rowIndexes")) {
                List<Object> rawList = (List<Object>) mapping.get("rowIndexes");
                for (int i = 0; i < rawList.size(); i++) {
                    outputPositions.add(i);
                    relRowIndexes.add(((Number) rawList.get(i)).intValue());
                }
            } else if (mapping.containsKey("blockRelativeRow")) {
                // Backward compatibility for old semicolon layout mapping formats
                int blockRel = ((Number) mapping.get("blockRelativeRow")).intValue();
                relRowIndexes.add(blockRel);
                outputPositions.add(0);
            }

            // Write title if present (only for rowOffset == 0)
            if (!title.isEmpty() && rowOffset == 0) {
                for (int cavityIndex = 0; cavityIndex < blockStarts.size(); cavityIndex++) {
                    if (isFixed && cavityIndex > 0) continue;

                    // BUG-FIX: Append set-number suffix to non-fixed titles for cavities > 0
                    // so users can identify which set each column belongs to.
                    // Set 1 → "Up Tol.", Set 2 → "Up Tol. (1)", Set 3 → "Up Tol. (2)"
                    String displayTitle = (!isFixed && cavityIndex > 0)
                            ? title + " (" + cavityIndex + ")"
                            : title;

                    int targetTitleCol;
                    if (isFixed) {
                        targetTitleCol = startCol;
                    } else {
                        // Compute groupWidth and offsetInGroup dynamically for this mapping's target Excel row (at outputPos = 0)
                        int targetBaseRow = "vertical".equals(direction) ? baseStartRow : baseStartRow;

                        List<Map<String, Object>> group = new ArrayList<>();
                        for (Map<String, Object> m : mappings) {
                            boolean mFixed = m.containsKey("fixed") && Boolean.TRUE.equals(m.get("fixed"));
                            if (!mFixed) {
                                String mStartCell = (String) m.get("startCell");
                                CellReference mRef = new CellReference(mStartCell);
                                int mBaseRow = (rowOffset > 0) ? Math.max(mRef.getRow(), rowOffset) : mRef.getRow();
                                int mTargetRow = "vertical".equals(m.get("direction")) ? mBaseRow : mBaseRow;

                                if (mTargetRow == targetBaseRow && direction.equals(m.get("direction"))) {
                                    group.add(m);
                                }
                            }
                        }
                        group.sort(java.util.Comparator.comparingInt(m -> new CellReference((String) m.get("startCell")).getCol()));
                        int groupWidth = group.size();
                        if (groupWidth == 0) groupWidth = 1;
                        int offsetInGroup = 0;
                        for (int g = 0; g < group.size(); g++) {
                            if (group.get(g) == mapping) {
                                offsetInGroup = g;
                                break;
                            }
                        }

                        int groupStartCol = group.isEmpty() ? startCol : new CellReference((String) group.get(0).get("startCell")).getCol();

                        if ("vertical".equals(direction)) {
                            targetTitleCol = groupStartCol + (cavityIndex * groupWidth) + offsetInGroup;
                        } else {
                            targetTitleCol = groupStartCol + 0 + 1 + (cavityIndex * groupWidth) + offsetInGroup;
                        }
                    }

                    if ("vertical".equals(direction)) {
                        if (baseStartRow > 0) {
                            Row titleRow = sheet.getRow(baseStartRow - 1);
                            if (titleRow == null) titleRow = sheet.createRow(baseStartRow - 1);
                            Cell cell = titleRow.getCell(targetTitleCol);
                            if (cell == null) cell = titleRow.createCell(targetTitleCol);
                            cell.setCellValue(displayTitle);
                        }
                    } else {
                        Row row = sheet.getRow(baseStartRow);
                        if (row == null) row = sheet.createRow(baseStartRow);
                        Cell cell = row.getCell(targetTitleCol);
                        if (cell == null) cell = row.createCell(targetTitleCol);
                        cell.setCellValue(displayTitle);
                    }
                }
            }

            // Write data points across all cavities/blocks
            for (int cavityIndex = 0; cavityIndex < blockStarts.size(); cavityIndex++) {
                int blockStart = blockStarts.get(cavityIndex);

                for (int i = 0; i < relRowIndexes.size(); i++) {
                    int relRow = relRowIndexes.get(i);
                    int srcRowIndex = blockStart + relRow;
                    if (srcRowIndex >= data.size() || srcRowIndex < 0) continue;

                    List<String> rowData = data.get(srcRowIndex);
                    // Skip @101 lines or subsequent blocks
                    if (!rowData.isEmpty() && "@101".equals(rowData.get(0).trim())) {
                        continue;
                    }

                    int outputPos = outputPositions.get(i);
                    String value = (sourceColumn < rowData.size()) ? rowData.get(sourceColumn) : "";

                    int targetRow;
                    int targetCol;

                    if (isFixed) {
                        if (cavityIndex > 0) continue; // fixed mappings only write for cavity 0
                        targetRow = "vertical".equals(direction) ? baseStartRow + outputPos : baseStartRow;
                        targetCol = "vertical".equals(direction) ? startCol : startCol + outputPos + 1;
                    } else {
                        // Compute groupWidth and offsetInGroup dynamically for this mapping's target Excel row
                        int targetBaseRow = "vertical".equals(direction) ? baseStartRow + outputPos : baseStartRow;

                        List<Map<String, Object>> group = new ArrayList<>();
                        for (Map<String, Object> m : mappings) {
                            boolean mFixed = m.containsKey("fixed") && Boolean.TRUE.equals(m.get("fixed"));
                            if (!mFixed) {
                                String mStartCell = (String) m.get("startCell");
                                CellReference mRef = new CellReference(mStartCell);
                                int mBaseRow = (rowOffset > 0) ? Math.max(mRef.getRow(), rowOffset) : mRef.getRow();
                                int mTargetRow = "vertical".equals(m.get("direction")) ? mBaseRow + outputPos : mBaseRow;

                                if (mTargetRow == targetBaseRow && direction.equals(m.get("direction"))) {
                                    group.add(m);
                                }
                            }
                        }
                        group.sort(java.util.Comparator.comparingInt(m -> new CellReference((String) m.get("startCell")).getCol()));
                        int groupWidth = group.size();
                        if (groupWidth == 0) groupWidth = 1;
                        int offsetInGroup = 0;
                        for (int g = 0; g < group.size(); g++) {
                            if (group.get(g) == mapping) {
                                offsetInGroup = g;
                                break;
                            }
                        }

                        int groupStartCol = group.isEmpty() ? startCol : new CellReference((String) group.get(0).get("startCell")).getCol();

                        if ("vertical".equals(direction)) {
                            targetRow = baseStartRow + outputPos;
                            targetCol = groupStartCol + (cavityIndex * groupWidth) + offsetInGroup;
                        } else {
                            targetRow = baseStartRow;
                            targetCol = groupStartCol + outputPos + 1 + (cavityIndex * groupWidth) + offsetInGroup;
                        }
                    }

                    Row excelRow = sheet.getRow(targetRow);
                    if (excelRow == null) excelRow = sheet.createRow(targetRow);
                    Cell cell = excelRow.getCell(targetCol);
                    if (cell == null) cell = excelRow.createCell(targetCol);
                    cell.setCellValue(value);
                }
            }
        }
    }
}
