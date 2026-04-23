package com.example.smarttemplatefiller.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnMapping {
    private Integer sourceColumn;
    private String startCell;
    private String direction;
    private String title;
    
    // Legacy fields
    private Map<String, Object> rowPattern;
    private List<Integer> rowIndexes;

    // Flex Pattern fields
    private Integer startField;
    private Integer fillField;
    private Integer spaceField;

    public ColumnMapping() {}

    public Integer getSourceColumn() { return sourceColumn; }
    public void setSourceColumn(Integer sourceColumn) { this.sourceColumn = sourceColumn; }

    public String getStartCell() { return startCell; }
    public void setStartCell(String startCell) { this.startCell = startCell; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Map<String, Object> getRowPattern() { return rowPattern; }
    public void setRowPattern(Map<String, Object> rowPattern) { this.rowPattern = rowPattern; }

    public List<Integer> getRowIndexes() { return rowIndexes; }
    public void setRowIndexes(List<Integer> rowIndexes) { this.rowIndexes = rowIndexes; }

    public Integer getStartField() { return startField; }
    public void setStartField(Integer startField) { this.startField = startField; }

    public Integer getFillField() { return fillField; }
    public void setFillField(Integer fillField) { this.fillField = fillField; }

    public Integer getSpaceField() { return spaceField; }
    public void setSpaceField(Integer spaceField) { this.spaceField = spaceField; }
}
