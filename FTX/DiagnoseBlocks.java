import java.io.*;
import java.util.*;

/**
 * Diagnostic to trace parseMultiLineGroupedBlock output and blockStarts detection.
 * Run with: java DiagnoseBlocks.java "Results2 - sample 3 sets.txt"
 */
public class DiagnoseBlocks {
    
    static List<String> HEADERS = List.of("Element", "Actual", "Nominal", "Deviat.", "Up Tol.", "Low Tol.", "Pass/Fail");

    public static void main(String[] args) throws Exception {
        File inputFile = new File("FTX/QV Results Example No looping.txt");
        System.out.println("Diagnosing file: " + inputFile.getAbsolutePath());

        List<List<String>> data = com.example.smarttemplatefiller.TxtParser.parseMultiLineGroupedBlock(inputFile);
        System.out.println("Parsed " + data.size() + " rows.");

        File outputFile = new File("FTX/result QV.csv");
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            for (List<String> row : data) {
                pw.println(String.join(",", row));
            }
        }
        System.out.println("Wrote output to: " + outputFile.getAbsolutePath());
        
        System.out.println("\n=== blockStarts detection ===");
        List<Integer> blockStarts = new ArrayList<>();
        blockStarts.add(0);
        for (int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            if (!row.isEmpty() && "@101".equals(row.get(0).trim())) {
                System.out.printf("  @101 found at data[%d], adding blockStart=%d%n", r, r+1);
                if (r + 1 < data.size()) {
                    blockStarts.add(r + 1);
                }
            }
        }
        System.out.println("blockStarts = " + blockStarts);
        
        System.out.println("\n=== block0Size ===");
        int block0Size = 0;
        for (int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            if (!row.isEmpty() && "@101".equals(row.get(0).trim())) {
                break;
            }
            block0Size++;
        }
        System.out.println("block0Size = " + block0Size);
        
        System.out.println("\n=== isSemicolonFile check ===");
        boolean hasSemicolon = data.stream().anyMatch(row -> row.size() > 1);
        boolean hasAt101 = data.stream().anyMatch(row -> !row.isEmpty() && "@101".equals(row.get(0).trim()));
        System.out.println("hasSemicolon = " + hasSemicolon);
        System.out.println("hasAt101 = " + hasAt101);
        System.out.println("isSemicolonFile = " + (hasSemicolon && hasAt101));
    }
    
    static List<List<String>> parseMultiLineGroupedBlock(File file) throws Exception {
        List<List<String>> result = new ArrayList<>();
        List<String> headers = HEADERS;
        result.add(headers);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentHeader = null;
            boolean hasParsedData = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if ("@101".equals(line)) {
                    System.out.println("  Parser sees @101, hasParsedData=" + hasParsedData);
                    if (hasParsedData) {
                        result.add(List.of("@101"));
                        result.add(headers);
                    }
                    continue;
                }

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
                    for (String val : values) { row.add(val); }
                    while (row.size() < 7) row.add("");
                    result.add(row);
                    hasParsedData = true;
                }
            }
        }
        return result;
    }
}
