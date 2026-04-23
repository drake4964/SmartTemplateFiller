package com.example.smarttemplatefiller.engine;

import com.example.smarttemplatefiller.mapping.ColumnMapping;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MappingPathResolverTest {

    @Test
    void testShouldUseFlexPathWithMap() {
        Map<String, Object> legacyMapping = new HashMap<>();
        legacyMapping.put("rowIndexes", java.util.List.of(1, 2, 3));
        assertFalse(MappingPathResolver.shouldUseFlexPath(legacyMapping));

        Map<String, Object> flexMapping = new HashMap<>();
        flexMapping.put("fillField", 5);
        assertTrue(MappingPathResolver.shouldUseFlexPath(flexMapping));
    }

    @Test
    void testShouldUseFlexPathWithDTO() {
        ColumnMapping legacy = new ColumnMapping();
        legacy.setRowIndexes(java.util.List.of(1, 2, 3));
        assertFalse(MappingPathResolver.shouldUseFlexPath(legacy));

        ColumnMapping flex = new ColumnMapping();
        flex.setFillField(5);
        assertTrue(MappingPathResolver.shouldUseFlexPath(flex));
    }
}
