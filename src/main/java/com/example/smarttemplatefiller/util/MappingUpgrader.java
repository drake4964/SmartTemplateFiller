package com.example.smarttemplatefiller.util;

import com.example.smarttemplatefiller.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Utility for upgrading v1.0 mapping configurations to v2.0 format.
 * Provides backward compatibility for existing single-file mappings.
 */
public class MappingUpgrader {
    private static final Logger logger = LoggerFactory.getLogger(MappingUpgrader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Check if a mapping file is v1.0 format (needs upgrade).
     * 
     * @param mappingFile Path to the mapping JSON file
     * @return true if file needs upgrade to v2.0
     */
    public boolean needsUpgrade(Path mappingFile) throws IOException {
        JsonNode root = objectMapper.readTree(mappingFile.toFile());

        // v2.0 has schemaVersion field
        if (root.has("schemaVersion")) {
            String version = root.get("schemaVersion").asText();
            return !version.startsWith("2.");
        }

        // No schemaVersion = v1.0
        return true;
    }

    /**
     * Upgrade a v1.0 mapping JSON to v2.0 format.
     * V1.0 mappings are assumed to be single-file (sourceFileSlot = 1).
     * 
     * @param legacyJson The v1.0 JSON content
     * @return Upgraded MappingConfiguration
     */
    public MappingConfiguration upgrade(JsonNode legacyJson) {
        logger.info("Upgrading legacy v1.0 mapping to v2.0 format");

        MappingConfiguration config = new MappingConfiguration();
        config.setSchemaVersion(MappingConfiguration.CURRENT_SCHEMA_VERSION);

        // Add default single file slot
        config.getFileSlots().add(new FileSlot(1, "Default Input"));

        // Convert legacy mappings
        if (legacyJson.has("mappings") && legacyJson.get("mappings").isArray()) {
            for (JsonNode mappingNode : legacyJson.get("mappings")) {
                MultiFileMapping mapping = new MultiFileMapping();
                mapping.setSourceFileSlot(1); // v1.0 is always single-file

                if (mappingNode.has("sourceColumn")) {
                    mapping.setSourceColumn(mappingNode.get("sourceColumn").asText());
                }
                if (mappingNode.has("targetCell")) {
                    mapping.setTargetCell(mappingNode.get("targetCell").asText());
                }
                if (mappingNode.has("direction")) {
                    String dir = mappingNode.get("direction").asText();
                    mapping.setDirection(Direction.valueOf(dir.toUpperCase()));
                }
                if (mappingNode.has("includeTitle")) {
                    mapping.setIncludeTitle(mappingNode.get("includeTitle").asBoolean());
                }

                config.addMapping(mapping);
            }
        }

        logger.info("Upgraded {} mappings to v2.0 format", config.getMappings().size());
        return config;
    }

    /**
     * Load and upgrade a mapping file if needed.
     * 
     * @param mappingFile Path to the mapping JSON file
     * @return MappingConfiguration (upgraded if necessary)
     */
    public MappingConfiguration loadAndUpgrade(Path mappingFile) throws IOException {
        JsonNode root = objectMapper.readTree(mappingFile.toFile());

        if (needsUpgrade(mappingFile)) {
            return upgrade(root);
        }

        // Already v2.0, parse directly
        return objectMapper.readValue(mappingFile.toFile(), MappingConfiguration.class);
    }
}
