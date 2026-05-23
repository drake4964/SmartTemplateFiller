package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.engine.MappingPathResolver;
import com.example.smarttemplatefiller.mapping.ColumnMapping;
import com.example.smarttemplatefiller.mapping.RowPatternDescriptor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Constitution Compliance Tests — Phase 6 (T019, T020, T021, T023)
 *
 * Verifies that the Row Pattern Flex feature adheres to the project
 * constitution principles before release.
 */
class ConstitutionComplianceTest {

    // -----------------------------------------------------------------------
    // T019 [Principle I]: Preview latency < 200ms
    // -----------------------------------------------------------------------

    /**
     * T019: The live preview is driven by RowPatternDescriptor.generateOutputSequence()
     * behind a 150ms UI debounce in FlexPatternPanel (< 200ms threshold).
     * This test verifies the core computation is fast enough that the total
     * latency (compute + 150ms debounce) stays well under 200ms.
     */
    @Test
    void t019_previewLogicCompletesWellUnder200ms() {
        RowPatternDescriptor desc = new RowPatternDescriptor(1, 3, 1);

        long start = System.currentTimeMillis();
        List<Map.Entry<Integer, Integer>> sequence = desc.generateOutputSequence(10_000)
                .limit(10)
                .collect(Collectors.toList());
        long elapsed = System.currentTimeMillis() - start;

        assertFalse(sequence.isEmpty(), "Preview sequence must not be empty");
        // The pure computation must be under 50ms (leaving plenty of headroom
        // together with the 150ms debounce to stay < 200ms total)
        assertTrue(elapsed < 50,
                "Core preview computation must be < 50ms; took " + elapsed + "ms");
    }

    /**
     * T019 supplementary: verify the FlexPatternPanel debounce constant is < 200ms
     * by inspecting the source file.
     */
    @Test
    void t019_flexPanelDebounceConstantIsBelowThreshold() throws Exception {
        File source = new File("src/main/java/com/example/smarttemplatefiller/FlexPatternPanel.java");
        assertTrue(source.exists(), "FlexPatternPanel.java must exist");

        String content = Files.readString(source.toPath());
        // The debounce schedule call must contain a numeric literal < 200
        // Pattern: .schedule(..., <N>);  where N is the ms delay
        assertTrue(content.contains("}, 150)"),
                "Debounce delay must be 150ms (< 200ms threshold) in FlexPatternPanel");
    }

    // -----------------------------------------------------------------------
    // T020 [Principle II]: RowPatternDescriptor is JavaFX-free
    // -----------------------------------------------------------------------

    /**
     * T020: RowPatternDescriptor must be a pure value object with no JavaFX
     * dependencies — it must remain independently testable without a display.
     */
    @Test
    void t020_rowPatternDescriptorHasNoJavaFxImports() throws Exception {
        File source = new File("src/main/java/com/example/smarttemplatefiller/mapping/RowPatternDescriptor.java");
        assertTrue(source.exists(), "RowPatternDescriptor.java must exist");

        List<String> lines = Files.readAllLines(source.toPath());
        List<String> javafxImports = lines.stream()
                .filter(l -> l.trim().startsWith("import") && l.contains("javafx"))
                .collect(Collectors.toList());

        assertEquals(0, javafxImports.size(),
                "RowPatternDescriptor must have zero JavaFX imports; found: " + javafxImports);
    }

    /**
     * T020: MappingPathResolver must also be JavaFX-free (stateless static helper).
     */
    @Test
    void t020_mappingPathResolverHasNoJavaFxImports() throws Exception {
        File source = new File("src/main/java/com/example/smarttemplatefiller/engine/MappingPathResolver.java");
        assertTrue(source.exists(), "MappingPathResolver.java must exist");

        List<String> lines = Files.readAllLines(source.toPath());
        List<String> javafxImports = lines.stream()
                .filter(l -> l.trim().startsWith("import") && l.contains("javafx"))
                .collect(Collectors.toList());

        assertEquals(0, javafxImports.size(),
                "MappingPathResolver must have zero JavaFX imports; found: " + javafxImports);
    }

    /**
     * Phase 7 T033: Verify semicolon package has been fully cleaned up to avoid circular or redundant UI dependencies.
     */
    @Test
    void t033_semicolonParserPackageCleanedUp() {
        File dir = new File("src/main/java/com/example/smarttemplatefiller/semicolon");
        assertFalse(dir.exists() && dir.list() != null && dir.list().length > 0, 
                "Semicolon package should be removed or empty for core architectural simplification");
    }

    @Test
    void t033_semicolonMappingEngineHasNoUiImports() throws Exception {
        File source = new File("src/main/java/com/example/smarttemplatefiller/SemicolonMappingEngine.java");
        if(source.exists()) {
            List<String> lines = Files.readAllLines(source.toPath());
            List<String> uiImports = lines.stream()
                    .filter(l -> l.trim().startsWith("import") && (l.contains("TeachModeController") || l.contains("javafx")))
                    .collect(Collectors.toList());
            assertEquals(0, uiImports.size(), "SemicolonMappingEngine must have zero UI imports; found: " + uiImports);
        }
    }

