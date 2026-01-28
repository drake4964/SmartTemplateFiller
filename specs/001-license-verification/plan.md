# Implementation Plan: Hardware-Based License Verification

**Branch**: `001-license-verification` | **Date**: 2026-01-28 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/001-license-verification/spec.md`

## Summary

Implement a hardware-based license verification system that validates encrypted license key files at application startup. Hardware identifiers (MAC addresses, motherboard serial) are hashed into a deviceId using SHA-256, then encrypted with AES-256 along with expiry timestamp. HMAC-SHA256 provides tamper detection. Display a configurable blocking error dialog on validation failure. Support enabling/disabling license checking via configuration for development builds. Three utilities: HardwareInfoExtractor (customer-facing), LicenseGenerator (admin-only), and LicenseRenewer (admin-only).

## Technical Context

**Language/Version**: Java 17 (existing project standard)  
**Primary Dependencies**: JavaFX 17.0.15 (UI), javax.crypto (AES-256 + HMAC), Jackson 2.15.3 (JSON), oshi-core (hardware detection)  
**Storage**: JSON files (license key file, configuration file)  
**Testing**: JUnit 5, Mockito (for hardware mocking), JaCoCo for coverage ≥80%  
**Target Platform**: Windows Desktop (primary), cross-platform JavaFX support  
**Project Type**: Single JavaFX desktop application  
**Performance Goals**: License validation completes within 2 seconds  
**Constraints**: No external network dependencies, offline operation, validation must complete before main window appears  
**Scale/Scope**: Single feature module, 6-8 new Java classes, 1 FXML dialog, 3 external utility tools

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. User Experience First | Error dialog provides clear message? Non-blocking for valid licenses? Logo and message configurable? | ✅ Pass |
| II. Modular Design | License validator is standalone module? No coupling to MainApp beyond startup hook? | ✅ Pass |
| III. Configuration-Driven | Error message, logo, enable/disable stored in JSON? | ✅ Pass |
| IV. Quality Testing | Hardware extractor mockable? Validation logic unit tested? Edge cases (missing file, expired, hardware mismatch) covered? | ✅ Pass |
| V. Documentation | Feature docs in `/docs/features/feature-license-verification.md`? Deployment guide for key generation? | ✅ Pass |
| VI. Reusable Components & Open Source | Use javax.crypto (built-in), oshi-core (proven hardware detection library), Jackson (existing)? | ✅ Pass |
| VII. Security & Data Handling | Hardware IDs not logged? HMAC secret embedded securely? License file validated before parsing? | ✅ Pass |

**Status Legend**: ⬜ Not Checked | ✅ Pass | ❌ Fail (requires justification in Complexity Tracking)

**Notes**: All gates pass. Design follows modular approach with standalone validator, configuration-driven UI, and secure handling of hardware identifiers. No justification needed.

## Project Structure

### Documentation (this feature)

```text
specs/001-license-verification/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0: hardware detection, AES-256 + HMAC, three-utility architecture
├── data-model.md        # Phase 1: LicenseData, ValidationResult, LicenseConfig models
├── quickstart.md        # Phase 1: Developer guide for testing with/without license
├── contracts/           # Phase 1: LicenseValidator interface, HardwareIdentifier interface
│   ├── LicenseValidator.md
│   └── HardwareIdentifier.md
└── tasks.md             # Phase 2: NOT created by /speckit-plan
```

### Source Code (repository root)

```text
src/main/java/com/example/smarttemplatefiller/
├── license/                           # NEW: License verification module
│   ├── LicenseValidator.java          # Main orchestrator: validate at startup
│   ├── HardwareIdentifier.java        # Extract MAC + motherboard serial (OSHI)
│   ├── LicenseFileReader.java         # Read and parse encrypted license JSON
│   ├── EncryptionValidator.java       # AES-256 decryption + HMAC-SHA256 verification
│   ├── LicenseConfig.java             # POJO: error message, logo path, enabled flag
│   └── LicenseErrorDialog.java        # JavaFX dialog with logo and configurable message
│
├── MainApp.java                       # MODIFIED: Add license check before main window
└── (existing files unchanged)

src/main/resources/
├── fxml/
│   └── license_error_dialog.fxml      # NEW: Error dialog layout
├── license_config.json                # NEW: Default configuration (error message, logo, enabled)
└── (existing files unchanged)

src/test/java/com/example/smarttemplatefiller/
└── license/                           # NEW: Unit tests for license module
    ├── LicenseValidatorTest.java      # Test validation logic (mocked hardware)
    ├── HardwareIdentifierTest.java    # Test hardware extraction (mocked OSHI)
    ├── EncryptionValidatorTest.java   # Test AES decryption + HMAC validation
    └── LicenseFileReaderTest.java     # Test JSON parsing edge cases

tools/
├── HardwareInfoExtractor/             # NEW: Customer-facing utility (NO secret key)
│   ├── build.gradle
│   ├── src/main/java/
│   │   └── HardwareExtractor.java     # Extract MAC + serial, display to user
│   └── README.md                      # Usage instructions for customers
│
├── LicenseGenerator/                  # NEW: Admin-only utility (HAS secret key)
│   ├── build.gradle
│   ├── src/main/java/
│   │   ├── LicenseGeneratorCLI.java   # CLI: input hardware → output encrypted license.json
│   │   ├── EncryptionUtils.java       # AES-256 + HMAC encryption logic
│   │   └── SecretKeyHolder.java       # Embedded 256-bit secret key
│   └── README.md                      # Admin deployment instructions
│
└── LicenseRenewer/                    # NEW: Admin renewal utility (optional)
    ├── build.gradle
    ├── src/main/java/
    │   └── LicenseRenewer.java         # Decrypt, update expiry, re-encrypt
    └── README.md                       # Renewal workflow instructions
```

**Structure Decision**: Single project with new `license/` package for modular separation. License validation is a self-contained module that MainApp calls before showing UI. Three separate utility tools under `tools/`: HardwareInfoExtractor (for customers, safe to distribute), LicenseGenerator and LicenseRenewer (admin-only, contain secret key for AES/HMAC operations).

**Security Architecture**:
- **Encryption**: AES-256 in CBC mode with random IV per license
- **Integrity**: HMAC-SHA256 signature over version + encrypted data
- **Obfuscation**: Hardware identifiers hashed into deviceId (SHA-256)
- **License Format**: `{"v": "1.0", "d": "<encrypted>", "s": "<signature>"}`
- **Secret Key**: Single 256-bit key embedded in application (obfuscated via ProGuard)
- **Workflow**: Customer extracts hardware → Admin generates license → Customer receives encrypted file

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

N/A - All gates pass, no violations.
