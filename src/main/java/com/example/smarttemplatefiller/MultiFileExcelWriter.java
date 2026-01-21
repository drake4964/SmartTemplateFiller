package com.example.smarttemplatefiller;

import com.example.smarttemplatefiller.model.MappingConfiguration;
import com.example.smarttemplatefiller.model.MultiFileMapping;
import com.example.smarttemplatefiller.service.MultiFileMergeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel writer that supports multi-file merge using MultiFileMergeService.
 * Works alongside existing ExcelWriter for backward compatibility.
 */
public class MultiFileExcelWriter {

    private final MultiFileMergeService mergeService;

    public MultiFileExcelWriter() {
        this.mergeService = new MultiFileMergeService();
    }

    /**
     * Write multiple input files to Excel using a mapping configuration.
     * 
     * @param inputFilesData Map of slot number to parsed file data
     * @param config         Mapping configuration with file slots
     * @param outputPath     Path to output Excel file
     */
    public void write(Map<Integer, List<List<String>>> inputFilesData,
            MappingConfiguration config,
            String outputPath) throws IOException {

        Path output = Paths.get(outputPath);
        mergeService.merge(inputFilesData, config, output);
    }

    /**
     * Check if a mapping configuration is multi-file (v2.0 schema).
     */
    public static boolean isMultiFile(MappingConfiguration config) {
        return config.isMultiFile();
    }

    /**
     * Convert legacy mapping format to MappingConfiguration.
     * For single-file backward compatibility.
     */
    public static MappingConfiguration convertLegacyMapping(List<Map<String, Object>> legacyMappings) {
        MappingConfiguration config = MappingConfiguration.createSingleFile();

        for (Map<String, Object> legacy : legacyMappings) {
            MultiFileMapping mapping = new MultiFileMapping();
            mapping.setSourceFileSlot(1); // Single file = slot 1

            if (legacy.containsKey("sourceColumn")) {
                int colIndex = ((Number) legacy.get("sourceColumn")).intValue();
                mapping.setSourceColumn(String.valueOf((char) ('A' + colIndex)));
            }

            if (legacy.containsKey("startCell")) {
                mapping.setTargetCell((String) legacy.get("startCell"));
            }

            if (legacy.containsKey("direction")) {
                String dir = (String) legacy.get("direction");
                mapping.setDirection(
                        "vertical".equalsIgnoreCase(dir)
                                ? com.example.smarttemplatefiller.model.Direction.VERTICAL
                                : com.example.smarttemplatefiller.model.Direction.HORIZONTAL);
            }

            config.addMapping(mapping);
        }

        return config;
    }
}
