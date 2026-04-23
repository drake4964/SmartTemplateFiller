package com.example.smarttemplatefiller.mapping;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// T014 [US2]: Tests for Start offset and sequence exhaustion (FR-010)
class RowPatternDescriptorTest {

    @Test
    void testStandardFillAndSpace() {
        // Start: 1, Fill: 3, Space: 1
        RowPatternDescriptor descriptor = new RowPatternDescriptor(1, 3, 1);
        List<Map.Entry<Integer, Integer>> sequence = descriptor.generateOutputSequence(10).collect(Collectors.toList());
        
        // Output indexes: 0, 1, 2, 3, 4, 5
        // Source rows:    0, 1, 2, 4, 5, 6
        assertEquals(0, sequence.get(0).getKey());
        assertEquals(0, sequence.get(0).getValue());
        
        assertEquals(1, sequence.get(1).getKey());
        assertEquals(1, sequence.get(1).getValue());
        
        assertEquals(2, sequence.get(2).getKey());
        assertEquals(2, sequence.get(2).getValue());
        
        // Skipping source row 3!
        assertEquals(3, sequence.get(3).getKey());
        assertEquals(4, sequence.get(3).getValue());
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new RowPatternDescriptor(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new RowPatternDescriptor(1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RowPatternDescriptor(1, 1, -1));
    }

    // T014 [US2] — Start offset tests (FR-010)

    /**
     * Independent Test for Phase 4: Set Start Field to 4, verify the first exported
     * Excel output maps to source row index 3 (0-based = source row 4 in 1-based terms).
     */
    @Test
    void testStartOffsetFour() {
        // Start=4: skip rows 0,1,2 (1-based rows 1,2,3); first read is row index 3
        RowPatternDescriptor descriptor = new RowPatternDescriptor(4, 1, 0);
        List<Map.Entry<Integer, Integer>> sequence = descriptor.generateOutputSequence(8).collect(Collectors.toList());

        assertFalse(sequence.isEmpty(), "Sequence should not be empty");
        // First output position (0) maps to source row index 3 (= start 4, 0-based)
        assertEquals(0, sequence.get(0).getKey(),  "First output index should be 0");
        assertEquals(3, sequence.get(0).getValue(), "First source row index should be 3 (source row 4 in 1-based)");

        // Subsequent rows continue sequentially from offset
        assertEquals(1, sequence.get(1).getKey());
        assertEquals(4, sequence.get(1).getValue());

        assertEquals(2, sequence.get(2).getKey());
        assertEquals(5, sequence.get(2).getValue());
    }

    /**
     * FR-010: When startField exceeds totalSourceRows, the sequence is empty (exhausted immediately).
     */
    @Test
    void testExhaustionWhenStartBeyondTotalRows() {
        // Only 3 rows, but start at row 5 — nothing to export
        RowPatternDescriptor descriptor = new RowPatternDescriptor(5, 1, 0);
        List<Map.Entry<Integer, Integer>> sequence = descriptor.generateOutputSequence(3).collect(Collectors.toList());

        assertTrue(sequence.isEmpty(), "Sequence should be empty when start > totalSourceRows");
    }

    /**
     * FR-010: Mid-sequence exhaustion — pattern truncates cleanly when source data runs out
     * partway through a fill+space cycle.
     */
    @Test
    void testExhaustionMidSequence() {
        // Start=1, Fill=3, Space=1, total=5 source rows (0-based: 0,1,2,3,4)
        // Cycle 0: reads rows 0,1,2 (fill=3); skips row 3 (space=1)
        // Cycle 1 would read rows 4,5,6 but only row 4 exists → stops after row 4
        RowPatternDescriptor descriptor = new RowPatternDescriptor(1, 3, 1);
        List<Map.Entry<Integer, Integer>> sequence = descriptor.generateOutputSequence(5).collect(Collectors.toList());

        // Expected output: outputPos→sourceRow: 0→0, 1→1, 2→2, 3→4
        assertEquals(4, sequence.size(), "Should produce exactly 4 entries before exhaustion");
        assertEquals(0, sequence.get(0).getValue());
        assertEquals(1, sequence.get(1).getValue());
        assertEquals(2, sequence.get(2).getValue());
        assertEquals(4, sequence.get(3).getValue());
    }

    /**
     * Continuous fill with no spacing from a non-trivial Start offset (space=0).
     * Start=3 means skip rows 0,1 (1-based rows 1,2) and read from row index 2 onward.
     */
    @Test
    void testNoSpacingWithStartOffset() {
        // Start=3, Fill=2, Space=0 — reads every 2 rows continuously from source row index 2
        RowPatternDescriptor descriptor = new RowPatternDescriptor(3, 2, 0);
        List<Map.Entry<Integer, Integer>> sequence = descriptor.generateOutputSequence(7).collect(Collectors.toList());

        // Source rows available after offset: indices 2,3,4,5,6 → 5 entries
        assertEquals(5, sequence.size());
        assertEquals(2, sequence.get(0).getValue());
        assertEquals(3, sequence.get(1).getValue());
        assertEquals(4, sequence.get(2).getValue());
        assertEquals(5, sequence.get(3).getValue());
        assertEquals(6, sequence.get(4).getValue());
    }
}
