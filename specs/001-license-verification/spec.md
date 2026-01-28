# Feature Specification: Hardware-Based License Verification

**Feature Branch**: `001-license-verification`  
**Created**: 2026-01-28  
**Status**: Draft  
**Input**: User description: "License mechanism for hardware-based authorization with encrypted key file validation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Successful Application Launch with Valid License (Priority: P1)

An authorized user launches the SmartTemplateFiller application with a valid license key file present in the application directory. The application validates the license against the hardware identifiers and expiry date, then proceeds to normal operation without interruption.

**Why this priority**: This is the core happy path that enables legitimate users to use the application. Without this working, no user can operate the software.

**Independent Test**: Can be fully tested by placing a valid license key file in the application directory, launching the application, and verifying it starts normally without blocking dialogs.

**Acceptance Scenarios**:

1. **Given** a valid license key file exists in the application directory, **When** the user launches the application, **Then** the application validates the license silently and proceeds to the main window
2. **Given** the license key file contains matching MAC address and motherboard serial number, **When** license validation occurs, **Then** hardware verification succeeds
3. **Given** the license key file has an expiry date in the future, **When** license validation occurs, **Then** expiry date validation succeeds
4. **Given** all license validations pass, **When** the main window loads, **Then** no blocking dialogs are displayed

---

### User Story 2 - Blocked Launch Due to Invalid/Missing License (Priority: P1)

A user without a valid license (missing file, invalid key, expired license, or hardware mismatch) attempts to launch the application. The system blocks access, displays a configurable error dialog with company logo and contact message, and exits the application after the user closes the dialog.

**Why this priority**: This is the enforcement mechanism that protects the software from unauthorized use. Equal priority to P1 as it's the other half of the core license system.

**Independent Test**: Can be fully tested by removing the license file (or using an invalid one), launching the application, and verifying that a blocking dialog appears with the configured message and logo, then closes the application.

**Acceptance Scenarios**:

1. **Given** no license key file exists, **When** the user launches the application, **Then** a blocking dialog appears with the configured error message
2. **Given** the license verification fails, **When** the error dialog is displayed, **Then** the configured company logo appears in the upper left corner of the dialog
3. **Given** the license verification fails, **When** the error dialog is displayed, **Then** the configured contact message (default: "Kindly contact Mitutoyo for more details") is shown
4. **Given** the error dialog is displayed, **When** the user attempts to interact with the main application, **Then** the main application remains blocked and unresponsive
5. **Given** the error dialog is displayed, **When** the user closes the dialog, **Then** the application terminates completely

---

### User Story 3 - Configurable Error Message and Branding (Priority: P2)

An administrator or deployment team needs to customize the license error dialog to match company branding and contact information. They can configure the error message text and company logo through an external configuration file without rebuilding the application.

**Why this priority**: Enables customization for different deployment scenarios and company branding requirements, but the feature works with defaults if not configured.

**Independent Test**: Can be fully tested by modifying the configuration file to change the error message and logo, then triggering a license failure and verifying the custom message and logo appear.

**Acceptance Scenarios**:

1. **Given** a license configuration file exists, **When** the file specifies a custom error message, **Then** that message is displayed instead of the default
2. **Given** a license configuration file exists, **When** the file specifies a custom logo path, **Then** that logo image is displayed in the error dialog
3. **Given** the configuration file does not exist or is invalid, **When** a license error occurs, **Then** the system falls back to default message and logo
4. **Given** a custom logo file path is specified, **When** the logo file does not exist, **Then** the system displays the dialog without a logo rather than crashing

---

### User Story 4 - Optional License Checking for Development Builds (Priority: P3)

A developer or build engineer packages the application for different environments (development, testing, production). They can enable or disable license checking through a build-time or configuration option, allowing development and testing without license restrictions.

**Why this priority**: Improves developer experience and testing workflows, but not critical to the core licensing functionality.

**Independent Test**: Can be fully tested by packaging the application with license checking disabled, launching it without a license file, and verifying it runs normally without validation.

**Acceptance Scenarios**:

1. **Given** license checking is disabled via configuration, **When** the application launches, **Then** no license validation occurs
2. **Given** license checking is disabled, **When** no license file is present, **Then** the application proceeds to normal operation
3. **Given** a packaged executable is created with license checking disabled, **When** distributed to testers, **Then** they can run the application without obtaining license files
4. **Given** license checking is enabled (default for production), **When** the application launches, **Then** license validation occurs as specified in P1 and P2 scenarios

---

### Edge Cases

