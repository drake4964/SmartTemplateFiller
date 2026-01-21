package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MultiFileMergeService.
 */
class MultiFileMergeServiceTest {

    private MultiFileMergeService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new MultiFileMergeService();
    }

    @Test
    void testValidateFileCount_TwoFiles_Valid() {
        assertDoesNotThrow(() -> service.validateFileCount(2));
    }

    @Test
    void testValidateFileCount_TenFiles_Valid() {
        assertDoesNotThrow(() -> service.validateFileCount(10));
    }

    @Test
    void testValidateFileCount_OneFile_ThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateFileCount(1));
        assertTrue(ex.getMessage().contains("At least 2 files"));
    }

    @Test
    void testValidateFileCount_ElevenFiles_ThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateFileCount(11));
        assertTrue(ex.getMessage().contains("Maximum 10 files"));
    }

    @Test
    void testMerge_TwoFiles_Success() throws IOException {
        // Prepare test data
        Map<Integer, List<List<String>>> inputFiles = new HashMap<>();

        // File 1: Column A has values 1, 2, 3
        List<List<String>> file1Data = Arrays.asList(
                Arrays.asList("A1", "B1"),
                Arrays.asList("A2", "B2"),
                Arrays.asList("A3", "B3"));
        inputFiles.put(1, file1Data);

        // File 2: Column A has values X, Y, Z
        List<List<String>> file2Data = Arrays.asList(
                Arrays.asList("X1", "Y1"),
                Arrays.asList("X2", "Y2"));
        inputFiles.put(2, file2Data);

        // Create mapping config
        MappingConfiguration config = new MappingConfiguration();
        config.addFileSlot(new FileSlot(1, "File 1"));
        config.addFileSlot(new FileSlot(2, "File 2"));

        // File1:A -> Output A1 (vertical)
        MultiFileMapping mapping1 = new MultiFileMapping(1, "A", "A1", Direction.VERTICAL);
        config.addMapping(mapping1);

        // File2:A -> Output B1 (vertical)
        MultiFileMapping mapping2 = new MultiFileMapping(2, "A", "B1", Direction.VERTICAL);
        config.addMapping(mapping2);

        Path outputPath = tempDir.resolve("output.xlsx");

        // Execute
        service.merge(inputFiles, config, outputPath);

        // Verify output file was created
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
    }

    @Test
    void testMerge_MixedMapping_Success() throws IOException {
        Map<Integer, List<List<String>>> inputFiles = new HashMap<>();

        List<List<String>> file1Data = Arrays.asList(
                Arrays.asList("Val1", "Val2", "Val3"));
        inputFiles.put(1, file1Data);

        List<List<String>> file2Data = Arrays.asList(
                Arrays.asList("Data1"),
                Arrays.asList("Data2"));
        inputFiles.put(2, file2Data);

        MappingConfiguration config = new MappingConfiguration();
        config.addFileSlot(new FileSlot(1, "Horizontal"));
        config.addFileSlot(new FileSlot(2, "Vertical"));

        // Horizontal mapping
        MultiFileMapping hMapping = new MultiFileMapping(1, "A", "A1", Direction.HORIZONTAL);
        config.addMapping(hMapping);

        // Vertical mapping
        MultiFileMapping vMapping = new MultiFileMapping(2, "A", "A3", Direction.VERTICAL);
        config.addMapping(vMapping);

        Path outputPath = tempDir.resolve("mixed.xlsx");
        service.merge(inputFiles, config, outputPath);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    void testMerge_MissingColumn_SkipsWithWarning() throws IOException {
        Map<Integer, List<List<String>>> inputFiles = new HashMap<>();

        // File only has column A
        List<List<String>> file1Data = Arrays.asList(
                Arrays.asList("OnlyA"));
        inputFiles.put(1, file1Data);
        inputFiles.put(2, Arrays.asList(Arrays.asList("File2")));

        MappingConfiguration config = new MappingConfiguration();
        config.addFileSlot(new FileSlot(1, "Test"));
        config.addFileSlot(new FileSlot(2, "Test2"));

        // Try to map column C which doesn't exist
        MultiFileMapping mapping = new MultiFileMapping(1, "C", "A1", Direction.VERTICAL);
        config.addMapping(mapping);

        Path outputPath = tempDir.resolve("missing_col.xlsx");

        // Should not throw, just skip the missing column
        assertDoesNotThrow(() -> service.merge(inputFiles, config, outputPath));
    }
}
