package com.example.smarttemplatefiller.license;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents external configuration for license system behavior.
 * Loaded from license_config.json file.
 */
public class LicenseConfig {

    private static final Logger LOGGER = Logger.getLogger(LicenseConfig.class.getName());

    /** Default error message shown when license validation fails */
    private static final String DEFAULT_ERROR_MESSAGE = "This application requires a valid license to run. Please contact your administrator for assistance.";

    /** Default path to license file */
    private static final String DEFAULT_LICENSE_PATH = "license.json";

    @JsonProperty("enabled")
    private boolean enabled = true;

    @JsonProperty("errorMessage")
    private String errorMessage = DEFAULT_ERROR_MESSAGE;

    @JsonProperty("logoPath")
    private String logoPath;

    @JsonProperty("licenseFilePath")
    private String licenseFilePath = DEFAULT_LICENSE_PATH;

    /**
     * Default constructor with default values.
     */
    public LicenseConfig() {
    }

    /**
     * Creates a configuration with all values specified.
     *
     * @param enabled         Whether license checking is active
     * @param errorMessage    Custom error message for failed validation
     * @param logoPath        Path to company logo image
     * @param licenseFilePath Path to license key file
     */
    public LicenseConfig(boolean enabled, String errorMessage, String logoPath, String licenseFilePath) {
        this.enabled = enabled;
        this.errorMessage = errorMessage != null ? errorMessage : DEFAULT_ERROR_MESSAGE;
        this.logoPath = logoPath;
        this.licenseFilePath = licenseFilePath != null ? licenseFilePath : DEFAULT_LICENSE_PATH;
    }

    /**
     * Loads configuration from a file path.
     *
     * @param filePath Path to the configuration JSON file
     * @return Loaded configuration or default configuration if file not found
     */
    public static LicenseConfig load(String filePath) {
        try {
            File configFile = new File(filePath);
            if (!configFile.exists()) {
                LOGGER.log(Level.INFO, "Config file not found at {0}, using defaults", filePath);
                return new LicenseConfig();
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(configFile, LicenseConfig.class);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load config from " + filePath + ", using defaults", e);
            return new LicenseConfig();
        }
    }

    /**
     * Loads configuration from classpath resource.
     *
     * @param resourcePath Classpath resource path (e.g., "/license_config.json")
     * @return Loaded configuration or default configuration if resource not found
     */
    public static LicenseConfig loadFromResource(String resourcePath) {
        try (InputStream is = LicenseConfig.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.log(Level.INFO, "Config resource not found at {0}, using defaults", resourcePath);
                return new LicenseConfig();
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, LicenseConfig.class);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load config from resource " + resourcePath + ", using defaults", e);
            return new LicenseConfig();
        }
    }

    /**
     * Checks if license checking is enabled.
     *
     * @return true if license validation should be performed
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether license checking is enabled.
     *
     * @param enabled true to enable license checking
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the error message to display on validation failure.
     *
     * @return Custom or default error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage Message to display on validation failure
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the path to the company logo image.
     *
     * @return Logo path or null if no logo configured
     */
    public String getLogoPath() {
        return logoPath;
    }

    /**
     * Sets the logo path.
     *
     * @param logoPath Path to company logo image file
     */
    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    /**
     * Gets the path to the license file.
     *
     * @return License file path
     */
    public String getLicenseFilePath() {
        return licenseFilePath;
    }

    /**
     * Sets the license file path.
     *
     * @param licenseFilePath Path to license key file
     */
    public void setLicenseFilePath(String licenseFilePath) {
        this.licenseFilePath = licenseFilePath;
    }

    @Override
    public String toString() {
        return "LicenseConfig{enabled=" + enabled +
                ", licenseFilePath='" + licenseFilePath + "'" +
                ", hasLogo=" + (logoPath != null) + "}";
    }
}