- What happens when the license file is corrupted or contains invalid data? → Display the standard error dialog and block access
- What happens when the MAC address changes (e.g., network adapter replacement)? → License validation fails and error dialog appears
- What happens when the motherboard is replaced? → License validation fails and error dialog appears
- What happens when multiple network adapters exist on the system? → License file can contain multiple MAC addresses; validation succeeds if ANY MAC address matches current hardware
- What happens when the system clock is set to a date before the license expiry? → System trusts the system clock; no tampering detection (per MVP scope)
- What happens when the license file is in an unexpected format or uses an unsupported encryption method? → Treat as invalid license and display error dialog
- What happens during the brief validation period at startup? → Main window should not appear until validation completes (either success or failure)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST validate a license key file at application startup before displaying the main window
- **FR-002**: System MUST extract the current machine's MAC address(es) and validate that at least one matches a MAC address in the license key file (supports multiple network adapters)
- **FR-003**: System MUST extract and compare the current machine's motherboard serial number against the value embedded in the license key file  
- **FR-004**: System MUST extract and validate the expiry date from the license key file against the current system date
- **FR-005**: System MUST display a blocking modal dialog when license validation fails for any reason (missing file, hardware mismatch, expired, corrupted)
- **FR-006**: System MUST load error message text from an external configuration file, with a fallback default message
- **FR-007**: System MUST load company logo image from a path specified in the configuration file
- **FR-008**: System MUST position the company logo in the upper left corner of the error dialog
- **FR-009**: System MUST terminate the application process when the user closes the license error dialog
- **FR-010**: System MUST block all user interactions with the main application while the license error dialog is displayed
- **FR-011**: System MUST support enabling or disabling license validation through external configuration
- **FR-012**: License key files MUST be stored in an encrypted format to prevent tampering
- **FR-013**: System MUST use cryptographic validation (HMAC or encryption) to verify license file integrity and authenticity

### Key Entities *(include if feature involves data)*

- **License Key File**: External file containing encrypted/signed data with MAC address, motherboard serial number, and expiry date. File format and location to be determined during planning.
- **License Configuration**: External configuration file (JSON recommended per project constitution) containing error message text, logo path, and enable/disable flag.
- **Hardware Identifiers**: MAC address of primary network adapter and motherboard serial number extracted from the host system.
- **Validation Result**: Boolean outcome of license verification process, determining whether to proceed or block.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Authorized users with valid licenses can launch and use the application without any blocking dialogs or delays exceeding 2 seconds for validation
- **SC-002**: Unauthorized users (missing/invalid/expired licenses) are blocked from accessing the application 100% of the time
- **SC-003**: License validation completes within 2 seconds on standard hardware
- **SC-004**: Configuration changes (error message, logo) take effect on next application launch without requiring application rebuild
- **SC-005**: Development builds with license checking disabled can be packaged and distributed to internal teams

## Technical Guidance for Planning *(informational only)*

> [!NOTE]
> This section provides technical context to inform planning decisions. These are recommendations, not requirements.

### Cryptographic Approach Recommendation

**Hash-based (HMAC) vs Encryption:**

For this use case, **HMAC (Hash-based Message Authentication Code) with SHA-256** is recommended over encryption:

**Advantages of HMAC:**
- Simpler implementation and key management
- Verification doesn't require private key on client (more secure)
- Tamper detection is the primary goal, not confidentiality
- Faster validation performance
- Smaller key file size

**Suggested Workflow:**
1. **Key Generation Tool** (run by administrator):
   - Collect MAC address + Motherboard serial + Expiry date
   - Concatenate and compute HMAC-SHA256 with secret key
   - Store as JSON: `{ "mac": "...", "serial": "...", "expiry": "...", "signature": "..." }`
   
2. **Application Validation**:
   - Read license file
   - Extract hardware identifiers from system
   - Recompute HMAC with embedded secret
   - Compare computed signature with stored signature
   - Validate expiry date

**Alternative (If confidentiality needed):** Use asymmetric encryption (RSA or EdDSA), where the license generator signs with private key and application verifies with public key.

### Key File Preparation Process

Administrator/deployment team will need:
1. **Key Generation Utility**: Command-line or GUI tool to create license files
2. **Inputs**: Target machine's MAC address, motherboard serial, desired expiry date
3. **Output**: License key file (e.g., `license.key` or `license.json`)
4. **Distribution**: Securely deliver key file to customer alongside application

## Constitution Alignment *(mandatory)*

| Principle | Alignment Check | Notes |
|-----------|-----------------|-------|
| I. User Experience First | Clear error messages; blocking dialog prevents confusion; configurable messaging | Error dialog provides immediate feedback with clear next steps |
| II. Modular Design | License verification module can be independent, testable component | Can be implemented as separate `LicenseValidator` class |
| III. Configuration-Driven | Error message, logo, enable/disable flag all externally configured via JSON | Follows established pattern of JSON configuration files |
| IV. Quality Testing | Edge cases identified; validation logic is highly testable | Hardware mocking enables comprehensive unit testing |
| V. Documentation | Feature requires user-facing documentation for license file deployment | Will document in `/docs/features/feature-license-verification.md` |
| VI. Reusable Components & Open Source | Can leverage standard cryptographic libraries (javax.crypto, BouncyCastle) | No need to implement custom crypto |
| VII. Security & Data Handling | Hardware identifiers are hashed/encrypted; no sensitive data logged | License validation errors should not log hardware details |
