package com.example.smarttemplatefiller.license;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LicenseValidator.
 * Tests organized by user story for traceability.
 */
class LicenseValidatorTest {

    @TempDir
    Path tempDir;

    private File configFile;
    private File licenseFile;

    @BeforeEach
    void setUp() throws Exception {
        configFile = tempDir.resolve("license_config.json").toFile();
        licenseFile = tempDir.resolve("license.json").toFile();
    }

    /**
     * Helper to write config file with specified values.
     */
    private void writeConfig(boolean enabled, String licenseFilePath) throws Exception {
        String json = String.format("""
                {
                    "enabled": %s,
                    "errorMessage": "Test error message",
                    "logoPath": null,
                    "licenseFilePath": "%s"
                }
                """, enabled, licenseFilePath.replace("\\", "\\\\"));

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        }
    }

    /**
     * Helper to write a dummy license file.
     */
    private void writeLicenseFile(String content) throws Exception {
        try (FileWriter writer = new FileWriter(licenseFile)) {
            writer.write(content);
        }
    }

    // =========================================================================
    // User Story 4: Optional License Checking for Development Builds
    // =========================================================================

    @Nested
    @DisplayName("US4: Dev Mode Bypass")
    class DevModeBypassTests {

        @Test
        @DisplayName("T021: enabled=false → validation bypassed, returns success")
        void testDisabledConfigReturnsSuccess() throws Exception {
            // Given: config with enabled=false
            writeConfig(false, licenseFile.getAbsolutePath());

            // When: validate is called
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: should return success without checking license
            assertTrue(result.isValid(), "Validation should succeed when disabled");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("T022: enabled=false with missing license file → no error, returns success")
        void testDisabledConfigWithMissingLicenseReturnsSuccess() throws Exception {
            // Given: config with enabled=false and non-existent license file
            writeConfig(false, "/nonexistent/path/license.json");

            // When: validate is called (license file doesn't exist)
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: should return success even though license file is missing
            assertTrue(result.isValid(), "Validation should succeed when disabled, even with missing file");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("enabled=false should skip all validation steps immediately")
        void testDisabledConfigSkipsAllValidation() throws Exception {
            // Given: config with enabled=false
            writeConfig(false, "/this/path/should/never/be/accessed/license.json");

            // When: validate is called
            long startTime = System.currentTimeMillis();
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();
            long elapsed = System.currentTimeMillis() - startTime;

            // Then: should return nearly instantly (< 500ms means we didn't try to load
            // files)
            assertTrue(result.isValid());
            assertTrue(elapsed < 500, "Disabled validation should be fast (was " + elapsed + "ms)");
        }
    }

    // =========================================================================
    // User Story 1: Valid License (to be added in Phase 4)
    // =========================================================================

    @Nested
    @DisplayName("US1: Valid License")
    class ValidLicenseTests {
        // T031, T032 will be added in Phase 4
    }

    // =========================================================================
    // User Story 2: Invalid/Missing License (to be added in Phase 5)
    // =========================================================================

    @Nested
    @DisplayName("US2: Invalid/Missing License")
    class InvalidLicenseTests {
        // T038-T042 will be added in Phase 5
    }

    // =========================================================================
    // Config Loading Tests
    // =========================================================================

    @Nested
    @DisplayName("Configuration Loading")
    class ConfigLoadingTests {

        @Test
        @DisplayName("Missing config file uses defaults (enabled=true)")
        void testMissingConfigUsesDefaults() {
            // Given: no config file exists
            String nonExistentConfig = tempDir.resolve("nonexistent_config.json").toString();

            // When: validator is created
            LicenseValidator validator = new DefaultLicenseValidator(nonExistentConfig);

            // Then: should use default config (which has enabled=true by default)
            // This means validation will try to run (and fail due to missing license)
            ValidationResult result = validator.validate();
            assertFalse(result.isValid(), "Default config should have enabled=true");
            assertEquals(ValidationResult.ErrorCode.FILE_NOT_FOUND, result.getErrorCode());
        }
    }
}
