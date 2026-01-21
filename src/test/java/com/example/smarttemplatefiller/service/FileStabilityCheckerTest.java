package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.util.FileStabilityChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileStabilityChecker.
 */
class FileStabilityCheckerTest {

    @TempDir
    Path tempDir;

    @Test
    void testIsStable_ExistingCompleteFile_True() throws IOException {
        // Create a file and let it settle
        Path testFile = tempDir.resolve("stable.txt");
        Files.writeString(testFile, "test content");

        FileStabilityChecker checker = new FileStabilityChecker(1); // 1 second check

        boolean stable = checker.isStable(testFile);

        assertTrue(stable);
    }

    @Test
    void testIsStable_NonExistentFile_False() {
        Path nonExistent = tempDir.resolve("does_not_exist.txt");

        FileStabilityChecker checker = new FileStabilityChecker(1);

        assertFalse(checker.isStable(nonExistent));
    }

    @Test
    void testIsReadable_ExistingFile_True() throws IOException {
        Path testFile = tempDir.resolve("readable.txt");
        Files.writeString(testFile, "content");

        FileStabilityChecker checker = new FileStabilityChecker(1);

        assertTrue(checker.isReadable(testFile));
    }

    @Test
    void testConstructor_ValidRange() {
        assertDoesNotThrow(() -> new FileStabilityChecker(1));
        assertDoesNotThrow(() -> new FileStabilityChecker(30));
    }

    @Test
    void testConstructor_InvalidRange_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new FileStabilityChecker(0));
        assertThrows(IllegalArgumentException.class, () -> new FileStabilityChecker(31));
    }

    @Test
    void testGetStabilityCheckSeconds() {
        FileStabilityChecker checker = new FileStabilityChecker(5);
        assertEquals(5, checker.getStabilityCheckSeconds());
    }
}