    /**
     * T020: Instantiate RowPatternDescriptor in a plain test (no JavaFX toolkit)
     * to confirm it runs without any display system requirement.
     */
    @Test
    void t020_rowPatternDescriptorInstantiatesWithoutJavaFx() {
        // If this test runs at all without toolkit errors, the class is JavaFX-free
        assertDoesNotThrow(() -> {
            RowPatternDescriptor d = new RowPatternDescriptor(3, 2, 1);
            long count = d.generateOutputSequence(20).count();
            assertTrue(count > 0);
        }, "RowPatternDescriptor must be instantiable without a JavaFX toolkit");
    }

    // -----------------------------------------------------------------------
    // T021 [Principle III]: Legacy mappings are perfectly reverse-compatible
    // -----------------------------------------------------------------------

    /**
     * T021: A ColumnMapping with null fillField (legacy) must NOT be flagged
     * as a flex path by MappingPathResolver.
     */
    @Test
    void t021_legacyMappingWithNullFillFieldNotFlaggedAsFlex() {
        ColumnMapping legacy = new ColumnMapping();
        legacy.setSourceColumn(0);
        legacy.setStartCell("A1");
        legacy.setDirection("vertical");
        // fillField intentionally not set → null

        assertFalse(MappingPathResolver.shouldUseFlexPath(legacy),
                "Legacy mapping with null fillField must NOT trigger flex path");
    }

    /**
     * T021: A raw JSON map with no flex keys must not be flagged as flex.
     */
    @Test
    void t021_legacyRawMapWithRowIndexesNotFlaggedAsFlex() {
        Map<String, Object> legacyMap = new java.util.LinkedHashMap<>();
        legacyMap.put("sourceColumn", 0);
        legacyMap.put("startCell", "A1");
        legacyMap.put("direction", "vertical");
        legacyMap.put("rowIndexes", List.of(0, 2, 4));

        assertFalse(MappingPathResolver.shouldUseFlexPath(legacyMap),
                "Legacy map with rowIndexes (no fillField) must NOT trigger flex path");
    }

    /**
     * T021: A raw JSON map with rowPattern (old odd/even/all format) also
     * must not be flagged as flex.
     */
    @Test
    void t021_legacyRawMapWithRowPatternNotFlaggedAsFlex() {
        Map<String, Object> legacyMap = new java.util.LinkedHashMap<>();
        legacyMap.put("sourceColumn", 1);
        legacyMap.put("startCell", "B2");
        legacyMap.put("direction", "vertical");
        Map<String, Object> rowPattern = new java.util.LinkedHashMap<>();
        rowPattern.put("type", "all");
        rowPattern.put("start", 0);
        legacyMap.put("rowPattern", rowPattern);

        assertFalse(MappingPathResolver.shouldUseFlexPath(legacyMap),
                "Legacy map with rowPattern must NOT trigger flex path");
    }

    /**
     * T021: A flex ColumnMapping with fillField set must be flagged correctly,
     * confirming the resolver discriminates accurately between old and new.
     */
    @Test
    void t021_flexMappingCorrectlyDistinguishedFromLegacy() {
        ColumnMapping flex = new ColumnMapping();
        flex.setFillField(3);
        flex.setStartField(1);
        flex.setSpaceField(0);

        assertTrue(MappingPathResolver.shouldUseFlexPath(flex),
                "Flex ColumnMapping with fillField set must trigger flex path");

        ColumnMapping legacy = new ColumnMapping();
        // No fillField

        assertFalse(MappingPathResolver.shouldUseFlexPath(legacy),
                "Bare ColumnMapping without fillField must NOT trigger flex path");
    }

    // -----------------------------------------------------------------------
    // T023 [Principle VII]: Malformed inputs are rejected gracefully
    // -----------------------------------------------------------------------

    /**
     * T023: RowPatternDescriptor constructor must reject start < 1.
     */
    @Test
    void t023_startFieldBelowOneRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new RowPatternDescriptor(0, 1, 0),
                "start=0 must be rejected with IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new RowPatternDescriptor(-5, 1, 0),
                "Negative start must be rejected");
    }

    /**
     * T023: RowPatternDescriptor constructor must reject fill < 1.
     */
    @Test
    void t023_fillFieldBelowOneRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new RowPatternDescriptor(1, 0, 0),
                "fill=0 must be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> new RowPatternDescriptor(1, -3, 0),
                "Negative fill must be rejected");
    }

    /**
     * T023: RowPatternDescriptor constructor must reject space < 0.
     */
    @Test
    void t023_spaceFieldBelowZeroRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new RowPatternDescriptor(1, 1, -1),
                "Negative space must be rejected");
    }

    /**
     * T023: Boundary — minimum valid values must NOT throw.
     */
    @Test
    void t023_minimumValidValuesAccepted() {
        assertDoesNotThrow(() -> new RowPatternDescriptor(1, 1, 0),
                "start=1, fill=1, space=0 is the minimum valid combination");
    }
}
