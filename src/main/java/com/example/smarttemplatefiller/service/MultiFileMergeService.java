package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.MappingConfiguration;
import com.example.smarttemplatefiller.model.MultiFileMapping;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Service for merging multiple input files into a single Excel output.
 * Supports 2-10 input files with flexible row/column/mixed mapping.
 */
public class MultiFileMergeService {
    private static final Logger logger = LoggerFactory.getLogger(MultiFileMergeService.class);

    private static final int MIN_FILES = 1;
    private static final int MAX_FILES = 10;

    /**
     * Merge multiple input files into a single Excel output.
     * 
     * @param inputFiles Map of slot number (1-10) to input file data (parsed
     *                   content)
     * @param config     The mapping configuration
     * @param outputPath Path for the output Excel file
     * @throws IOException              If file operations fail
     * @throws IllegalArgumentException If file count is invalid
     */
    public void merge(Map<Integer, List<List<String>>> inputFiles,
            MappingConfiguration config,
            Path outputPath) throws IOException {

        validateFileCount(inputFiles.size());
        logger.info("Starting multi-file merge with {} input files", inputFiles.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Merged Data");

            for (MultiFileMapping mapping : config.getMappings()) {
                int slot = mapping.getSourceFileSlot();
                List<List<String>> fileData = inputFiles.get(slot);

                if (fileData == null) {
                    logger.warn("No data for file slot {}, skipping mapping: {}",
                            slot, mapping.toDisplayString());
                    continue;
                }

                applyMapping(sheet, fileData, mapping);
            }

            // Auto-size columns
            for (int i = 0; i < 26; i++) { // A-Z
                sheet.autoSizeColumn(i);
            }

            // Write output
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }

            logger.info("Multi-file merge complete. Output: {}", outputPath);
        }
    }

    /**
     * Apply a single mapping to the worksheet.
     */
    private void applyMapping(Sheet sheet, List<List<String>> fileData, MultiFileMapping mapping) {
        String sourceColumn = mapping.getSourceColumn();
        String targetCell = mapping.getTargetCell();

        // Parse source column (A, B, C, etc. or index)
        int sourceColIndex = parseColumnIndex(sourceColumn);
        if (sourceColIndex < 0) {
            logger.warn("Invalid source column '{}', skipping", sourceColumn);
            return;
        }

        // Parse target cell (e.g., A1, B5)
        CellReference targetRef = parseCellReference(targetCell);
        if (targetRef == null) {
            logger.warn("Invalid target cell '{}', skipping", targetCell);
            return;
        }

        int startRow = targetRef.row;
        int startCol = targetRef.col;

        logger.debug("Applying mapping: File{}:{} -> {}",
                mapping.getSourceFileSlot(), sourceColumn, targetCell);

        boolean isVertical = mapping.getDirection() == com.example.smarttemplatefiller.model.Direction.VERTICAL;

        // Determine which rows to process
        List<Integer> rowIndexes = new java.util.ArrayList<>();
        if (mapping.getRowPattern() != null) {
            Map<String, Object> pattern = mapping.getRowPattern();
            String type = (String) pattern.get("type");
            int start = pattern.get("start") instanceof Number ? ((Number) pattern.get("start")).intValue() : 0;
            // Use TxtParser utility to generate indexes
            rowIndexes = com.example.smarttemplatefiller.TxtParser.generateIndexes(fileData.size(), type, start);
        } else if (mapping.getRowIndexes() != null) {
            rowIndexes = mapping.getRowIndexes();
        } else {
            // Default: All rows
            for (int i = 0; i < fileData.size(); i++) {
                rowIndexes.add(i);
            }
        }

        // Write title if present (placed one unit "before" the start cell)
        if (mapping.getTitle() != null && !mapping.getTitle().isEmpty()) {
            if (isVertical) {
                if (startRow > 0) {
                    setCellValue(sheet, startRow - 1, startCol, mapping.getTitle());
                } else {
                    logger.warn("Cannot write vertical title at row -1 for target {}", targetCell);
                }
            } else {
                if (startCol > 0) {
                    setCellValue(sheet, startRow, startCol - 1, mapping.getTitle());
                } else {
                    logger.warn("Cannot write horizontal title at col -1 for target {}", targetCell);
                }
            }
        }

        // Copy filtering and packing data
        for (int i = 0; i < rowIndexes.size(); i++) {
            int rowIndex = rowIndexes.get(i);

            if (rowIndex >= fileData.size() || rowIndex < 0)
                continue;

            List<String> rowData = fileData.get(rowIndex);

            if (sourceColIndex >= rowData.size()) {
                // Skip or write empty? Writing empty is safer for alignment
                // But following original logic: just skip
                continue;
            }

            String value = rowData.get(sourceColIndex);

            // Packed target index: i (0, 1, 2...) matches compressed output
            int targetRowIndex = isVertical ? startRow + i : startRow;
            int targetColIndex = isVertical ? startCol : startCol + i;

            setCellValue(sheet, targetRowIndex, targetColIndex, value);
        }
    }

    /**
     * Parse column letter(s) or number to column index.
     */
    private int parseColumnIndex(String column) {
        if (column == null || column.isEmpty()) {
            return -1;
        }

        // Check if it's a letter (A, B, C, etc.)
        if (Character.isLetter(column.charAt(0))) {
            column = column.toUpperCase();
            int index = 0;
            for (int i = 0; i < column.length(); i++) {
                index = index * 26 + (column.charAt(i) - 'A' + 1);
            }
            return index - 1; // 0-indexed
        }

        // Try parsing as number
        try {
            return Integer.parseInt(column);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parse Excel cell reference (e.g., A1, B5).
     */
    private CellReference parseCellReference(String cellRef) {
        if (cellRef == null || cellRef.isEmpty()) {
            return null;
        }

        cellRef = cellRef.toUpperCase();
        StringBuilder colPart = new StringBuilder();
        StringBuilder rowPart = new StringBuilder();

        for (char c : cellRef.toCharArray()) {
            if (Character.isLetter(c)) {
                colPart.append(c);
            } else if (Character.isDigit(c)) {
                rowPart.append(c);
            }
        }

        if (colPart.length() == 0 || rowPart.length() == 0) {
            return null;
        }

        int col = parseColumnIndex(colPart.toString());
        int row = Integer.parseInt(rowPart.toString()) - 1; // 0-indexed

        return new CellReference(row, col);
    }

    /**
     * Set a cell value in the sheet.
     */
    private void setCellValue(Sheet sheet, int rowIndex, int colIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }

        // Try to set as number if possible
        try {
            double numValue = Double.parseDouble(value);
            cell.setCellValue(numValue);
        } catch (NumberFormatException e) {
            cell.setCellValue(value);
        }
    }

    /**
     * Validate file count is within limits.
     */
    public void validateFileCount(int count) {
        if (count < MIN_FILES) {
            throw new IllegalArgumentException(
                    String.format("At least %d files are required for multi-file merge (got %d)",
                            MIN_FILES, count));
        }
        if (count > MAX_FILES) {
            throw new IllegalArgumentException(
                    String.format("Maximum %d files allowed for multi-file merge (got %d)",
                            MAX_FILES, count));
        }
    }

    /**
     * Simple helper class for cell references.
     */
    private static class CellReference {
        final int row;
        final int col;

        CellReference(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
