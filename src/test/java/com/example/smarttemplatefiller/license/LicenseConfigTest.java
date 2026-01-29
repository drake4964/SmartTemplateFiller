package com.example.smarttemplatefiller.license;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LicenseConfig.
 * Tests JSON deserialization and customization capabilities (US3).
 */
class LicenseConfigTest {

    @TempDir
    Path tempDir;

    // =========================================================================
    // T050: JSON Deserialization Tests
    // =========================================================================

    @Nested
    @DisplayName("T050: JSON Deserialization")
    class JsonDeserializationTests {

        @Test
        @DisplayName("Should deserialize complete config JSON")
        void testCompleteJsonDeserialization() throws IOException {
            // Given: complete config JSON
            File configFile = tempDir.resolve("config.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "errorMessage": "Custom error message",
                        "logoPath": "logo.png",
                        "licenseFilePath": "my-license.json"
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: all fields should be populated
            assertTrue(config.isEnabled());
            assertEquals("Custom error message", config.getErrorMessage());
            assertEquals("logo.png", config.getLogoPath());
            assertEquals("my-license.json", config.getLicenseFilePath());
        }

        @Test
        @DisplayName("Should handle partial JSON with default values")
        void testPartialJsonUsesDefaults() throws IOException {
            // Given: partial config JSON (only enabled field)
            File configFile = tempDir.resolve("partial.json").toFile();
            String json = """
                    {
                        "enabled": false
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: explicit field should be set, others should have defaults
            assertFalse(config.isEnabled());
            assertNotNull(config.getErrorMessage(), "Default error message should be set");
            assertNull(config.getLogoPath(), "No logo path by default");
            assertEquals("license.json", config.getLicenseFilePath(), "Default license path");
        }

        @Test
        @DisplayName("Should parse boolean enabled field correctly")
        void testBooleanEnabledParsing() throws IOException {
            // Given: configs with true and false values
            File configTrue = tempDir.resolve("enabled_true.json").toFile();
            File configFalse = tempDir.resolve("enabled_false.json").toFile();

            try (FileWriter writer = new FileWriter(configTrue)) {
                writer.write("{\"enabled\": true}");
            }
            try (FileWriter writer = new FileWriter(configFalse)) {
                writer.write("{\"enabled\": false}");
            }

            // When: configs are loaded
            LicenseConfig trueConfig = LicenseConfig.load(configTrue.getAbsolutePath());
            LicenseConfig falseConfig = LicenseConfig.load(configFalse.getAbsolutePath());

            // Then: boolean values should be correct
            assertTrue(trueConfig.isEnabled());
            assertFalse(falseConfig.isEnabled());
        }
    }

    // =========================================================================
    // T051: Custom Error Message Tests
    // =========================================================================

    @Nested
    @DisplayName("T051: Custom Error Message")
    class CustomErrorMessageTests {

        @Test
        @DisplayName("Custom errorMessage → dialog displays custom message")
        void testCustomErrorMessageLoaded() throws IOException {
            // Given: config with custom error message
            File configFile = tempDir.resolve("custom_message.json").toFile();
            String customMessage = "Please contact support@company.com for a valid license.";
            String json = String.format("""
                    {
                        "enabled": true,
                        "errorMessage": "%s"
                    }
                    """, customMessage);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: custom message should be available for dialog
            assertEquals(customMessage, config.getErrorMessage());
        }

        @Test
        @DisplayName("Empty error message should be preserved")
        void testEmptyErrorMessage() throws IOException {
            // Given: config with empty error message
            File configFile = tempDir.resolve("empty_message.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "errorMessage": ""
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: empty message should be preserved (dialog can handle it)
            assertEquals("", config.getErrorMessage());
        }

        @Test
        @DisplayName("Message with special characters should be preserved")
        void testMessageWithSpecialCharacters() throws IOException {
            // Given: config with special characters (no multi-line to avoid encoding
            // issues)
            File configFile = tempDir.resolve("special_chars.json").toFile();
            String specialMessage = "Contact us: support@example.com - Phone: +1-555-1234";
            String json = String.format("""
                    {
                        "enabled": true,
                        "errorMessage": "%s"
                    }
                    """, specialMessage);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: message with special characters should be preserved
            assertEquals(specialMessage, config.getErrorMessage());
        }
    }

    // =========================================================================
    // T052: Custom Logo Path Tests
    // =========================================================================

    @Nested
    @DisplayName("T052: Custom Logo Path")
    class CustomLogoPathTests {

        @Test
        @DisplayName("Custom logoPath → ImageView loads custom logo")
        void testCustomLogoPathLoaded() throws IOException {
            // Given: config with custom logo path
            File configFile = tempDir.resolve("custom_logo.json").toFile();
            String logoPath = "C:/company/branding/company_logo.png";
            String json = String.format("""
                    {
                        "enabled": true,
                        "logoPath": "%s"
                    }
                    """, logoPath.replace("\\", "\\\\"));
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: logo path should be available for ImageView loading
            assertEquals(logoPath, config.getLogoPath());
        }

        @Test
        @DisplayName("Relative logo path should be preserved")
        void testRelativeLogoPath() throws IOException {
            // Given: config with relative logo path
            File configFile = tempDir.resolve("relative_logo.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "logoPath": "assets/logo.png"
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: relative path should be preserved
            assertEquals("assets/logo.png", config.getLogoPath());
        }

        @Test
        @DisplayName("Null logo path should be handled")
        void testNullLogoPath() throws IOException {
            // Given: config without logo path
            File configFile = tempDir.resolve("no_logo.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "errorMessage": "Error message only"
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: logo path should be null (dialog will hide ImageView)
            assertNull(config.getLogoPath());
        }
    }

    // =========================================================================
    // T053: Missing Config File Fallback Tests
    // =========================================================================

    @Nested
    @DisplayName("T053: Missing Config File Fallback")
    class MissingConfigFallbackTests {

        @Test
        @DisplayName("Missing config file → fallback to default message")
        void testMissingConfigFallbackToDefaultMessage() {
            // Given: non-existent config file
            String nonExistentPath = tempDir.resolve("does_not_exist.json").toString();

            // When: attempt to load
            LicenseConfig config = LicenseConfig.load(nonExistentPath);

            // Then: should return default config with default message
            assertNotNull(config);
            assertTrue(config.isEnabled(), "Default should be enabled=true");
            assertNotNull(config.getErrorMessage(), "Should have default error message");
            assertTrue(config.getErrorMessage().contains("license"), "Default message should mention license");
        }

        @Test
        @DisplayName("Default config should have all required fields")
        void testDefaultConfigHasRequiredFields() {
            // Given: non-existent config
            LicenseConfig config = LicenseConfig.load("/nonexistent/path/config.json");

            // Then: all fields should have sensible defaults
            assertTrue(config.isEnabled());
            assertNotNull(config.getErrorMessage());
            assertFalse(config.getErrorMessage().isEmpty());
            assertEquals("license.json", config.getLicenseFilePath());
            // logoPath can be null (no default logo)
        }

        @Test
        @DisplayName("Invalid JSON file → fallback to defaults")
        void testInvalidJsonFallbackToDefaults() throws IOException {
            // Given: file with invalid JSON
            File invalidFile = tempDir.resolve("invalid.json").toFile();
            try (FileWriter writer = new FileWriter(invalidFile)) {
                writer.write("{ this is not valid JSON }}}");
            }

            // When: attempt to load
            LicenseConfig config = LicenseConfig.load(invalidFile.getAbsolutePath());

            // Then: should fallback to defaults (not throw exception)
            assertNotNull(config);
            assertTrue(config.isEnabled());
            assertNotNull(config.getErrorMessage());
        }
    }

    // =========================================================================
    // T054: Invalid Logo Path Handling Tests
    // =========================================================================

    @Nested
    @DisplayName("T054: Invalid Logo Path Handling")
    class InvalidLogoPathTests {

        @Test
        @DisplayName("Invalid logoPath → dialog displays without logo (no crash)")
        void testInvalidLogoPathDoesNotCrash() throws IOException {
            // Given: config with non-existent logo path
            File configFile = tempDir.resolve("invalid_logo.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "errorMessage": "License required",
                        "logoPath": "/nonexistent/path/to/logo.png"
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: logo path should be loaded (validation happens in dialog)
            assertEquals("/nonexistent/path/to/logo.png", config.getLogoPath());
            // Note: LicenseErrorDialog.createLogoView() will return null for invalid paths
        }

        @Test
        @DisplayName("Empty logo path should be preserved")
        void testEmptyLogoPath() throws IOException {
            // Given: config with empty logo path
            File configFile = tempDir.resolve("empty_logo.json").toFile();
            String json = """
                    {
                        "enabled": true,
                        "logoPath": ""
                    }
                    """;
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: empty path should be preserved (dialog will hide ImageView)
            assertEquals("", config.getLogoPath());
        }

        @Test
        @DisplayName("Logo path with spaces should be preserved")
        void testLogoPathWithSpaces() throws IOException {
            // Given: config with path containing spaces
            File configFile = tempDir.resolve("path_with_spaces.json").toFile();
            String pathWithSpaces = "C:/My Documents/Company Assets/logo.png";
            String json = String.format("""
                    {
                        "enabled": true,
                        "logoPath": "%s"
                    }
                    """, pathWithSpaces);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json);
            }

            // When: config is loaded
            LicenseConfig config = LicenseConfig.load(configFile.getAbsolutePath());

            // Then: path with spaces should be preserved
            assertEquals(pathWithSpaces, config.getLogoPath());
        }
    }

    // =========================================================================
    // Additional Edge Cases
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Constructor should set default values")
        void testConstructorDefaults() {
            LicenseConfig config = new LicenseConfig();

            assertTrue(config.isEnabled());
            assertNotNull(config.getErrorMessage());
            assertNull(config.getLogoPath());
            assertEquals("license.json", config.getLicenseFilePath());
        }

        @Test
        @DisplayName("Full constructor should use provided values")
        void testFullConstructor() {
            LicenseConfig config = new LicenseConfig(
                    false,
                    "Custom message",
                    "custom/logo.png",
                    "custom/license.json");

            assertFalse(config.isEnabled());
            assertEquals("Custom message", config.getErrorMessage());
            assertEquals("custom/logo.png", config.getLogoPath());
            assertEquals("custom/license.json", config.getLicenseFilePath());
        }

        @Test
        @DisplayName("Setters should update values")
        void testSetters() {
            LicenseConfig config = new LicenseConfig();

            config.setEnabled(false);
            config.setErrorMessage("New message");
            config.setLogoPath("new/logo.png");
            config.setLicenseFilePath("new/license.json");

            assertFalse(config.isEnabled());
            assertEquals("New message", config.getErrorMessage());
            assertEquals("new/logo.png", config.getLogoPath());
            assertEquals("new/license.json", config.getLicenseFilePath());
        }

        @Test
        @DisplayName("toString should not expose sensitive data")
        void testToStringDoesNotExposeSensitiveData() {
            LicenseConfig config = new LicenseConfig(true, "Secret message", "logo.png", "license.json");

            String str = config.toString();

            // toString should contain basic info but not full error message
            assertTrue(str.contains("enabled=true"));
            assertTrue(str.contains("hasLogo=true"));
            // Error message should not be in toString (could be sensitive)
            assertFalse(str.contains("Secret message"));
        }
    }
}
