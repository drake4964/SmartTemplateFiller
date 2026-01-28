package com.example.smarttemplatefiller.license;

/**
 * Validates license files against current hardware and expiry constraints.
 * 
 * This is the main entry point for license verification, called during
 * application startup before the main window is displayed.
 */
public interface LicenseValidator {

    /**
     * Validates the license file located at the configured path.
     * 
     * Validation steps:
     * 1. Check if license checking is enabled in configuration
     * 2. Load license file from configured path
     * 3. Extract current hardware identifiers (MAC, motherboard serial)
     * 4. Verify HMAC signature
     * 5. Compare hardware identifiers
     * 6. Check expiry date
     *
     * @return ValidationResult indicating success or specific failure reason
     */
    ValidationResult validate();

    /**
     * Validates a specific license file (for testing).
     *
     * @param licenseFilePath absolute path to license file
     * @return ValidationResult indicating success or specific failure reason
     */
    ValidationResult validate(String licenseFilePath);
}
