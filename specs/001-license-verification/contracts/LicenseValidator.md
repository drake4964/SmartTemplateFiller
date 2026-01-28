# Interface Contract: LicenseValidator

**Feature**: 001-license-verification  
**Date**: 2026-01-28  
**Purpose**: Main orchestrator for license validation logic

## Interface Definition

```java
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
     * @throws RuntimeException if configuration file cannot be loaded
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
```

## Implementation Notes

**Default Implementation**: `DefaultLicenseValidator`

**Dependencies**:
- `LicenseFileReader` - Parse JSON license file
- `HardwareIdentifier` - Extract current system hardware
- `HmacValidator` - Verify cryptographic signature
- `LicenseConfig` - Load configuration settings

**Error Handling**:
- Missing config file → Use default configuration (enabled=true, default message)
- Missing license file → Return `ValidationResult.failure(FILE_NOT_FOUND, ...)`
- JSON parse errors → Return `ValidationResult.failure(INVALID_FORMAT, ...)`
- Hardware extraction errors → Return `ValidationResult.failure(SIGNATURE_MISMATCH, ...)`

**Performance Requirements**:
- Total validation time MUST be < 2 seconds (per SC-003)
- Configuration and license file reads are synchronous (blocking)

## Usage Example

```java
// In MainApp.start()
LicenseValidator validator = new DefaultLicenseValidator();
ValidationResult result = validator.validate();

if (!result.isValid()) {
    LicenseErrorDialog.show(result.getErrorMessage());
    Platform.exit();
    return;
}

// Continue normal application startup
```

## Testing Strategy

**Unit Tests**:
- Mock `HardwareIdentifier` to return controlled MAC/serial values
- Test each error code path (missing file, expired, hardware mismatch, etc.)
- Verify configuration disable flag bypasses validation

**Integration Tests**:
- Create valid license file with known hardware identifiers
- Extract actual hardware and verify validation succeeds
- Modify license file (corrupt signature) and verify failure
