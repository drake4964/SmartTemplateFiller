package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.engine.MappingPathResolver;
import com.example.smarttemplatefiller.mapping.ColumnMapping;
import com.example.smarttemplatefiller.mapping.RowPatternDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T018 [US3]: Headless integration test verifying RunningModeController's
 * execution path — i.e. the ExcelWriter + MappingPathResolver + RowPatternDescriptor
 * pipeline — handles flex JSON mappings correctly without any UI overrides.
 *
 * RunningModeController itself calls FolderWatcher which calls
 * ExcelWriter.writeAdvancedMappedFile / appendToMappedFile.
 * We test those methods directly here to keep the test headless/JavaFX-free.
 */
class RunningModeFlexIntegrationTest {

    @TempDir
    Path tempDir;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Creates a minimal .txt source file with N data rows (pipe-separated). */
    private File createSourceFile(String filename, int rows) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (int i = 0; i < rows; i++) {
                // col0  col1  col2  ...  col4
                writer.println("row" + i + "  val" + i + "A  val" + i + "B  val" + i + "C  data" + i);
            }
        }
        return file;
    }

    /** Creates a flex mapping JSON file for the given parameters. */
    private File createFlexMappingJson(String filename, int sourceCol,
                                       String startCell, String direction,
                                       int startField, int fillField, int spaceField) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> mappings = new ArrayList<>();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("sourceColumn", sourceCol);
        entry.put("startCell", startCell);
        entry.put("direction", direction);
        entry.put("startField", startField);
        entry.put("fillField", fillField);
        entry.put("spaceField", spaceField);
        mappings.add(entry);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, mappings);
        return file;
    }

    /** Creates a legacy rowIndexes mapping JSON file. */
    private File createLegacyMappingJson(String filename, int sourceCol,
                                          String startCell, String direction,
                                          List<Integer> rowIndexes) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> mappings = new ArrayList<>();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("sourceColumn", sourceCol);
        entry.put("startCell", startCell);
        entry.put("direction", direction);
        entry.put("rowIndexes", rowIndexes);
        mappings.add(entry);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, mappings);
        return file;
    }

    /** Reads a cell value from an xlsx file. Row/col are 0-based. */
    private String readCell(File xlsx, int sheet, int row, int col) throws IOException {
        try (FileInputStream fis = new FileInputStream(xlsx);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet s = wb.getSheetAt(sheet);
            if (s == null) return null;
            Row r = s.getRow(row);
            if (r == null) return null;
            var cell = r.getCell(col);
            if (cell == null) return null;
            return cell.getStringCellValue();
        }
    }

    // -----------------------------------------------------------------------
    // T018 Tests
    // -----------------------------------------------------------------------

    /**
     * T018 core: ExcelWriter.writeAdvancedMappedFile with a flex mapping JSON
     * must apply the Start offset so the first exported cell contains source row 4
     * (startField=4 → 0-based index 3), not source row 1.
     */
    @Test
    void testFlexMappingJsonWrittenAndReadByExcelWriterHeadlessly() throws IOException {
        // 10 source rows available
        File source      = createSourceFile("source.txt", 10);
        // Start=4: skip preamble rows 1-3; Fill=1: read 1 at a time; Space=0: no gaps
        File mappingJson = createFlexMappingJson("flex.json", 4, "A1", "vertical", 4, 1, 0);
        File output      = tempDir.resolve("output.xlsx").toFile();

        ExcelWriter.writeAdvancedMappedFile(source, mappingJson, output);

        assertTrue(output.exists(), "Output Excel file must be created");

        // The first Excel cell (A1 = row 0, col 0) must contain the value from
        // source row index 3 (= startField 4 in 1-based). parseFlatTable produces
        // rows split on 2+ whitespace so col4 is index 4 → "data3" for row index 3.
        // Let's check col 0 (first token per row) which is "row3", "row4"... etc.
        String firstCell = readCell(output, 0, 0, 0);
        assertNotNull(firstCell);
        // parseFlatTable splits "row3  val3A  val3B  val3C  data3"
        // sourceColumn=4 → last token "data3"
        assertEquals("data3", firstCell,
                "Start=4 should skip rows 0-2 and begin exporting from source row index 3");

        String secondCell = readCell(output, 0, 1, 0);
        assertEquals("data4", secondCell,
                "Second exported cell should be source row index 4");
    }

    /**
     * T018: Flex mapping with Fill=2, Space=1 must produce alternating 2-read/1-skip pattern
     * in the headless export path.
     */
    @Test
    void testFlexFillAndSpacePatternHeadless() throws IOException {
        File source      = createSourceFile("source2.txt", 10);
        // Start=1, Fill=2, Space=1: output maps to source rows 0,1,3,4,6,7...
        File mappingJson = createFlexMappingJson("flex2.json", 4, "A1", "vertical", 1, 2, 1);
        File output      = tempDir.resolve("output2.xlsx").toFile();

        ExcelWriter.writeAdvancedMappedFile(source, mappingJson, output);

        assertTrue(output.exists());
        // col4 token = "data<n>" for source row n
        assertEquals("data0", readCell(output, 0, 0, 0));
        assertEquals("data1", readCell(output, 0, 1, 0));
        assertEquals("data3", readCell(output, 0, 2, 0), "Row 2 (index 2) must be skipped");
        assertEquals("data4", readCell(output, 0, 3, 0));
    }

    /**
     * T018: MappingPathResolver must correctly classify the same JSON that
     * ExcelWriter loads at runtime — verifying pipeline coherence.
     */
    @Test
    void testMappingPathResolverCoherenceWithSerializedJson() throws IOException {
        File mappingJson = createFlexMappingJson("flex3.json", 0, "A1", "vertical", 2, 3, 1);
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> rawMaps = mapper.readValue(mappingJson,
                mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        assertEquals(1, rawMaps.size());

        // Both the Map-based and ColumnMapping-based overloads must agree
        assertTrue(MappingPathResolver.shouldUseFlexPath(rawMaps.get(0)),
                "Map-based resolver must detect flex from raw JSON");

        ColumnMapping cm = mapper.convertValue(rawMaps.get(0), ColumnMapping.class);
        assertTrue(MappingPathResolver.shouldUseFlexPath(cm),
                "ColumnMapping-based resolver must detect flex after Jackson conversion");
    }

    /**
     * T018 legacy fallback: a legacy rowIndexes mapping must still be processed
     * correctly by ExcelWriter (regression guard - RunningMode must not break).
     */
    @Test
    void testLegacyRowIndexesMappingStillWorksHeadlessly() throws IOException {
        File source      = createSourceFile("source_legacy.txt", 8);
        // rowIndexes [0,2,4] in 0-based → rows 1,3,5 in 1-based user terms
        File mappingJson = createLegacyMappingJson("legacy.json", 4, "A1", "vertical",
                List.of(0, 2, 4));
        File output      = tempDir.resolve("output_legacy.xlsx").toFile();

        ExcelWriter.writeAdvancedMappedFile(source, mappingJson, output);

        assertTrue(output.exists());
        assertEquals("data0", readCell(output, 0, 0, 0));
        assertEquals("data2", readCell(output, 0, 1, 0));
        assertEquals("data4", readCell(output, 0, 2, 0));
    }
}
