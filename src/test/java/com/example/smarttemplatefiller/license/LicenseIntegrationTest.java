package com.example.smarttemplatefiller.license;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the license verification system.
 * Tests end-to-end validation scenarios combining all user stories.
 * 
 * Phase 8 Tasks:
 * - T086: Valid license → app launches
 * - T087: Missing license → error dialog
 * - T088: Expired license → error with default message
 * - T089: Custom config shows custom values
 * - T090: Disabled checking bypasses validation
 * - T091-T093: Utility tool integration (manual + automated)
 */
class LicenseIntegrationTest {

    @TempDir
    Path tempDir;

    private File configFile;
    private File licenseFile;

    // Test hardware identifiers
    private static final List<String> TEST_MACS = List.of("00:1A:2B:3C:4D:5E", "AA:BB:CC:DD:EE:FF");
    private static final String TEST_SERIAL = "TEST-SERIAL-123";

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("license_config.json").toFile();
        licenseFile = tempDir.resolve("license.json").toFile();
    }

    /**
     * Writes a license configuration file.
     */
    private void writeConfig(boolean enabled, String errorMessage, String logoPath, String licenseFilePath)
            throws Exception {
        String json = String.format("""
                {
                    "enabled": %s,
                    "errorMessage": %s,
                    "logoPath": %s,
                    "licenseFilePath": "%s"
                }
                """,
                enabled,
                errorMessage != null ? "\"" + errorMessage + "\"" : "null",
                logoPath != null ? "\"" + logoPath.replace("\\", "\\\\") + "\"" : "null",
                licenseFilePath.replace("\\", "\\\\"));

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        }
    }

    /**
     * Writes a valid license file for the test hardware.
     */
    private void writeValidLicense() throws Exception {
        long futureExpiry = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();
        String licenseJson = TestLicenseGenerator.generateLicenseJson(TEST_MACS, TEST_SERIAL, futureExpiry);
        try (FileWriter writer = new FileWriter(licenseFile)) {
            writer.write(licenseJson);
        }
    }

    /**
     * Writes an expired license file for the test hardware.
     */
    private void writeExpiredLicense() throws Exception {
        long pastExpiry = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        String licenseJson = TestLicenseGenerator.generateLicenseJson(TEST_MACS, TEST_SERIAL, pastExpiry);
        try (FileWriter writer = new FileWriter(licenseFile)) {
            writer.write(licenseJson);
        }
    }

    // =========================================================================
    // T086: Valid license → app launches → main window visible
    // =========================================================================

    @Nested
    @DisplayName("T086: Valid License Scenario")
    class ValidLicenseTests {

        @Test
        @DisplayName("Valid license with matching hardware → validation succeeds")
        void testValidLicenseMatchingHardware() throws Exception {
            // Given: Valid config and license
            writeConfig(true, "Error", null, licenseFile.getAbsolutePath());
            writeValidLicense();

            // And: Hardware that matches the license
            MockHardwareIdentifier hardware = new MockHardwareIdentifier(TEST_MACS, TEST_SERIAL);

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Should succeed
            assertTrue(result.isValid(), "Valid license with matching hardware should pass");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("Valid license allows application to proceed")
        void testValidLicenseAllowsAppToLaunch() throws Exception {
            // Given: Valid configuration
            writeConfig(true, null, null, licenseFile.getAbsolutePath());
            writeValidLicense();

            MockHardwareIdentifier hardware = new MockHardwareIdentifier(TEST_MACS, TEST_SERIAL);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);

            // When: Validate returns success
            ValidationResult result = validator.validate();

            // Then: isValid() returns true, allowing app to continue
            assertTrue(result.isValid());
            // In real integration, this would allow MainApp.start() to proceed to main
            // window
        }
    }

    // =========================================================================
    // T087: Missing license → error dialog → app exits
    // =========================================================================

    @Nested
    @DisplayName("T087: Missing License Scenario")
    class MissingLicenseTests {

        @Test
        @DisplayName("Missing license file → returns FILE_NOT_FOUND error")
        void testMissingLicenseReturnsError() throws Exception {
            // Given: Config pointing to non-existent license
            File nonExistent = tempDir.resolve("nonexistent.json").toFile();
            writeConfig(true, "License not found", null, nonExistent.getAbsolutePath());

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Should fail with FILE_NOT_FOUND
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.FILE_NOT_FOUND, result.getErrorCode());
        }

        @Test
        @DisplayName("Missing license returns custom error message from config")
        void testMissingLicenseReturnsCustomMessage() throws Exception {
            // Given: Config with custom error message
            String customMessage = "Please contact support@example.com";
            File nonExistent = tempDir.resolve("missing.json").toFile();
            writeConfig(true, customMessage, null, nonExistent.getAbsolutePath());

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Error message should be the custom one
            assertFalse(result.isValid());
            assertEquals(customMessage, result.getErrorMessage());
        }
    }

    // =========================================================================
    // T088: Expired license → error dialog with default message
    // =========================================================================

    @Nested
    @DisplayName("T088: Expired License Scenario")
    class ExpiredLicenseTests {

        @Test
        @DisplayName("Expired license → returns LICENSE_EXPIRED error")
        void testExpiredLicenseReturnsError() throws Exception {
            // Given: Config and expired license
            writeConfig(true, "License expired", null, licenseFile.getAbsolutePath());
            writeExpiredLicense();

            MockHardwareIdentifier hardware = new MockHardwareIdentifier(TEST_MACS, TEST_SERIAL);

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Should fail with LICENSE_EXPIRED
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.LICENSE_EXPIRED, result.getErrorCode());
        }

        @Test
        @DisplayName("Expired license shows error message")
        void testExpiredLicenseShowsMessage() throws Exception {
            // Given: Config with error message
            String errorMessage = "Your license has expired";
            writeConfig(true, errorMessage, null, licenseFile.getAbsolutePath());
            writeExpiredLicense();

            MockHardwareIdentifier hardware = new MockHardwareIdentifier(TEST_MACS, TEST_SERIAL);

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Error message from config should be returned
            assertFalse(result.isValid());
            assertEquals(errorMessage, result.getErrorMessage());
        }
    }

    // =========================================================================
    // T089: Custom config (message + logo) → error dialog shows custom values
    // =========================================================================

    @Nested
    @DisplayName("T089: Custom Configuration Scenario")
    class CustomConfigTests {

        @Test
        @DisplayName("Custom error message is returned in ValidationResult")
        void testCustomErrorMessageReturned() throws Exception {
            // Given: Config with custom error message
            String customMessage = "Contact sales@company.com for license renewal";
            File nonExistent = tempDir.resolve("nolicense.json").toFile();
            writeConfig(true, customMessage, null, nonExistent.getAbsolutePath());

            // When: Validation fails
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Custom message is available for display
            assertFalse(result.isValid());
            assertEquals(customMessage, result.getErrorMessage());
        }

        @Test
        @DisplayName("Config with logo path is loaded correctly")
        void testConfigWithLogoPath() throws Exception {
            // Given: Config with logo path
            File logoFile = tempDir.resolve("logo.png").toFile();
            logoFile.createNewFile(); // Create empty file

            writeConfig(true, "Error", logoFile.getAbsolutePath(),
                    tempDir.resolve("missing.json").toFile().getAbsolutePath());

            // When: Load config
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: Logo path is set correctly
            assertEquals(logoFile.getAbsolutePath().replace("\\", "/"),
                    config.getLogoPath().replace("\\", "/"));
        }

        @Test
        @DisplayName("LicenseConfig loads all custom values correctly")
        void testAllCustomValuesLoaded() throws Exception {
            // Given: Config with all custom values
            String customMessage = "Custom error message";
            File logoFile = tempDir.resolve("custom_logo.png").toFile();
            logoFile.createNewFile();

            writeConfig(true, customMessage, logoFile.getAbsolutePath(),
                    licenseFile.getAbsolutePath());

            // When: Load config
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: All values loaded
            assertTrue(config.isEnabled());
            assertEquals(customMessage, config.getErrorMessage());
            assertNotNull(config.getLogoPath());
            assertEquals(licenseFile.getAbsolutePath().replace("\\", "/"),
                    config.getLicenseFilePath().replace("\\", "/"));
        }
    }

    // =========================================================================
    // T090: Disabled license checking (US4) → app launches without license
    // =========================================================================

    @Nested
    @DisplayName("T090: Disabled License Checking (Dev Mode)")
    class DisabledCheckingTests {

        @Test
        @DisplayName("enabled=false → validation bypassed, returns success")
        void testDisabledValidationReturnsSuccess() throws Exception {
            // Given: Config with enabled=false
            File nonExistent = tempDir.resolve("nolicense.json").toFile();
            writeConfig(false, "Should not see this", null, nonExistent.getAbsolutePath());

            // When: Validate (no license file exists)
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Should succeed without checking license
            assertTrue(result.isValid(), "Should bypass validation when disabled");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        @Test
        @DisplayName("enabled=false → app can launch without any license")
        void testAppLaunchesWithoutLicense() throws Exception {
            // Given: Disabled validation with no license
            writeConfig(false, null, null, "/nonexistent/license.json");

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Validation succeeds, allowing app launch
            assertTrue(result.isValid());
            // In real scenario, MainApp would proceed to show main window
        }

        @Test
        @DisplayName("enabled=false with expired license → still succeeds (skips all checks)")
        void testDisabledWithExpiredLicenseSucceeds() throws Exception {
            // Given: Disabled validation with expired license
            writeConfig(false, null, null, licenseFile.getAbsolutePath());
            writeExpiredLicense();

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Should succeed (doesn't even read the license)
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // T091-T093: Utility Tool Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("T091-T093: Utility Tool Workflow Integration")
    class UtilityToolIntegrationTests {

        /**
         * T091: Test that a license generated by TestLicenseGenerator validates
         * correctly.
         * This simulates the LicenseGenerator tool workflow.
         */
        @Test
        @DisplayName("T091: Generated license validates with matching hardware")
        void testGeneratedLicenseValidates() throws Exception {
            // Given: Config and license generated for specific hardware
            writeConfig(true, "Error", null, licenseFile.getAbsolutePath());

            // Generate license for specific hardware (simulates LicenseGenerator)
            List<String> customerMacs = List.of("11:22:33:44:55:66");
            String customerSerial = "CUSTOMER-BOARD-001";
            long expiry = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();

            String licenseJson = TestLicenseGenerator.generateLicenseJson(
                    customerMacs, customerSerial, expiry);
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write(licenseJson);
            }

            // When: Validate with matching hardware (simulates customer's machine)
            MockHardwareIdentifier hardware = new MockHardwareIdentifier(customerMacs, customerSerial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Validation succeeds
            assertTrue(result.isValid(), "License should validate on matching hardware");
        }

        /**
         * T092: Test that hardware extraction + license generation + validation works.
         * This simulates the full HardwareInfoExtractor → LicenseGenerator → App
         * workflow.
         */
        @Test
        @DisplayName("T092: Hardware extraction → license generation → validation workflow")
        void testFullLicenseWorkflow() throws Exception {
            // Step 1: Simulate HardwareInfoExtractor output
            List<String> extractedMacs = List.of("AA:BB:CC:DD:EE:00", "AA:BB:CC:DD:EE:01");
            String extractedSerial = "EXTRACTED-SERIAL-XYZ";

            // Step 2: Simulate LicenseGenerator creating license from extracted hardware
            long expiry = Instant.now().plus(180, ChronoUnit.DAYS).toEpochMilli();
            String generatedLicense = TestLicenseGenerator.generateLicenseJson(
                    extractedMacs, extractedSerial, expiry);

            // Step 3: Write license to file
            writeConfig(true, null, null, licenseFile.getAbsolutePath());
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write(generatedLicense);
            }

            // Step 4: Validate with same hardware (customer runs app)
            MockHardwareIdentifier hardware = new MockHardwareIdentifier(extractedMacs, extractedSerial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Full workflow succeeds
            assertTrue(result.isValid(), "Full workflow should produce valid license");
            assertEquals(ValidationResult.ErrorCode.SUCCESS, result.getErrorCode());
        }

        /**
         * T093: Test that renewed license validates correctly.
         * This simulates the LicenseRenewer workflow.
         */
        @Test
        @DisplayName("T093: Renewed license validates correctly")
        void testRenewedLicenseValidates() throws Exception {
            // Given: Original license with near-expiry date
            List<String> macs = List.of("DE:AD:BE:EF:00:01");
            String serial = "RENEW-TEST-SERIAL";

            // Create "old" license (still valid but about to expire)
            long oldExpiry = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();
            String oldLicense = TestLicenseGenerator.generateLicenseJson(macs, serial, oldExpiry);

            // Simulate renewal: same deviceId, new expiry
            // In real LicenseRenewer, it would decrypt, extract deviceId, and re-encrypt
            long newExpiry = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();
            String renewedLicense = TestLicenseGenerator.generateLicenseJson(macs, serial, newExpiry);

            // Write renewed license
            writeConfig(true, null, null, licenseFile.getAbsolutePath());
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write(renewedLicense);
            }

            // When: Validate with original hardware
            MockHardwareIdentifier hardware = new MockHardwareIdentifier(macs, serial);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Renewed license validates
            assertTrue(result.isValid(), "Renewed license should validate");
        }

        /**
         * Test that license fails with different hardware.
         * Ensures hardware binding works correctly.
         */
        @Test
        @DisplayName("License fails validation on different hardware")
        void testLicenseFailsOnDifferentHardware() throws Exception {
            // Given: License generated for hardware A
            List<String> originalMacs = List.of("11:11:11:11:11:11");
            String originalSerial = "ORIGINAL-SERIAL";

            long expiry = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();
            String license = TestLicenseGenerator.generateLicenseJson(originalMacs, originalSerial, expiry);

            writeConfig(true, "Hardware mismatch", null, licenseFile.getAbsolutePath());
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write(license);
            }

            // When: Validate with different hardware
            MockHardwareIdentifier differentHardware = new MockHardwareIdentifier(
                    List.of("99:99:99:99:99:99"), "DIFFERENT-SERIAL");
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), differentHardware);
            ValidationResult result = validator.validate();

            // Then: Should fail with hardware mismatch
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.HARDWARE_MISMATCH, result.getErrorCode());
        }
    }

    // =========================================================================
    // Additional Integration Scenarios
    // =========================================================================

    @Nested
    @DisplayName("Cross-Cutting Integration Scenarios")
    class CrossCuttingTests {

        @Test
        @DisplayName("Corrupted license file → INVALID_FORMAT error")
        void testCorruptedLicenseFile() throws Exception {
            // Given: Config pointing to corrupted license
            writeConfig(true, "Invalid license", null, licenseFile.getAbsolutePath());
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write("{ this is not valid JSON");
            }

            // When: Validate
            LicenseValidator validator = new DefaultLicenseValidator(configFile.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Should fail with format error
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.INVALID_FORMAT, result.getErrorCode());
        }

        @Test
        @DisplayName("Tampered license signature → SIGNATURE_INVALID error")
        void testTamperedSignature() throws Exception {
            // Given: Valid license with tampered signature
            writeConfig(true, "Signature error", null, licenseFile.getAbsolutePath());

            String tamperedLicense = TestLicenseGenerator.generateTamperedSignatureLicense(
                    TEST_MACS, TEST_SERIAL);
            try (FileWriter writer = new FileWriter(licenseFile)) {
                writer.write(tamperedLicense);
            }

            // When: Validate
            MockHardwareIdentifier hardware = new MockHardwareIdentifier(TEST_MACS, TEST_SERIAL);
            LicenseValidator validator = new DefaultLicenseValidator(
                    configFile.getAbsolutePath(), hardware);
            ValidationResult result = validator.validate();

            // Then: Should fail with signature invalid
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.SIGNATURE_MISMATCH, result.getErrorCode());
        }

        @Test
        @DisplayName("Missing config file → uses default values, fails on missing license")
        void testMissingConfigFile() throws Exception {
            // Given: Non-existent config file
            File missingConfig = tempDir.resolve("nonexistent_config.json").toFile();

            // When: Create validator with missing config
            LicenseValidator validator = new DefaultLicenseValidator(missingConfig.getAbsolutePath());
            ValidationResult result = validator.validate();

            // Then: Uses defaults (enabled=true), fails because license not found
            assertFalse(result.isValid());
            assertEquals(ValidationResult.ErrorCode.FILE_NOT_FOUND, result.getErrorCode());
        }
    }
}
