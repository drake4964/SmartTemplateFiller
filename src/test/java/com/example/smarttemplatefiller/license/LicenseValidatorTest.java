package com.example.smarttemplatefiller.license;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

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

        @Test
        @DisplayName("T031: valid license with matching deviceId and future expiry → success")
        void testValidLicenseWithMatchingHardware() throws Exception {
            // Given: valid license for specific hardware
            List<String> macs = List.of("00:1A:2B:3C:4D:5E");
            String serial = "ABC123456789";
            String licenseJson = TestLicenseGenerator.generateValidLicense(macs, serial);
            writeLicenseFile(licenseJson);

            // And: config with enabled=true
            writeConfig(true, licenseFile.getAbsolutePath());

            // And: hardware that matches the license
            HardwareIdentifier mockHardware = new MockHardwareIdentifier(macs, serial);

            // When: validate is called
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return success
            assertTrue(result.isValid(), "Valid license should pass validation");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("T032: valid license with multiple MACs, one matching → success")
        void testValidLicenseWithMultipleMacsOneMatching() throws Exception {
            // Given: license generated with multiple MAC addresses
            List<String> licenseMacs = List.of("00:1A:2B:3C:4D:5E", "AA:BB:CC:DD:EE:FF");
            String serial = "XYZ987654321";
            String licenseJson = TestLicenseGenerator.generateValidLicense(licenseMacs, serial);
            writeLicenseFile(licenseJson);

            // And: config with enabled=true
            writeConfig(true, licenseFile.getAbsolutePath());

            // And: hardware with same MACs (in any order) and matching serial
            HardwareIdentifier mockHardware = new MockHardwareIdentifier(
                    List.of("AA:BB:CC:DD:EE:FF", "00:1A:2B:3C:4D:5E"), // Different order
                    serial);

            // When: validate is called
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return success (MACs are sorted before comparison)
            assertTrue(result.isValid(), "License with multiple MACs should validate when hardware matches");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("Validation should complete in under 2 seconds (SC-003)")
        void testValidationPerformance() throws Exception {
            // Given: valid license setup
            List<String> macs = List.of("00:1A:2B:3C:4D:5E");
            String serial = "PERF123456";
            String licenseJson = TestLicenseGenerator.generateValidLicense(macs, serial);
            writeLicenseFile(licenseJson);
            writeConfig(true, licenseFile.getAbsolutePath());

            HardwareIdentifier mockHardware = new MockHardwareIdentifier(macs, serial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);

            // When: validate is called and timed
            long startTime = System.currentTimeMillis();
            ValidationResult result = validator.validate();
            long elapsed = System.currentTimeMillis() - startTime;

            // Then: should complete in under 2 seconds per SC-003
            assertTrue(result.isValid());
            assertTrue(elapsed < 2000, "Validation took " + elapsed + "ms, should be < 2000ms");
        }
    }

    // =========================================================================
    // User Story 2: Invalid/Missing License (to be added in Phase 5)
    // =========================================================================

    @Nested
    @DisplayName("US2: Invalid/Missing License")
    class InvalidLicenseTests {

        @Test
        @DisplayName("T038: missing license file → FILE_NOT_FOUND")
        void testMissingLicenseFile() throws Exception {
            // Given: config enabled but license file doesn't exist
            writeConfig(true, tempDir.resolve("nonexistent_license.json").toString());

            // When: validate is called
            HardwareIdentifier mockHardware = new MockHardwareIdentifier("00:1A:2B:3C:4D:5E", "ABC123");
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return FILE_NOT_FOUND
            assertFalse(result.isValid(), "Missing license should fail validation");
            assertEquals(ValidationResult.ErrorCode.FILE_NOT_FOUND, result.getErrorCode());
        }

        @Test
        @DisplayName("T039: invalid JSON format → INVALID_FORMAT")
        void testInvalidJsonFormat() throws Exception {
            // Given: license file with invalid JSON
            writeLicenseFile("{ invalid json content }}}");
            writeConfig(true, licenseFile.getAbsolutePath());

            // When: validate is called
            HardwareIdentifier mockHardware = new MockHardwareIdentifier("00:1A:2B:3C:4D:5E", "ABC123");
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return INVALID_FORMAT
            assertFalse(result.isValid(), "Invalid JSON should fail validation");
            assertEquals(ValidationResult.ErrorCode.INVALID_FORMAT, result.getErrorCode());
        }

        @Test
        @DisplayName("T040: HMAC signature mismatch → SIGNATURE_MISMATCH")
        void testSignatureMismatch() throws Exception {
            // Given: license file with tampered signature
            List<String> macs = List.of("00:1A:2B:3C:4D:5E");
            String serial = "ABC123456789";
            String tamperedLicense = TestLicenseGenerator.generateTamperedSignatureLicense(macs, serial);
            writeLicenseFile(tamperedLicense);
            writeConfig(true, licenseFile.getAbsolutePath());

            // When: validate is called
            HardwareIdentifier mockHardware = new MockHardwareIdentifier(macs, serial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return SIGNATURE_MISMATCH
            assertFalse(result.isValid(), "Tampered signature should fail validation");
            assertEquals(ValidationResult.ErrorCode.SIGNATURE_MISMATCH, result.getErrorCode());
        }

        @Test
        @DisplayName("T041: incorrect deviceId → HARDWARE_MISMATCH")
        void testHardwareMismatch() throws Exception {
            // Given: license generated for different hardware
            List<String> licenseMacs = List.of("AA:BB:CC:DD:EE:FF");
            String licenseSerial = "LICENSE123";
            String licenseJson = TestLicenseGenerator.generateValidLicense(licenseMacs, licenseSerial);
            writeLicenseFile(licenseJson);
            writeConfig(true, licenseFile.getAbsolutePath());

            // When: validate is called with DIFFERENT hardware
            HardwareIdentifier differentHardware = new MockHardwareIdentifier(
                    List.of("11:22:33:44:55:66"), // Different MAC
                    "DIFFERENT_SERIAL"); // Different serial
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), differentHardware);
            ValidationResult result = validator.validate();

            // Then: should return HARDWARE_MISMATCH
            assertFalse(result.isValid(), "Different hardware should fail validation");
            assertEquals(ValidationResult.ErrorCode.HARDWARE_MISMATCH, result.getErrorCode());
        }

        @Test
        @DisplayName("T042: expired license → LICENSE_EXPIRED")
        void testExpiredLicense() throws Exception {
            // Given: license that has expired
            List<String> macs = List.of("00:1A:2B:3C:4D:5E");
            String serial = "ABC123456789";
            String expiredLicense = TestLicenseGenerator.generateExpiredLicense(macs, serial);
            writeLicenseFile(expiredLicense);
            writeConfig(true, licenseFile.getAbsolutePath());

            // When: validate is called
            HardwareIdentifier mockHardware = new MockHardwareIdentifier(macs, serial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), mockHardware);
            ValidationResult result = validator.validate();

            // Then: should return LICENSE_EXPIRED
            assertFalse(result.isValid(), "Expired license should fail validation");
            assertEquals(ValidationResult.ErrorCode.LICENSE_EXPIRED, result.getErrorCode());
        }

        @Test
        @DisplayName("Error messages should use configured message from license_config.json")
        void testErrorMessageFromConfig() throws Exception {
            // Given: config with custom error message
            String customMessage = "Custom test error message from config";
            String configJson = String.format("""
                    {
                        "enabled": true,
                        "errorMessage": "%s",
                        "logoPath": null,
                        "licenseFilePath": "%s"
                    }
                    """, customMessage, tempDir.resolve("nonexistent.json").toString().replace("\\", "\\\\"));
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(configJson);
            }

            // When: validate fails
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: error message should come from config
            assertFalse(result.isValid());
            assertEquals(customMessage, result.getErrorMessage());
        }
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
