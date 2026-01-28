package com.example.smarttemplatefiller.license;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of LicenseValidator.
 * Implements config-check-first logic to support development mode bypass.
 */
public class DefaultLicenseValidator implements LicenseValidator {

    private static final Logger LOGGER = Logger.getLogger(DefaultLicenseValidator.class.getName());

    private final String configFilePath;
    private final HardwareIdentifier hardwareIdentifier;
    private final EncryptionValidator encryptionValidator;
    private final LicenseFileReader licenseFileReader;

    // Cached config loaded once during validation
    private LicenseConfig config;

    /**
     * Creates a DefaultLicenseValidator with the specified config file path.
     *
     * @param configFilePath Path to license_config.json
     */
    public DefaultLicenseValidator(String configFilePath) {
        this(configFilePath, new OshiHardwareIdentifier());
    }

    /**
     * Creates a DefaultLicenseValidator with custom hardware identifier (for
     * testing).
     *
     * @param configFilePath     Path to license_config.json
     * @param hardwareIdentifier Hardware identifier implementation
     */
    public DefaultLicenseValidator(String configFilePath, HardwareIdentifier hardwareIdentifier) {
        this.configFilePath = configFilePath;
        this.hardwareIdentifier = hardwareIdentifier;
        this.encryptionValidator = new EncryptionValidator();
        this.licenseFileReader = new LicenseFileReader();
    }

    /**
     * Creates a DefaultLicenseValidator that loads config from classpath resource.
     * Uses the default resource path "/license_config.json".
     */
    public DefaultLicenseValidator() {
        this.configFilePath = null; // Will use resource loading
        this.hardwareIdentifier = new OshiHardwareIdentifier();
        this.encryptionValidator = new EncryptionValidator();
        this.licenseFileReader = new LicenseFileReader();
    }

    @Override
    public ValidationResult validate() {
        // Load configuration
        config = loadConfig();

        // T025, T026: Check enabled flag FIRST - if disabled, skip all validation
        if (!config.isEnabled()) {
            // T027: Log that license checking is disabled
            LOGGER.log(Level.INFO, "License checking disabled via configuration");
            return ValidationResult.success();
        }

        // Use configured license file path
        return validate(config.getLicenseFilePath());
    }

    @Override
    public ValidationResult validate(String licenseFilePath) {
        // Ensure config is loaded
        if (config == null) {
            config = loadConfig();
        }

        // If disabled, bypass validation (handles case where validate(path) is called
        // directly)
        if (!config.isEnabled()) {
            LOGGER.log(Level.INFO, "License checking disabled via configuration");
            return ValidationResult.success();
        }

        LOGGER.log(Level.FINE, "Starting license validation");

        // Step 1: Load and parse license file
        LicenseData licenseData;
        try {
            licenseData = licenseFileReader.read(licenseFilePath);
        } catch (LicenseFileReader.LicenseReadException e) {
            LOGGER.log(Level.WARNING, "Failed to read license file: {0}", e.getMessage());
            return mapReadException(e);
        }

        // Step 2: Verify HMAC signature (tamper detection)
        if (!encryptionValidator.verifySignature(
                licenseData.getVersion(),
                licenseData.getEncryptedData(),
                licenseData.getSignature())) {
            LOGGER.log(Level.WARNING, "License signature verification failed");
            return ValidationResult.failure(ValidationResult.ErrorCode.SIGNATURE_MISMATCH,
                    "License file has been tampered with or is invalid");
        }

        // Step 3: Decrypt license data
        EncryptionValidator.PayloadData payload;
        try {
            String decrypted = encryptionValidator.decrypt(licenseData.getEncryptedData());
            payload = encryptionValidator.parsePayload(decrypted);
        } catch (EncryptionValidator.LicenseDecryptionException e) {
            LOGGER.log(Level.WARNING, "Failed to decrypt license: {0}", e.getMessage());
            return ValidationResult.failure(ValidationResult.ErrorCode.DECRYPTION_FAILED,
                    "Failed to decrypt license data");
        }

        // Step 4: Extract current hardware and compute device ID
        CurrentHardwareInfo currentHardware = hardwareIdentifier.getCurrentHardware();
        String currentDeviceId = currentHardware.computeDeviceId();

        // Step 5: Compare device IDs
        if (!payload.getDeviceId().equals(currentDeviceId)) {
            LOGGER.log(Level.WARNING, "Hardware mismatch detected");
            return ValidationResult.failure(ValidationResult.ErrorCode.HARDWARE_MISMATCH,
                    config.getErrorMessage());
        }

        // Step 6: Check expiry
        long currentTime = Instant.now().toEpochMilli();
        if (currentTime > payload.getExpiryTimestamp()) {
            LOGGER.log(Level.WARNING, "License has expired");
            return ValidationResult.failure(ValidationResult.ErrorCode.LICENSE_EXPIRED,
                    config.getErrorMessage());
        }

        LOGGER.log(Level.INFO, "License validation successful");
        return ValidationResult.success();
    }

    /**
     * Loads the license configuration.
     */
    private LicenseConfig loadConfig() {
        if (configFilePath != null) {
            return LicenseConfig.load(configFilePath);
        } else {
            // Load from classpath resource
            return LicenseConfig.loadFromResource("/license_config.json");
        }
    }

    /**
     * Maps LicenseReadException to appropriate ValidationResult.
     */
    private ValidationResult mapReadException(LicenseFileReader.LicenseReadException e) {
        return switch (e.getErrorType()) {
            case FILE_NOT_FOUND -> ValidationResult.failure(
                    ValidationResult.ErrorCode.FILE_NOT_FOUND,
                    config != null ? config.getErrorMessage() : e.getMessage());
            case INVALID_PATH, INVALID_FORMAT -> ValidationResult.failure(
                    ValidationResult.ErrorCode.INVALID_FORMAT,
                    config != null ? config.getErrorMessage() : e.getMessage());
        };
    }
}
