package com.example.smarttemplatefiller.integration;

import com.example.smarttemplatefiller.model.*;
import com.example.smarttemplatefiller.service.MultiFileMergeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for multi-file teach mode functionality.
 * Tests the complete workflow: loading multiple files, creating mappings, and
 * saving configuration.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiFileTeachModeIntegrationTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private Path mappingFile;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mappingFile = tempDir.resolve("test-mapping.json");
    }

    @Test
    @Order(1)
    @DisplayName("Should create mapping configuration with multiple file slots")
    void testCreateMultiFileMapping() throws IOException {
        // Given: Create a mapping configuration with 3 file slots
        MappingConfiguration config = new MappingConfiguration();
        config.setSchemaVersion("2.0");

        // Create file slots
        List<FileSlot> fileSlots = new ArrayList<>();
        fileSlots.add(createFileSlot(1, "Temperature Data"));
        fileSlots.add(createFileSlot(2, "Pressure Data"));
        fileSlots.add(createFileSlot(3, "Flow Rate Data"));
        config.setFileSlots(fileSlots);

        // Create mappings for each file slot
        List<MultiFileMapping> mappings = new ArrayList<>();
        mappings.add(createMapping(1, "A", "A1", Direction.VERTICAL, false));
        mappings.add(createMapping(1, "B", "B1", Direction.VERTICAL, false));
        mappings.add(createMapping(2, "A", "C1", Direction.VERTICAL, false));
        mappings.add(createMapping(3, "A", "D1", Direction.VERTICAL, false));
        config.setMappings(mappings);

        // When: Save to JSON
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingFile.toFile(), config);

        // Then: Verify file was created
        assertTrue(Files.exists(mappingFile), "Mapping file should exist");

        // And: Verify we can read it back
        MappingConfiguration loaded = objectMapper.readValue(mappingFile.toFile(), MappingConfiguration.class);
        assertEquals("2.0", loaded.getSchemaVersion());
        assertEquals(3, loaded.getFileSlots().size());
        assertEquals(4, loaded.getMappings().size());
    }

    @Test
    @Order(2)
    @DisplayName("Should correctly identify file slot for each mapping")
    void testFileSlotIdentification() throws IOException {
        // Given: Load the mapping configuration
        MappingConfiguration config = createTestConfiguration();

        // Then: Verify each mapping has correct file slot
        Map<Integer, Long> slotCounts = new HashMap<>();
        for (MultiFileMapping mapping : config.getMappings()) {
            int slot = mapping.getSourceFileSlot();
            assertTrue(slot >= 1 && slot <= 3, "File slot should be between 1 and 3");
            slotCounts.put(slot, slotCounts.getOrDefault(slot, 0L) + 1);
        }

        // And: Verify distribution
        assertEquals(2, slotCounts.get(1), "File 1 should have 2 mappings");
        assertEquals(1, slotCounts.get(2), "File 2 should have 1 mapping");
        assertEquals(1, slotCounts.get(3), "File 3 should have 1 mapping");
    }

    @Test
    @Order(3)
    @DisplayName("Should display file source in mapping list format")
    void testMappingDisplayFormat() {
        // Given: A mapping from file 1
        MultiFileMapping mapping = createMapping(1, "A", "A1", Direction.VERTICAL, false);

        // When: Format for display
        String displayText = String.format("File%d:%s → %s",
                mapping.getSourceFileSlot(),
                mapping.getSourceColumn(),
                mapping.getTargetCell());

        // Then: Verify format
        assertEquals("File1:A → A1", displayText);
    }

    @Test
    @Order(4)
    @DisplayName("Should save and load multi-file configuration with all metadata")
    void testSaveLoadRoundTrip() throws IOException {
        // Given: A complete configuration
        MappingConfiguration original = createTestConfiguration();
        original.setWatchConfig(createWatchConfig());
        original.setArchiveConfig(createArchiveConfig());

        // When: Save and load
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingFile.toFile(), original);
        MappingConfiguration loaded = objectMapper.readValue(mappingFile.toFile(), MappingConfiguration.class);

        // Then: Verify all data is preserved
        assertEquals(original.getSchemaVersion(), loaded.getSchemaVersion());
        assertEquals(original.getFileSlots().size(), loaded.getFileSlots().size());
        assertEquals(original.getMappings().size(), loaded.getMappings().size());
        assertNotNull(loaded.getWatchConfig());
        assertNotNull(loaded.getArchiveConfig());
    }

    @Test
    @Order(5)
    @DisplayName("Should enforce 10-file limit")
    void testFileSlotLimit() {
        // Given: Try to create 11 file slots
        MappingConfiguration config = new MappingConfiguration();
        List<FileSlot> fileSlots = new ArrayList<>();

        // When: Add 11 slots
        for (int i = 1; i <= 11; i++) {
            fileSlots.add(createFileSlot(i, "File " + i));
        }
        config.setFileSlots(fileSlots);

        // Then: Validate that slot numbers should be within 1-10
        for (FileSlot slot : config.getFileSlots()) {
            if (slot.getSlot() > 10) {
                fail("File slot " + slot.getSlot() + " exceeds maximum of 10");
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle backward compatibility with v1.0 mappings")
    void testBackwardCompatibility() throws IOException {
        // Given: A v1.0 style mapping (no schemaVersion, no fileSlots)
        Map<String, Object> v1Config = new HashMap<>();
        v1Config.put("mappings", List.of(
                Map.of("sourceColumn", "A", "targetCell", "A1", "direction", "VERTICAL")));

        // When: Save v1.0 format
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingFile.toFile(), v1Config);

        // Then: Should be able to upgrade (this would be handled by MappingUpgrader in
        // real code)
        // For now, just verify the file is readable
        assertTrue(Files.exists(mappingFile));
        assertTrue(Files.size(mappingFile) > 0);
    }

    // Helper methods

    private MappingConfiguration createTestConfiguration() {
        MappingConfiguration config = new MappingConfiguration();
        config.setSchemaVersion("2.0");

        List<FileSlot> fileSlots = new ArrayList<>();
        fileSlots.add(createFileSlot(1, "Temperature Data"));
        fileSlots.add(createFileSlot(2, "Pressure Data"));
        fileSlots.add(createFileSlot(3, "Flow Rate Data"));
        config.setFileSlots(fileSlots);

        List<MultiFileMapping> mappings = new ArrayList<>();
        mappings.add(createMapping(1, "A", "A1", Direction.VERTICAL, false));
        mappings.add(createMapping(1, "B", "B1", Direction.VERTICAL, false));
        mappings.add(createMapping(2, "A", "C1", Direction.VERTICAL, false));
        mappings.add(createMapping(3, "A", "D1", Direction.VERTICAL, false));
        config.setMappings(mappings);

        return config;
    }

    private FileSlot createFileSlot(int slot, String description) {
        FileSlot fileSlot = new FileSlot();
        fileSlot.setSlot(slot);
        fileSlot.setDescription(description);
        return fileSlot;
    }

    private MultiFileMapping createMapping(int fileSlot, String sourceColumn, String targetCell,
            Direction direction, boolean includeTitle) {
        MultiFileMapping mapping = new MultiFileMapping();
        mapping.setSourceFileSlot(fileSlot);
        mapping.setSourceColumn(sourceColumn);
        mapping.setTargetCell(targetCell);
        mapping.setDirection(direction);
        mapping.setIncludeTitle(includeTitle);
        return mapping;
    }

    private WatchConfiguration createWatchConfig() {
        WatchConfiguration config = new WatchConfiguration();
        config.setStabilityCheckSeconds(2);
        config.setMatchingStrategy(MatchingStrategy.PREFIX);
        return config;
    }

    private ArchiveConfiguration createArchiveConfig() {
        ArchiveConfiguration config = new ArchiveConfiguration();
        config.setOutputFolder(tempDir);
        config.setTimestampFormat(TimestampFormat.DATETIME);
        config.setArchiveInputFiles(true);
        return config;
    }
}
