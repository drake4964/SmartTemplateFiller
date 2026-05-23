package com.example.smarttemplatefiller;

import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

class UserVerificationTest {

    @Test
    void generateUserOutcomes() throws Exception {
        File ftxDir = new File("FTX");
        
        File results3 = new File(ftxDir, "Results2 - sample 3 sets.txt");
        List<List<String>> data = TxtParser.parseFile(results3);
        System.out.println("=== PARSED DATA ROWS ===");
        System.out.println("Total rows: " + data.size());
        List<Integer> blockStarts = new ArrayList<>();
        blockStarts.add(0);
        for (int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            if (row.size() == 1 && "@101".equals(row.get(0).trim())) {
                System.out.println("Found @101 at data row: " + r);
            }
            if (!row.isEmpty() && "@101".equals(row.get(0).trim())) {
                if (r + 1 < data.size()) {
                    blockStarts.add(r + 1);
                }
            }
        }
        System.out.println("Block starts: " + blockStarts);

        File txt4 = new File(ftxDir, "Txt4.json");
        File outcome3sets = new File(ftxDir, "output888 - outcome 3sets.xlsx");
        
        System.out.println("Generating 3-set outcome...");
        ExcelWriter.writeAdvancedMappedFile(results3, txt4, outcome3sets);
        System.out.println("3-set outcome generated successfully!");

        // Compare headers of output999.xlsx, output888 - expected 3sets.xlsx, and output888 - outcome 3sets.xlsx
        printHeaders(new File(ftxDir, "output888 - expected 3sets.xlsx"), "EXPECTED 3SETS");
        printHeaders(new File(ftxDir, "output888 - outcome 3sets.xlsx"), "OUTCOME 3SETS");
        File output999 = new File(ftxDir, "output999.xlsx");
        if (output999.exists()) {
            printHeaders(output999, "OUTPUT 999");
        } else {
            System.out.println("output999.xlsx does not exist!");
        }
    }

    private void printHeaders(File file, String label) throws Exception {
        System.out.println("=== " + label + " HEADERS ===");
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            if (row == null) {
                System.out.println("Row 0 is NULL");
                return;
            }
            for (int c = 0; c < 20; c++) {
                Cell cell = row.getCell(c);
                String val = (cell == null) ? "NULL" : cell.toString();
                char colLetter = (char) ('A' + c);
                System.out.println(colLetter + "1: " + val);
            }
        }
    }
}


