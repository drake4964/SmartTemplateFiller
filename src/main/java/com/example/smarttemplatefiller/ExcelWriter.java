package com.example.smarttemplatefiller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    public static void writeAdvancedMappedFile(File txtFile, File mappingFile, File outputFile) {
        try {
            List<List<String>> data = TxtParser.parseFile(txtFile);

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> mappings = new ArrayList<>();

            JsonNode root = mapper.readTree(mappingFile);

            if (root.isArray()) {
                // V1 Legacy Format (Array of mappings)
                mappings = mapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {
                });
            } else if (root.isObject() && root.has("mappings")) {
                // V2 Format (Object with mappings list) - Convert to legacy structure
                JsonNode v2Mappings = root.get("mappings");
                for (JsonNode m : v2Mappings) {
                    Map<String, Object> map = new HashMap<>();

                    // Convert column (String "A" or "1") to Index (int 0)
                    String srcColStr = m.has("sourceColumn") ? m.get("sourceColumn").asText() : "A";
                    map.put("sourceColumn", parseColumnIndex(srcColStr));

                    map.put("startCell", m.has("targetCell") ? m.get("targetCell").asText() : "A1");
                    map.put("direction", m.has("direction") ? m.get("direction").asText().toLowerCase() : "vertical");
                    if (m.has("includeTitle") && m.get("includeTitle").asBoolean()) {
                        // Titles not fully supported in simple V2 conversion without specific title
                        // text field
                        // defaulting to empty or ignoring
                    }

                    // V2 assumes all rows by default, synthesize rowIndexes
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++)
                        indexes.add(i);
                    map.put("rowIndexes", indexes);

                    mappings.add(map);
                }
            } else {
                // Fallback or invalid
                System.err.println("Warning: Unrecognized mapping file format");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Result");

                for (Map<String, Object> mapping : mappings) {
                    Object srcColObj = mapping.get("sourceColumn");
                    int sourceColumn = srcColObj instanceof Number ? ((Number) srcColObj).intValue() : 0;

                    String startCell = (String) mapping.get("startCell");
                    String direction = (String) mapping.get("direction");
                    String title = mapping.containsKey("title") ? mapping.get("title").toString() : "";

                    // BUG-001 FIX: Use CellReference for proper parsing (supports AA, AB, etc.)
                    CellReference ref = new CellReference(startCell);
                    int startRow = ref.getRow();
                    int startCol = ref.getCol();

                    // Determine row indexes
                    List<Integer> rowIndexes = new ArrayList<>();
                    if (mapping.containsKey("rowPattern")) {
                        Map<String, Object> rowPattern = (Map<String, Object>) mapping.get("rowPattern");
                        // BUG-005 FIX: Use Number.intValue() for safe casting
                        int start = ((Number) rowPattern.get("start")).intValue();
                        String type = (String) rowPattern.get("type");
                        rowIndexes = TxtParser.generateIndexes(data.size(), type, start);
                    } else if (mapping.containsKey("rowIndexes")) {
                        List<Object> rawList = (List<Object>) mapping.get("rowIndexes");
                        for (Object obj : rawList) {
                            rowIndexes.add(((Number) obj).intValue());
                        }
                    }

                    // BUG-002 FIX: Only write title if row > 0 and title is not empty
                    if (!title.isEmpty()) {
                        if (direction.equals("vertical")) {
                            if (startRow > 0) {
                                Row titleRow = sheet.getRow(startRow - 1);
                                if (titleRow == null)
                                    titleRow = sheet.createRow(startRow - 1);
                                Cell cell = titleRow.createCell(startCol);
                                cell.setCellValue(title);
                            }
                        } else {
                            Row row = sheet.getRow(startRow);
                            if (row == null)
                                row = sheet.createRow(startRow);
                            Cell titleCell = row.createCell(startCol);
                            titleCell.setCellValue(title);
                        }
                    }

                    // Write data
                    for (int i = 0; i < rowIndexes.size(); i++) {
                        int rowIndex = rowIndexes.get(i);
                        if (rowIndex >= data.size() || rowIndex < 0)
                            continue;
                        List<String> rowData = data.get(rowIndex);
                        // Safe index check
                        String value = "";
                        if (sourceColumn >= 0 && sourceColumn < rowData.size()) {
                            value = rowData.get(sourceColumn);
                        }

                        if (direction.equals("vertical")) {
                            Row row = sheet.getRow(startRow + i);
                            if (row == null)
                                row = sheet.createRow(startRow + i);
                            row.createCell(startCol).setCellValue(value);
                        } else {
                            Row row = sheet.getRow(startRow);
                            if (row == null)
                                row = sheet.createRow(startRow);
                            row.createCell(startCol + i + 1).setCellValue(value);
                        }
                    }
                }

                // BUG-007 FIX: Use try-with-resources for FileOutputStream
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    workbook.write(out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseColumnIndex(String column) {
        if (column == null || column.isEmpty()) {
            return 0;
        }

        // Check if it's a letter (A, B, C, etc.)
        if (Character.isLetter(column.charAt(0))) {
            column = column.toUpperCase();
            int index = 0;
            for (int i = 0; i < column.length(); i++) {
                index = index * 26 + (column.charAt(i) - 'A' + 1);
            }
            return index - 1; // 0-indexed
        }

        // Try parsing as number
        try {
            int val = Integer.parseInt(column);
            // Assuming "1" means index 0 if it's user input?
            // V1 used "0" for Col 1? Let's assume user input "1" is index 0 for consistency
            // with V2 "A"=0
            // Actually usually "1" -> 0.
            return val > 0 ? val - 1 : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
