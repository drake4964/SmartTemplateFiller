package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.MatchingStrategy;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileMatchingService.
 */
class FileMatchingServiceTest {

    @Test
    void testExtractMatchKey_PrefixStrategy_WithUnderscore() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        assertEquals("PART001", service.extractMatchKey("PART001_001.txt"));
        assertEquals("PART001", service.extractMatchKey("PART001_002.txt"));
        assertEquals("JOB", service.extractMatchKey("JOB_A_B.txt"));
    }

    @Test
    void testExtractMatchKey_PrefixStrategy_NoUnderscore() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        // No underscore - returns full basename
        assertEquals("REPORT", service.extractMatchKey("REPORT.txt"));
        assertEquals("DATA", service.extractMatchKey("DATA.csv"));
    }

    @Test
    void testExtractMatchKey_ExactBasenameStrategy() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.EXACT_BASENAME);

        assertEquals("PART001_001", service.extractMatchKey("PART001_001.txt"));
        assertEquals("PART001_002", service.extractMatchKey("PART001_002.txt"));
    }

    @Test
    void testFilesMatch_SamePrefix_True() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        Path file1 = Path.of("C:/folder1/PART001_001.txt");
        Path file2 = Path.of("C:/folder2/PART001_002.txt");

        assertTrue(service.filesMatch(file1, file2));
    }

    @Test
    void testFilesMatch_DifferentPrefix_False() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        Path file1 = Path.of("C:/folder1/PART001_001.txt");
        Path file2 = Path.of("C:/folder2/PART002_001.txt");

        assertFalse(service.filesMatch(file1, file2));
    }

    @Test
    void testFilesMatch_ExactBasename_SameName() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.EXACT_BASENAME);

        Path file1 = Path.of("C:/folder1/DATA.txt");
        Path file2 = Path.of("C:/folder2/DATA.txt");

        assertTrue(service.filesMatch(file1, file2));
    }

    @Test
    void testFindCommonMatchKey_AllMatch() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        Map<Integer, Path> slotFiles = new HashMap<>();
        slotFiles.put(1, Path.of("PART001_001.txt"));
        slotFiles.put(2, Path.of("PART001_002.txt"));
        slotFiles.put(3, Path.of("PART001_003.txt"));

        Optional<String> key = service.findCommonMatchKey(slotFiles);

        assertTrue(key.isPresent());
        assertEquals("PART001", key.get());
    }

    @Test
    void testFindCommonMatchKey_NoMatch() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        Map<Integer, Path> slotFiles = new HashMap<>();
        slotFiles.put(1, Path.of("PART001_001.txt"));
        slotFiles.put(2, Path.of("PART002_001.txt"));

        Optional<String> key = service.findCommonMatchKey(slotFiles);

        assertFalse(key.isPresent());
    }

    @Test
    void testGroupByMatchKey() {
        FileMatchingService service = new FileMatchingService(MatchingStrategy.PREFIX);

        List<Path> files = Arrays.asList(
                Path.of("PART001_001.txt"),
                Path.of("PART001_002.txt"),
                Path.of("PART002_001.txt"));

        Map<String, List<Path>> grouped = service.groupByMatchKey(files);

        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("PART001").size());
        assertEquals(1, grouped.get("PART002").size());
    }
}
