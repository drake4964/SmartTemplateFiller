# License Verification Feature

> **Status**: Complete  
> **Version**: 1.0  
> **Last Updated**: 2026-01-29

## Overview

The License Verification feature protects SmartTemplateFiller by requiring a valid license file that binds to specific hardware. This prevents unauthorized use while maintaining a good user experience for legitimate customers.

## User Stories

| ID | Story | Status |
|----|-------|--------|
| US1 | Block application start if no valid license | ✅ |
| US2 | Bind license to hardware (MAC + motherboard) | ✅ |
| US3 | Display custom error messages and branding | ✅ |
| US4 | Dev mode bypass for development builds | ✅ |

## Architecture

```
src/main/java/com/example/smarttemplatefiller/license/
├── LicenseValidator.java          # Interface for validation
├── DefaultLicenseValidator.java   # Main implementation
├── ValidationResult.java          # Result with error codes
├── LicenseConfig.java             # JSON configuration loader
├── LicenseData.java               # License file structure
├── EncryptionValidator.java       # AES-256 + HMAC cryptography
├── HardwareIdentifier.java        # Interface for hardware detection
├── OshiHardwareIdentifier.java    # OSHI-based implementation
└── LicenseErrorDialog.java        # Error dialog UI
```

## Configuration

The feature is controlled via `license_config.json`:

```json
{
    "enabled": true,
    "licenseFilePath": "license.json",
    "errorMessage": "Contact support@example.com",
    "logoPath": "/path/to/custom/logo.png"
}
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `enabled` | boolean | `true` | Set to `false` for dev builds |
| `licenseFilePath` | string | `"license.json"` | Path to license file |
| `errorMessage` | string | Default message | Custom error message |
| `logoPath` | string | `null` | Path to custom logo |

## License File Format

```json
{
    "v": "1.0",
    "d": "<base64-encoded-encrypted-data>",
    "s": "<base64-encoded-hmac-signature>"
}
```

## Cryptographic Design

- **Encryption**: AES-256-CBC with random IV
- **Integrity**: HMAC-SHA256 signature over version + encrypted data
- **Device Binding**: SHA-256 hash of MAC addresses + motherboard serial
- **Expiry**: Unix timestamp embedded in encrypted payload

## Utility Tools

Three admin/customer tools are provided:

| Tool | For | Purpose |
|------|-----|---------|
| HardwareInfoExtractor | Customers | Extract MAC addresses and motherboard serial |
| LicenseGenerator | Admins | Generate encrypted license files |
| LicenseRenewer | Admins | Extend license expiry without re-generating |

## Deployment

### For Development

1. Set `enabled: false` in `license_config.json`
2. Build and run normally - license validation is bypassed

### For Production

1. Set `enabled: true` in `license_config.json`
2. Bundle the app with empty `license.json` placeholder
3. Customer runs HardwareInfoExtractor, sends output to admin
4. Admin runs LicenseGenerator with hardware info and expiry date
5. Admin sends license file to customer
6. Customer places `license.json` in app directory

## Testing

All tests are in `src/test/java/com/example/smarttemplatefiller/license/`:

- `LicenseValidatorTest.java` - Unit tests for validation logic
- `LicenseConfigTest.java` - Configuration loading tests
- `LicenseDataTest.java` - Data model tests
- `EncryptionValidatorTest.java` - Cryptographic tests
- `LicenseIntegrationTest.java` - End-to-end scenarios

Run tests: `.\gradlew.bat test`

## Security Considerations

- Secret key is obfuscated but recommend ProGuard/R8 for production
- Hardware IDs are never logged
- LicenseGenerator and LicenseRenewer are admin-only (contain secret key)
- Only HardwareInfoExtractor should be distributed to customers
