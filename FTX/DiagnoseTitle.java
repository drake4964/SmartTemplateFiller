import java.io.*;
import java.util.*;

/**
 * Deep diagnostic to trace title-column computation for non-fixed mappings.
 */
public class DiagnoseTitle {
    
    static List<String> HEADERS = List.of("Element", "Actual", "Nominal", "Deviat.", "Up Tol.", "Low Tol.", "Pass/Fail");
    static String[] COL_LETTERS = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"};

    // Simple CellReference parser (col-letter to 0-based index)
    static int colLetterToIndex(String cell) {
        // e.g. "E2" -> 4
        String colPart = cell.replaceAll("[0-9]", "");
        int result = 0;
        for (char c : colPart.toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }
    
    static int rowNumToIndex(String cell) {
        // e.g. "E2" -> 1 (0-based)
        String numPart = cell.replaceAll("[A-Za-z]", "");
        return Integer.parseInt(numPart) - 1;
    }

    public static void main(String[] args) throws Exception {
        // Simulate the Txt4.json mappings
        List<Map<String, Object>> mappings = new ArrayList<>();
        
        String[][] defs = {
            {"0", "A2", "vertical", "true", "Element", "2", "1", "0"},
            {"1", "B2", "vertical", "true", "Actuak",  "2", "1", "0"},
            {"2", "C2", "vertical", "true", "Nominal", "2", "1", "0"},
            {"3", "D2", "vertical", "true", "Deviation.", "2", "1", "0"},
            {"4", "E2", "vertical", "false", "Up Tol.", "2", "1", "0"},
            {"5", "F2", "vertical", "false", "Low Tol.", "2", "1", "0"},
            {"6", "G2", "vertical", "false", "Pass/Fail", "2", "1", "0"},
        };
        
        for (String[] d : defs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sourceColumn", Integer.parseInt(d[0]));
            m.put("startCell", d[1]);
            m.put("direction", d[2]);
            m.put("fixed", Boolean.parseBoolean(d[3]));
            m.put("title", d[4]);
            m.put("startField", Integer.parseInt(d[5]));
            m.put("fillField", Integer.parseInt(d[6]));
            m.put("spaceField", Integer.parseInt(d[7]));
            mappings.add(m);
        }
        
        List<Integer> blockStarts = List.of(0, 18, 36, 54);
        int rowOffset = 0;
        
        System.out.println("=== Title Writing Trace ===");
        System.out.println("blockStarts = " + blockStarts);
        System.out.println();
        
        for (Map<String, Object> mapping : mappings) {
            String title = mapping.get("title").toString();
            boolean isFixed = Boolean.TRUE.equals(mapping.get("fixed"));
            String startCell = (String) mapping.get("startCell");
            String direction = (String) mapping.get("direction");
            
            int startRow = rowNumToIndex(startCell);
            int startCol = colLetterToIndex(startCell);
            int baseStartRow = (rowOffset > 0) ? Math.max(startRow, rowOffset) : startRow;
            
            if (title.isEmpty()) continue;
            if (rowOffset != 0) continue; // Only write titles for rowOffset==0
            
            System.out.println("Mapping: " + startCell + " title='" + title + "' fixed=" + isFixed);
            
            for (int cavityIndex = 0; cavityIndex < blockStarts.size(); cavityIndex++) {
                if (isFixed && cavityIndex > 0) {
                    System.out.println("  cavity " + cavityIndex + " SKIPPED (isFixed && cavityIndex>0)");
                    continue;
                }
                
                int targetTitleCol;
                if (isFixed) {
                    targetTitleCol = startCol;
                } else {
                    int targetBaseRow = baseStartRow; // same for vertical or horizontal
                    
                    List<Map<String, Object>> group = new ArrayList<>();
                    for (Map<String, Object> m : mappings) {
                        boolean mFixed = m.containsKey("fixed") && Boolean.TRUE.equals(m.get("fixed"));
                        if (!mFixed) {
                            String mStartCell = (String) m.get("startCell");
                            int mBaseRow = rowNumToIndex(mStartCell);
                            int mTargetRow = mBaseRow; // both branches same
                            
                            if (mTargetRow == targetBaseRow && direction.equals(m.get("direction"))) {
                                group.add(m);
                            }
                        }
                    }
                    // Sort by col
                    group.sort(Comparator.comparingInt(m -> colLetterToIndex((String) m.get("startCell"))));
                    
                    int groupWidth = group.size();
                    if (groupWidth == 0) groupWidth = 1;
                    int offsetInGroup = 0;
                    for (int g = 0; g < group.size(); g++) {
                        if (group.get(g) == mapping) {
                            offsetInGroup = g;
                            break;
                        }
                    }
                    
                    int groupStartCol = group.isEmpty() ? startCol : colLetterToIndex((String) group.get(0).get("startCell"));
                    
                    System.out.println("  group size=" + group.size() + " groupStartCol=" + groupStartCol + " groupWidth=" + groupWidth + " offsetInGroup=" + offsetInGroup);
                    
                    if ("vertical".equals(direction)) {
                        targetTitleCol = groupStartCol + (cavityIndex * groupWidth) + offsetInGroup;
                    } else {
                        targetTitleCol = groupStartCol + 0 + 1 + (cavityIndex * groupWidth) + offsetInGroup;
                    }
                }
                
                // Compute title Excel address
                int excelRow = baseStartRow - 1; // 0-based row index for title
                String colLetter = targetTitleCol < COL_LETTERS.length ? COL_LETTERS[targetTitleCol] : "?" + targetTitleCol;
                
                System.out.printf("  cavity %d -> WRITE title '%s' at %s%d%n", 
                    cavityIndex, title, colLetter, excelRow + 1);
            }
            System.out.println();
        }
    }
}
