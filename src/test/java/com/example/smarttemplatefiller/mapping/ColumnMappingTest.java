package com.example.smarttemplatefiller.mapping;

import com.example.smarttemplatefiller.engine.MappingPathResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// T016 [US3]: JSON Serialization/Deserialization tests for ColumnMapping flex fields
class ColumnMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // Flex path round-trip
    // -----------------------------------------------------------------------

    /**
     * US3 core contract: a mapping with flex fields serializes to JSON and
     * deserializes back with all three fields intact.
     */
    @Test
    void testFlexFieldsRoundTrip() throws Exception {
        ColumnMapping original = new ColumnMapping();
        original.setSourceColumn(2);
        original.setStartCell("B3");
        original.setDirection("vertical");
        original.setStartField(4);
        original.setFillField(3);
        original.setSpaceField(1);

        String json = mapper.writeValueAsString(original);

        // JSON must contain the three flex keys
        assertTrue(json.contains("\"startField\""), "JSON should contain startField");
        assertTrue(json.contains("\"fillField\""),  "JSON should contain fillField");
        assertTrue(json.contains("\"spaceField\""), "JSON should contain spaceField");

        ColumnMapping loaded = mapper.readValue(json, ColumnMapping.class);

        assertEquals(original.getSourceColumn(), loaded.getSourceColumn());
        assertEquals(original.getStartCell(),    loaded.getStartCell());
        assertEquals(original.getDirection(),    loaded.getDirection());
        assertEquals(4, loaded.getStartField(),  "startField must survive round-trip");
        assertEquals(3, loaded.getFillField(),   "fillField must survive round-trip");
        assertEquals(1, loaded.getSpaceField(),  "spaceField must survive round-trip");
    }

    /**
     * MappingPathResolver must detect the flex path after deserialization.
     */
    @Test
    void testMappingPathResolverDetectsFlexAfterDeserialization() throws Exception {
        String json = "{\"sourceColumn\":1,\"startCell\":\"A1\",\"direction\":\"vertical\","
                + "\"startField\":1,\"fillField\":2,\"spaceField\":0}";

        ColumnMapping mapping = mapper.readValue(json, ColumnMapping.class);

        assertTrue(MappingPathResolver.shouldUseFlexPath(mapping),
                "MappingPathResolver should flag flex path when fillField is present");
    }

    // -----------------------------------------------------------------------
    // Legacy path preservation
    // -----------------------------------------------------------------------

    /**
     * Legacy mappings with rowIndexes must deserialize correctly and NOT be
     * flagged as flex by MappingPathResolver.
     */
    @Test
    void testLegacyRowIndexesPreservedOnRoundTrip() throws Exception {
        ColumnMapping original = new ColumnMapping();
        original.setSourceColumn(0);
        original.setStartCell("A2");
        original.setDirection("vertical");
        original.setRowIndexes(Arrays.asList(0, 2, 4, 6));

        String json = mapper.writeValueAsString(original);

        assertFalse(json.contains("\"fillField\""),
                "Legacy mapping should NOT serialize fillField");

        ColumnMapping loaded = mapper.readValue(json, ColumnMapping.class);

        assertNull(loaded.getFillField(),  "fillField must be null for legacy mapping");
        assertNotNull(loaded.getRowIndexes(), "rowIndexes must survive round-trip");
        assertEquals(List.of(0, 2, 4, 6), loaded.getRowIndexes());

        assertFalse(MappingPathResolver.shouldUseFlexPath(loaded),
                "Legacy mapping should NOT be detected as flex path");
    }

    // -----------------------------------------------------------------------
    // Graceful handling of missing/null optional flex fields
    // -----------------------------------------------------------------------

    /**
     * When loading a JSON that has fillField but omits startField and spaceField,
     * deserialization must succeed and null fields must be handled safely.
     */
    @Test
    void testPartialFlexJsonDeserialization() throws Exception {
        // Only fillField present — startField and spaceField are absent
        String json = "{\"sourceColumn\":0,\"startCell\":\"A1\",\"direction\":\"vertical\","
                + "\"fillField\":5}";

        ColumnMapping mapping = mapper.readValue(json, ColumnMapping.class);

        assertEquals(5, mapping.getFillField());
        assertNull(mapping.getStartField(),  "startField should be null when absent from JSON");
        assertNull(mapping.getSpaceField(), "spaceField should be null when absent from JSON");

        // MappingPathResolver must still flag this as flex
        assertTrue(MappingPathResolver.shouldUseFlexPath(mapping));
    }

    /**
     * Unknown JSON keys must be silently ignored (verifies @JsonIgnoreProperties).
     */
    @Test
    void testUnknownJsonFieldsAreIgnored() throws Exception {
        String json = "{\"sourceColumn\":1,\"startCell\":\"C5\",\"direction\":\"vertical\","
                + "\"fillField\":2,\"spaceField\":0,\"startField\":1,"
                + "\"unknownFutureField\":\"someValue\",\"anotherField\":42}";

        // Must not throw
        ColumnMapping mapping = assertDoesNotThrow(() -> mapper.readValue(json, ColumnMapping.class));
        assertEquals(2, mapping.getFillField());
    }

    // -----------------------------------------------------------------------
    // Null flex fields → no path detection
    // -----------------------------------------------------------------------

    @Test
    void testNullFillFieldNotDetectedAsFlex() {
        ColumnMapping mapping = new ColumnMapping();
        mapping.setFillField(null);

        assertFalse(MappingPathResolver.shouldUseFlexPath(mapping),
                "null fillField must not trigger flex path");
    }

    // -----------------------------------------------------------------------
    // Semicolon Mode path
    // -----------------------------------------------------------------------

    @Test
    void testSemicolonFieldsRoundTrip() throws Exception {
        ColumnMapping original = new ColumnMapping();
        original.setDelimiterMode("semicolon");
        original.setSourceColumn(0);
        original.setDirection("vertical");
        original.setStartCell("C3");
        original.setBlockRelativeRow(2);
        original.setFieldIndex(1);
        original.setFixed(true);
        original.setGroupWidth(3);

        String json = mapper.writeValueAsString(original);

        assertTrue(json.contains("\"delimiterMode\":\"semicolon\""));
        assertTrue(json.contains("\"blockRelativeRow\":2"));
        assertTrue(json.contains("\"fieldIndex\":1"));
        assertTrue(json.contains("\"fixed\":true"));
        assertTrue(json.contains("\"groupWidth\":3"));

        ColumnMapping loaded = mapper.readValue(json, ColumnMapping.class);

        assertEquals("semicolon", loaded.getDelimiterMode());
        assertEquals(2, loaded.getBlockRelativeRow());
        assertEquals(1, loaded.getFieldIndex());
        assertEquals(true, loaded.getFixed());
        assertEquals(3, loaded.getGroupWidth());
    }
}
