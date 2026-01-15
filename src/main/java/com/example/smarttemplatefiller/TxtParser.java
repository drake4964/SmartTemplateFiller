package com.example.smarttemplatefiller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TxtParser {

    public static List<List<String>> parseFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> previewLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && previewLines.size() < 10) {
                previewLines.add(line);
            }

            boolean isBlockHeader = previewLines.stream()
                    .anyMatch(l -> l.matches("(?i)(Circle|Line|Plane|Point|Distance|Angle).*\\(ID:.*\\).*"));
            boolean isFixedColumn = previewLines.stream().anyMatch(l -> l.matches("\\s*\\d+\\s+N\\d+\\s+.*\\s+\\*+.*"));

            if (isBlockHeader) {
                return parseMultiLineGroupedBlock(file);
            } else if (isFixedColumn) {
                return parseFixedColumnTable(file);
            } else {
                return parseFlatTable(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static List<List<String>> parseFixedColumnTable(File file) {
        Map<String, Integer> columnWidths = loadColumnConfig();
        List<List<String>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> row = new ArrayList<>();
                int cursor = 0;
                for (Integer width : columnWidths.values()) {
                    if (cursor >= line.length()) {
                        row.add("");
                        cursor += width;
                        continue;
                    }
                    int end = Math.min(cursor + width, line.length());
                    String field = line.substring(cursor, end).trim();
                    row.add(field);
                    cursor += width;
                }
                result.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Map<String, Integer> loadColumnConfig() {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File("column_config.json");
        try {
            return mapper.readValue(configFile, LinkedHashMap.class);
        } catch (IOException e) {
            System.err.println("Failed to load column_config.json. Using fallback config.");
            Map<String, Integer> fallback = new LinkedHashMap<>();
            fallback.put("Col1", 3);
            fallback.put("Col2", 6);
            fallback.put("Col3", 9);
            fallback.put("Col4", 4);
            fallback.put("Col5", 11);
            fallback.put("Col6", 11);
            fallback.put("Col7", 11);
            fallback.put("Col8", 11);
            fallback.put("Col9", 12);
            return fallback;
        }
    }

    public static List<List<String>> parseMultiLineGroupedBlock(File file) {
        List<List<String>> result = new ArrayList<>();
        result.add(List.of("Element", "Actual", "Nominal", "Deviat.", "Up Tol.", "Low Tol.", "Pass/Fail"));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentHeader = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.matches("(?i)(Circle|Line|Plane|Point|Distance|Angle).*\\(ID:.*\\).*")) {
                    currentHeader = line;
                    continue;
                }

                if (currentHeader != null && line.contains("=")) {
                    String[] parts = line.split("=");
                    String label = parts[0].trim();
                    String valuesPart = parts.length > 1 ? parts[1].trim() : "";
                    String[] values = valuesPart.isEmpty() ? new String[0] : valuesPart.split("\\s+");

                    List<String> row = new ArrayList<>();
                    row.add(currentHeader + " → " + label);

                    for (String val : values) {
                        row.add(val);
                    }

                    while (row.size() < 7)
                        row.add("");
                    result.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<List<String>> parseFlatTable(File file) {
        List<List<String>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\s{2,}");
                List<String> row = new ArrayList<>();
                for (String part : parts) {
                    row.add(part.trim());
                }

                while (row.size() < 8)
                    row.add("");

                result.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Generate row indexes based on pattern type.
     * 
     * @param totalRows   Total number of rows in data
     * @param patternType "odd", "even", or "all"
     * @param startIndex  Starting row index (0-based)
     * @return List of 0-based row indexes matching the pattern
     */
    public static List<Integer> generateIndexes(int totalRows, String patternType, int startIndex) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = startIndex; i < totalRows; i++) {
            // Row numbers are 1-based for user, but indexes are 0-based
            int rowNum = i + 1; // Convert to 1-based row number
            if ("odd".equalsIgnoreCase(patternType) && rowNum % 2 == 1) {
                // Odd rows: 1, 3, 5, 7... (display rows)
                indexes.add(i);
            } else if ("even".equalsIgnoreCase(patternType) && rowNum % 2 == 0) {
                // Even rows: 2, 4, 6, 8... (display rows)
                indexes.add(i);
            } else if ("all".equalsIgnoreCase(patternType)) {
                indexes.add(i);
            }
        }
        return indexes;
    }
}
