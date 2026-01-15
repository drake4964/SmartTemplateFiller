package com.example.smarttemplatefiller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    public static void writeAdvancedMappedFile(File txtFile, File mappingFile, File outputFile) {
        try {
            List<List<String>> data = TxtParser.parseFile(txtFile);

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> mappings = mapper.readValue(
                    mappingFile,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Result");

                for (Map<String, Object> mapping : mappings) {
                    int sourceColumn = ((Number) mapping.get("sourceColumn")).intValue();
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
                        String value = (sourceColumn < rowData.size()) ? rowData.get(sourceColumn) : "";

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
}
