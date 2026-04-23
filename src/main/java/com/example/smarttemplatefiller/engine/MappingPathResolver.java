package com.example.smarttemplatefiller.engine;

import com.example.smarttemplatefiller.mapping.ColumnMapping;
import java.util.Map;

public class MappingPathResolver {

    /**
     * Determines whether the mapping uses the new flex parameters or the legacy map-based fallback.
     */
    public static boolean shouldUseFlexPath(Map<String, Object> mappingNode) {
        return mappingNode.containsKey("fillField") || 
               mappingNode.get("fillField") != null;
    }

    public static boolean shouldUseFlexPath(ColumnMapping mapping) {
        return mapping.getFillField() != null;
    }
}
