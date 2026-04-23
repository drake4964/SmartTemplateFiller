package com.example.smarttemplatefiller.mapping;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.AbstractMap;
import java.util.Map;

public class RowPatternDescriptor {
    private final int startField;
    private final int fillField;
    private final int spaceField;

    public RowPatternDescriptor(int startField, int fillField, int spaceField) {
        if (startField < 1) throw new IllegalArgumentException("Start field must be >= 1");
        if (fillField < 1) throw new IllegalArgumentException("Fill field must be >= 1");
        if (spaceField < 0) throw new IllegalArgumentException("Space field must be >= 0");
        
        this.startField = startField;
        this.fillField = fillField;
        this.spaceField = spaceField;
    }

    public int getStartField() { return startField; }
    public int getFillField() { return fillField; }
    public int getSpaceField() { return spaceField; }

    /**
     * Given the total number of source rows, this generates a stream of map entries
     * where the key is the index in the Excel output (0-based) and the value is the 
     * 0-based sourceRowIndex that should be read from the source data.
     */
    public Stream<Map.Entry<Integer, Integer>> generateOutputSequence(long totalSourceRows) {
        if (startField > totalSourceRows) return Stream.empty();
        
        // Output indexes mapped to Source Indexes
        return IntStream.iterate(0, i -> i + 1)
                .<Map.Entry<Integer, Integer>>mapToObj(outputPos -> {
                    int cycleLength = fillField + spaceField;
                    int cycleIndex = outputPos / fillField;
                    int offsetWithinCycle = outputPos % fillField;
                    
                    int sourceRow0Indexed = (startField - 1) + (cycleIndex * cycleLength) + offsetWithinCycle;
                    
                    if (sourceRow0Indexed >= totalSourceRows) {
                        return null; // Exhausted
                    }
                    return new AbstractMap.SimpleEntry<>(outputPos, sourceRow0Indexed);
                })
                .takeWhile(Objects::nonNull);
    }
}
