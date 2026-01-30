package com.example.smarttemplatefiller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExcelWriter append functionality.
 * Tests row offset calculation, data preservation, and error handling.
 */
class ExcelWriterAppendTest {

    @TempDir
    Path tempDir;

    private File mappingFile;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        // Create a simple mapping file for testing
        List<Map<String, Object>> mappings = List.of(
                Map.of(
                        "sourceColumn", 0,
                        "startCell", "A1",
                        "direction", "vertical",
                        "rowPattern", Map.of("start", 0, "type", "all")));
        mappingFile = tempDir.resolve("test_mapping.json").toFile();
        mapper.writeValue(mappingFile, mappings);
    }

    // ========== T007 Tests ==========

    @Test
    @DisplayName("Append to empty file should have offset 0")
    void testAppendToEmptyFile() throws Exception {
        // Create empty Excel file
        File excelFile = tempDir.resolve("empty.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Result");
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file with data
        File sourceFile = createSourceFile("line1\nline2\nline3");

        // Append
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

        // Verify
        assertTrue(result.isSuccess(), "Append should succeed");
        assertEquals(0, result.getRowOffset(), "Offset should be 0 for empty file");
        assertEquals(3, result.getRowsAdded(), "Should add 3 rows");
    }

    @Test
    @DisplayName("Append to file with data should calculate offset correctly")
    void testAppendToFileWithData() throws Exception {
        // Create Excel file with 3 rows of data
        File excelFile = tempDir.resolve("with_data.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Result");
            for (int i = 0; i < 3; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue("existing_row_" + i);
            }
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file with new data
        File sourceFile = createSourceFile("new1\nnew2");

        // Append
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

        // Verify result
        assertTrue(result.isSuccess(), "Append should succeed");
        assertEquals(3, result.getRowOffset(), "Offset should be 3 (existing rows)");
        assertEquals(2, result.getRowsAdded(), "Should add 2 rows");

        // Verify data in file - new data should start at row 3 (0-indexed)
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");
            assertEquals("new1", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals("new2", sheet.getRow(4).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Append should preserve existing data")
    void testAppendPreservesExistingData() throws Exception {
        // Create Excel file with existing data
        File excelFile = tempDir.resolve("preserve_test.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Result");
            sheet.createRow(0).createCell(0).setCellValue("original_A1");
            sheet.getRow(0).createCell(1).setCellValue("original_B1");
            sheet.createRow(1).createCell(0).setCellValue("original_A2");
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file with new data
        File sourceFile = createSourceFile("appended_data");

        // Append
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

        // Verify existing data is preserved
        assertTrue(result.isSuccess());
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");

            // Original data should be untouched
            assertEquals("original_A1", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("original_B1", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("original_A2", sheet.getRow(1).getCell(0).getStringCellValue());

            // New data should be after existing
            assertEquals("appended_data", sheet.getRow(2).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Calculate row offset handles edge cases correctly")
    void testCalculateRowOffsetEdgeCases() throws Exception {
        // Test with file having gaps (empty rows in between)
        File excelFile = tempDir.resolve("gaps_test.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Result");
            sheet.createRow(0).createCell(0).setCellValue("row0");
            // Row 1 is empty (gap)
            sheet.createRow(2).createCell(0).setCellValue("row2");
            // Row 3 is empty (gap)
            sheet.createRow(4).createCell(0).setCellValue("row4");
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file
        File sourceFile = createSourceFile("new_data");

        // Append should use last row num + 1 as offset (row 4 is last, so offset = 5)
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

        assertTrue(result.isSuccess());
        assertEquals(5, result.getRowOffset(), "Offset should be based on last row, not row count");

        // Verify new data is at row 5
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");
            assertEquals("new_data", sheet.getRow(5).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Append should not duplicate headers when appending")
    void testAppendDoesNotDuplicateHeaders() throws Exception {
        // Create mapping with title/header
        List<Map<String, Object>> mappingsWithTitle = List.of(
                Map.of(
                        "sourceColumn", 0,
                        "startCell", "A2", // Data starts at A2, title at A1
                        "direction", "vertical",
                        "title", "Column Header",
                        "rowPattern", Map.of("start", 0, "type", "all")));
        File mappingWithTitleFile = tempDir.resolve("mapping_with_title.json").toFile();
        mapper.writeValue(mappingWithTitleFile, mappingsWithTitle);

        // Create Excel file that already has the header and data
        File excelFile = tempDir.resolve("headers_test.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Result");
            sheet.createRow(0).createCell(0).setCellValue("Column Header");
            sheet.createRow(1).createCell(0).setCellValue("existing_data");
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file
        File sourceFile = createSourceFile("new_row1\nnew_row2");

        // Append
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingWithTitleFile, excelFile);

        assertTrue(result.isSuccess());

        // Verify header is NOT duplicated
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");

            // Count how many cells have "Column Header"
            int headerCount = 0;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    if (cell != null && "Column Header".equals(cell.getStringCellValue())) {
                        headerCount++;
                    }
                }
            }

            assertEquals(1, headerCount, "Header should appear only once, not duplicated on append");
        }
    }

    @Test
    @DisplayName("Append should handle startRow > 0 without creating gaps")
    void testAppendWithStartRowOne() throws Exception {
        // Create mapping starting at A2 (row index 1)
        List<Map<String, Object>> mappings = List.of(
                Map.of(
                        "sourceColumn", 0,
                        "startCell", "A2",
                        "direction", "vertical",
                        "title", "Header",
                        "rowPattern", Map.of("start", 0, "type", "all")));
        File mappingFile = tempDir.resolve("mapping_A2.json").toFile();
        mapper.writeValue(mappingFile, mappings);

        // Create Excel file with data up to row index 28 (Row 29)
        // This simulates the user's case where data exists, and next append should be
        // at row index 29 (Row 30)
        File excelFile = tempDir.resolve("start_row_test.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Result");
            // Fill rows 0 to 28
            for (int i = 0; i <= 28; i++) {
                sheet.createRow(i).createCell(0).setCellValue("row_" + i);
            }
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source
        File sourceFile = createSourceFile("new_data\nmore_data");

        // Append
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

        assertTrue(result.isSuccess());
        // Last row was index 28. Offset = 29.
        assertEquals(29, result.getRowOffset());

        // Verify data placement
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");

            // User reported bug: creates gap at row 30 (index 29) because target =
            // startRow(1) + offset(29) = 30
            // We expect data at index 29
            Row row29 = sheet.getRow(29);
            assertNotNull(row29, "Row 29 (30th row) should not be null");
            Cell cell29 = row29.getCell(0);
            assertNotNull(cell29, "Cell at 29 should not be null");
            assertEquals("new_data", cell29.getStringCellValue(), "New data should be at row index 29");
        }
    }

    // ========== T008 Tests ==========

    @Test
    @DisplayName("Append to locked file returns appropriate error")
    void testAppendWithLockedFile() throws Exception {
        // Create Excel file
        File excelFile = tempDir.resolve("locked.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Result");
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
        }

        // Create source file
        File sourceFile = createSourceFile("test_data");

        // Lock the file using FileChannel
        try (FileChannel channel = FileChannel.open(excelFile.toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {

            // Try to append while file is locked
            AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);

            // Should fail with appropriate error message
            assertFalse(result.isSuccess(), "Append should fail when file is locked");
            assertNotNull(result.getErrorMessage(), "Error message should not be null");
            // The error message should indicate file access issue
            assertTrue(
                    result.getErrorMessage().toLowerCase().contains("access") ||
                            result.getErrorMessage().toLowerCase().contains("open") ||
                            result.getErrorMessage().toLowerCase().contains("locked") ||
                            result.getErrorMessage().toLowerCase().contains("another"),
                    "Error message should mention file access issue: " + result.getErrorMessage());
        }
    }

    @Test
    @DisplayName("Append to non-existent file returns failure")
    void testAppendToNonExistentFile() throws Exception {
        File nonExistent = tempDir.resolve("does_not_exist.xlsx").toFile();
        File sourceFile = createSourceFile("test_data");

        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, nonExistent);

        assertFalse(result.isSuccess(), "Append should fail for non-existent file");
        assertTrue(result.getErrorMessage().contains("does not exist"),
                "Error should mention file doesn't exist");
    }

    // ========== Helper Methods ==========

    private File createSourceFile(String content) throws IOException {
        File sourceFile = tempDir.resolve("source_" + System.nanoTime() + ".txt").toFile();
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(content);
        }
        return sourceFile;
    }
}
