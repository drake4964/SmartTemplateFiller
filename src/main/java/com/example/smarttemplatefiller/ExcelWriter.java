package com.example.smarttemplatefiller;

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

                for (Map<String, Object> mapping : mappings) {
                    int sourceColumn = ((Number) mapping.get("sourceColumn")).intValue();
                    String startCell = (String) mapping.get("startCell");
                    String direction = (String) mapping.get("direction");
                    String title = mapping.containsKey("title") ? mapping.get("title").toString() : "";

                    // BUG-001 FIX: Use CellReference for proper parsing (supports AA, AB, etc.)
                    CellReference ref = new CellReference(startCell);
                    int startRow = ref.getRow();
                    int startCol = ref.getCol();

                    // Determine row indexes
                    List<Integer> rowIndexes = new ArrayList<>();
                    if (mapping.containsKey("rowPattern")) {
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

                // Apply mappings with offset
                for (Map<String, Object> mapping : mappings) {
                    int sourceColumn = ((Number) mapping.get("sourceColumn")).intValue();
                    String startCell = (String) mapping.get("startCell");
                    String direction = (String) mapping.get("direction");

                    CellReference ref = new CellReference(startCell);
                    int startRow = ref.getRow();
                    int startCol = ref.getCol();

                    // Determine row indexes
                    List<Integer> rowIndexes = new ArrayList<>();
                    if (mapping.containsKey("rowPattern")) {
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
                            int targetRow = startRow + rowOffset + i;

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
                            int targetRow = startRow + rowOffset;
                            Row row = sheet.getRow(targetRow);
                            if (row == null)
                                row = sheet.createRow(targetRow);
                            row.createCell(startCol + i + 1).setCellValue(value);
                            rowsAdded = 1;
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

                // Log the operation
                System.out.println(String.format("[ExcelWriter] Appended %d rows to %s (offset: %d)",
                        rowsAdded, existingExcelFile.getName(), rowOffset));

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
}
