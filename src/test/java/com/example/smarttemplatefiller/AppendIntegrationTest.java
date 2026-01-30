package com.example.smarttemplatefiller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Excel append functionality in run mode.
 * Tests the complete workflow of processing multiple files sequentially
 * and appending to a single Excel file.
 */
class AppendIntegrationTest {

    @TempDir
    Path tempDir;

    private File mappingFile;
    private File outputFolder;
    private ObjectMapper mapper = new ObjectMapper();

    private String originalUserHome;

    @BeforeEach
    void setUp() throws Exception {
        // Save original user.home to avoid overwriting real config
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toAbsolutePath().toString());

        // Create a simple mapping file that maps column 0 to A1 vertically
        List<Map<String, Object>> mappings = List.of(
                Map.of(
                        "sourceColumn", 0,
                        "startCell", "A1",
                        "direction", "vertical",
                        "rowPattern", Map.of("start", 0, "type", "all")));
        mappingFile = tempDir.resolve("test_mapping.json").toFile();
        mapper.writeValue(mappingFile, mappings);

        // Create output folder
        outputFolder = tempDir.resolve("output").toFile();
        outputFolder.mkdirs();
    }

    @AfterEach
    void tearDown() {
        // Restore original user.home
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    // ========== T013 Tests ==========

    /**
     * Test run mode append with multiple sequential files.
     * 3 sequential source files should result in a single Excel file
     * containing all data (3× the individual file data).
     */
    @Test
    @DisplayName("Run mode append multiple files to single Excel")
    void testRunModeAppendMultipleFiles() throws Exception {
        // Create 3 source files with different data
        File sourceFile1 = createSourceFile("file1_row1\nfile1_row2");
        File sourceFile2 = createSourceFile("file2_row1\nfile2_row2");
        File sourceFile3 = createSourceFile("file3_row1\nfile3_row2");

        // Simulate run mode: first file creates new Excel
        File excelFile = tempDir.resolve("accumulated.xlsx").toFile();

        // Process first file - creates new file
        createNewExcelWithData(sourceFile1, excelFile);

        // Verify first file created Excel with 2 rows
        int rowCountAfterFirst = getRowCount(excelFile);
        assertEquals(2, rowCountAfterFirst, "First file should create 2 rows");

        // Process second file - append
        AppendResult result2 = ExcelWriter.appendToMappedFile(sourceFile2, mappingFile, excelFile);
        assertTrue(result2.isSuccess(), "Second append should succeed");
        assertEquals(2, result2.getRowOffset(), "Offset should be 2 after first file");
        assertEquals(2, result2.getRowsAdded(), "Should add 2 rows from second file");

        // Process third file - append
        AppendResult result3 = ExcelWriter.appendToMappedFile(sourceFile3, mappingFile, excelFile);
        assertTrue(result3.isSuccess(), "Third append should succeed");
        assertEquals(4, result3.getRowOffset(), "Offset should be 4 after two files");
        assertEquals(2, result3.getRowsAdded(), "Should add 2 rows from third file");

        // Verify final Excel has all 6 rows (2 × 3 files)
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");
            assertNotNull(sheet, "Result sheet should exist");

            // Verify total row count (lastRowNum is 0-indexed)
            int totalRows = sheet.getLastRowNum() + 1;
            assertEquals(6, totalRows, "Should have 6 total rows (2 per file × 3 files)");

            // Verify data from each file is in correct position
            assertEquals("file1_row1", getCellValue(sheet, 0, 0), "Row 0 should be from file 1");
            assertEquals("file1_row2", getCellValue(sheet, 1, 0), "Row 1 should be from file 1");
            assertEquals("file2_row1", getCellValue(sheet, 2, 0), "Row 2 should be from file 2");
            assertEquals("file2_row2", getCellValue(sheet, 3, 0), "Row 3 should be from file 2");
            assertEquals("file3_row1", getCellValue(sheet, 4, 0), "Row 4 should be from file 3");
            assertEquals("file3_row2", getCellValue(sheet, 5, 0), "Row 5 should be from file 3");
        }
    }

    /**
     * Test edge case: verify append preserves data order with timestamp scenario.
     * Simulates real run mode where files are processed sequentially over time.
     */
    @Test
    @DisplayName("Run mode append maintains correct data order")
    void testRunModeAppendMaintainsDataOrder() throws Exception {
        File excelFile = tempDir.resolve("ordered.xlsx").toFile();

        // Create and process files in specific order
        for (int i = 1; i <= 3; i++) {
            File sourceFile = createSourceFile("batch_" + i + "_data");

            if (i == 1) {
                // First file creates new Excel
                createNewExcelWithData(sourceFile, excelFile);
            } else {
                // Subsequent files append
                AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, excelFile);
                assertTrue(result.isSuccess(), "Append for batch " + i + " should succeed");
            }
        }

        // Verify order is preserved
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");

            assertEquals("batch_1_data", getCellValue(sheet, 0, 0));
            assertEquals("batch_2_data", getCellValue(sheet, 1, 0));
            assertEquals("batch_3_data", getCellValue(sheet, 2, 0));
        }
    }

    // ========== T014 Tests ==========

    /**
     * Test edge case: target file deleted between appends.
     * When the target Excel file is deleted mid-session, the system should
     * gracefully handle this by returning an error, allowing the caller
     * to create a new file and warn the user.
     */
    @Test
    @DisplayName("Run mode append when target file deleted returns failure")
    void testRunModeAppendWhenFileDeleted() throws Exception {
        // Create initial Excel file
        File excelFile = tempDir.resolve("will_be_deleted.xlsx").toFile();
        File sourceFile1 = createSourceFile("initial_data");
        createNewExcelWithData(sourceFile1, excelFile);

        // Verify file exists and has data
        assertTrue(excelFile.exists(), "Excel file should exist initially");
        assertEquals(1, getRowCount(excelFile), "Should have 1 row");

        // Delete the file (simulating external deletion)
        assertTrue(excelFile.delete(), "Should be able to delete file");
        assertFalse(excelFile.exists(), "File should be deleted");

        // Try to append to deleted file
        File sourceFile2 = createSourceFile("second_batch");
        AppendResult result = ExcelWriter.appendToMappedFile(sourceFile2, mappingFile, excelFile);

        // Append should fail gracefully
        assertFalse(result.isSuccess(), "Append should fail when target file is deleted");
        assertNotNull(result.getErrorMessage(), "Should have error message");
        assertTrue(
                result.getErrorMessage().toLowerCase().contains("not exist") ||
                        result.getErrorMessage().toLowerCase().contains("deleted") ||
                        result.getErrorMessage().toLowerCase().contains("found"),
                "Error should indicate file not found: " + result.getErrorMessage());
    }

    /**
     * Test the recovery scenario: after file deletion, creating new file works.
     * This simulates the real run mode behavior where if target is deleted,
     * we create a new file and continue accumulating.
     */
    @Test
    @DisplayName("Run mode recovery after file deletion")
    void testRunModeRecoveryAfterFileDeletion() throws Exception {
        File excelFile = tempDir.resolve("recovery_test.xlsx").toFile();

        // First batch - creates file
        File sourceFile1 = createSourceFile("batch1");
        createNewExcelWithData(sourceFile1, excelFile);
        assertEquals(1, getRowCount(excelFile));

        // Second batch - appends
        File sourceFile2 = createSourceFile("batch2");
        AppendResult result2 = ExcelWriter.appendToMappedFile(sourceFile2, mappingFile, excelFile);
        assertTrue(result2.isSuccess());
        assertEquals(2, getRowCount(excelFile));

        // File gets deleted
        excelFile.delete();

        // Third batch - append fails
        File sourceFile3 = createSourceFile("batch3");
        AppendResult result3 = ExcelWriter.appendToMappedFile(sourceFile3, mappingFile, excelFile);
        assertFalse(result3.isSuccess(), "Append to deleted file should fail");

        // Recovery: create new file for third batch
        createNewExcelWithData(sourceFile3, excelFile);
        assertEquals(1, getRowCount(excelFile), "New file should have 1 row");

        // Fourth batch - append to new file works
        File sourceFile4 = createSourceFile("batch4");
        AppendResult result4 = ExcelWriter.appendToMappedFile(sourceFile4, mappingFile, excelFile);
        assertTrue(result4.isSuccess(), "Append to new file should succeed");
        assertEquals(2, getRowCount(excelFile), "Should have 2 rows after recovery");
    }

    // ========== T023 Tests (Persistence) ==========

    /**
     * Test persistence of RunningModeConfig append setting.
     * Verifies that the appendModeEnabled flag is saved and reloaded correctly.
     */
    @Test
    @DisplayName("RunningModeConfig persists append mode setting")
    void testAppendModePreferencePersistence() throws Exception {
        // 1. Create config with default values
        RunningModeConfig config = new RunningModeConfig();
        assertFalse(config.isAppendModeEnabled(), "Default should be false");

        // 2. Change setting and save
        config.setAppendModeEnabled(true);
        config.save();

        // 3. Load new instance and verify persistence
        RunningModeConfig loadedConfig = RunningModeConfig.load();
        assertTrue(loadedConfig.isAppendModeEnabled(), "Append mode should be true after loading");

        // 4. Update again and verify
        loadedConfig.setAppendModeEnabled(false);
        loadedConfig.save();

        RunningModeConfig config3 = RunningModeConfig.load();
        assertFalse(config3.isAppendModeEnabled(), "Append mode should be false after second update");
    }

    /**
     * Test persistence of ExportConfiguration (used in Manual Export dialog).
     * Verifies that the user's last choice for "Create New" vs "Append" is saved.
     */
    @Test
    @DisplayName("ExportConfiguration persists export dialog preference")
    void testExportDialogPreferencePersistence() {
        // 1. Initial load (defaults)
        ExportConfiguration config = new ExportConfiguration();
        assertFalse(config.isAppendMode(), "Default export mode should be false (Create New)");

        // 2. Set to Append Mode and save
        config.setAppendMode(true);
        config.save();

        // 3. Load from disk and verify
        ExportConfiguration loaded = ExportConfiguration.load();
        assertTrue(loaded.isAppendMode(), "Export preference should be persisted as true");

        // 4. Set back to Create New and save
        loaded.setAppendMode(false);
        loaded.save();

        ExportConfiguration finalConfig = ExportConfiguration.load();
        assertFalse(finalConfig.isAppendMode(), "Export preference should be persisted as false");
    }

    // ========== Helper Methods ==========

    private File createSourceFile(String content) throws IOException {
        File sourceFile = tempDir.resolve("source_" + System.nanoTime() + ".txt").toFile();
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(content);
        }
        return sourceFile;
    }

    /**
     * Creates a new Excel file with data from source file using the mapping.
     * Simulates the first file in run mode (when no append target exists).
     */
    private void createNewExcelWithData(File sourceFile, File outputExcel) throws Exception {
        ExcelWriter.writeAdvancedMappedFile(sourceFile, mappingFile, outputExcel);
    }

    /**
     * Gets the row count from an Excel file's Result sheet.
     */
    private int getRowCount(File excelFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Result");
            if (sheet == null) {
                return 0;
            }
            // lastRowNum is 0-indexed, so add 1 for actual count
            return sheet.getLastRowNum() + 1;
        }
    }

    /**
     * Gets the string value from a cell at the specified row and column.
     */
    private String getCellValue(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null)
            return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;
        return cell.getStringCellValue();
    }
}
