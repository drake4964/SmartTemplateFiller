# Tasks: Hardware-Based License Verification

**Input**: Design documents from `/specs/001-license-verification/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Unit tests are required per project constitution (≥80% coverage). Integration tests are included for user story validation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Phase Structure**: 13 phases total (109 tasks)
- Phase 1: Setup (8 tasks)
- **Phase 2A**: Core Data Models (5 tasks) - Simple POJOs
- **Phase 2B**: Core Services & Cryptography (6 tasks) - Complex AES-256 + HMAC
- Phase 3: US4 - Dev Mode Bypass (11 tasks) 🚀 Implement FIRST
- Phase 4: US1 - Valid License (7 tasks)
- Phase 5: US2 - Error Dialog (12 tasks)
- Phase 6: US3 - Configuration (11 tasks)
- **Phase 7A**: HardwareInfoExtractor Utility (6 tasks)
- **Phase 7B**: LicenseGenerator Utility (13 tasks) - Most complex
- **Phase 7C**: LicenseRenewer Utility (6 tasks)
- Phase 8: Integration Testing (8 tasks)
- Phase 9: Polish & Constitution (16 tasks)

**Note**: Phase 2 split into 2A+2B to separate POJOs from crypto. Phase 7 split into 7A+7B+7C to avoid AI confusion between three different tools. User Story 4 (dev mode bypass) is implemented FIRST (Phase 3) to enable development without blocking on license features.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Single JavaFX project**: `src/main/java/`, `src/test/java/`, `tools/` at repository root
- Paths use project structure from plan.md

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure for license verification module

- [x] T001 Create `src/main/java/com/example/smarttemplatefiller/license/` package directory
- [x] T002 Create `src/test/java/com/example/smarttemplatefiller/license/` test package directory
- [x] T003 Create `tools/HardwareInfoExtractor/` directory for customer utility
- [x] T004 Create `tools/LicenseGenerator/` directory for admin utility
- [x] T005 Create `tools/LicenseRenewer/` directory for renewal utility
- [x] T006 [P] Add OSHI dependency (`com.github.oshi:oshi-core:6.4.+`) to `build.gradle`
- [x] T007 [P] Create `src/main/resources/license_config.json` with default configuration (enabled=false for development)
- [x] T008 [P] Create `src/main/resources/fxml/license_error_dialog.fxml` UI layout file

---

## Phase 2A: Core Data Models

**Purpose**: Create foundational data structures (POJOs) for license verification

**⚠️ CRITICAL**: These data models are required before implementing services

- [x] T009 Create `LicenseData.java` data model in `src/main/java/.../license/` with fields: version, encryptedData, signature
- [x] T010 Create `ValidationResult.java` with ErrorCode enum in `src/main/java/.../license/`
- [x] T011 Create `LicenseConfig.java` POJO in `src/main/java/.../license/` (enabled, errorMessage, logoPath, licenseFilePath)
- [x] T012 Create `CurrentHardwareInfo.java` in `src/main/java/.../license/` (macAddresses, motherboardSerial)
- [x] T017 [P] Create unit test `LicenseDataTest.java` in `src/test/java/.../license/` to test JSON deserialization

**Checkpoint**: Basic data structures ready - can now implement core services

---

## Phase 2B: Core Services & Cryptography

**Purpose**: Implement complex cryptographic validation and hardware detection services

**⚠️ CRITICAL**: This phase contains the MOST COMPLEX cryptographic logic - focus required

**Dependencies**: Requires Phase 2A completion (data models must exist)

- [x] T013 Implement `HardwareIdentifier.java` interface in `src/main/java/.../license/` (getMacAddresses, getMotherboardSerial, getCurrentHardware)
- [x] T014 Implement `OshiHardwareIdentifier.java` in `src/main/java/.../license/` using OSHI library to extract MAC addresses and motherboard serial
- [x] T015 Implement `EncryptionValidator.java` in `src/main/java/.../license/` with AES-256 decryption + HMAC-SHA256 verification methods
- [x] T016 Implement `LicenseFileReader.java` in `src/main/java/.../license/` to parse JSON license files using Jackson
- [x] T018 [P] Create unit test `EncryptionValidatorTest.java` in `src/test/java/.../license/` with test vectors for AES + HMAC
- [x] T019 [P] Create `MockHardwareIdentifier.java` test utility in `src/test/java/.../license/` for controlled hardware simulation

**🛑 CRITICAL CHECKPOINT - DO NOT PROCEED WITHOUT VALIDATION**:

**Required Validations Before Proceeding to User Stories**:
1. ✅ Run `gradle test` - all Phase 2B tests must pass
2. ✅ Run JaCoCo coverage report: `gradle jacocoTestReport`
   - Verify ≥80% code coverage for `EncryptionValidator.java`
   - Verify ≥80% code coverage for `OshiHardwareIdentifier.java`
   - Verify ≥80% code coverage for `LicenseFileReader.java`
3. ✅ Manually verify AES-256 encryption/decryption test vectors pass
4. ✅ Manually verify HMAC-SHA256 signature validation test vectors pass
5. ✅ Verify OSHI can successfully extract MAC addresses on test machine
6. ✅ Verify OSHI can successfully extract motherboard serial on test machine

**⚠️ WARNING**: If any crypto tests fail or coverage is below 80%, DO NOT proceed to Phase 3. Fix issues first.

**Checkpoint**: Foundation complete and VALIDATED - user story implementation can now begin in parallel

---

## Phase 3: User Story 4 - Optional License Checking for Development Builds (Priority: P3) 🚀 DEV ENABLER

**Goal**: Developers can disable license checking for development/testing builds via configuration

**Why First**: Implementing this FIRST allows you to continue developing other features without being blocked by incomplete license validation. Set `enabled=false` in config and the app launches normally during development.

**Independent Test**: Set `license_config.json` enabled=false, launch app without license file, verify app runs normally

### Tests for User Story 4

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T020 [P] [US4] Create unit test `LicenseValidatorTest.java` in `src/test/java/.../license/` (basic structure for all license tests)
- [x] T021 [P] [US4] Add test case: LicenseConfig.enabled=false → validation bypassed, returns ValidationResult.success()
- [x] T022 [P] [US4] Add test case: enabled=false with missing license file → no error, app launches

### Implementation for User Story 4

- [x] T023 [US4] Implement `LicenseValidator.java` interface in `src/main/java/.../license/`
- [x] T024 [US4] Implement `DefaultLicenseValidator.java` in `src/main/java/.../license/` with config-check-first logic
- [x] T025 [US4] In `DefaultLicenseValidator.validate()`: check LicenseConfig.enabled first
- [x] T026 [US4] If enabled=false, skip all validation logic and return ValidationResult.success()
- [x] T027 [US4] Add logging: "License checking disabled via configuration" (INFO level)
- [x] T028 [US4] Integrate license validation into `MainApp.java` start() method before showing primary stage
- [x] T029 [US4] Test with `license_config.json` enabled=false: verify app launches without license file
- [x] T030 [US4] Run unit tests to verify bypass works correctly

**Checkpoint**: ✅ Development unblocked! App now launches with `enabled=false`. You can continue developing while building license features.

---

## Phase 4: User Story 1 - Successful Application Launch with Valid License (Priority: P1) 🎯 MVP

**Goal**: Authorized users with valid license files can launch the application without blocking dialogs

**Independent Test**: Place a valid `license.json` in application directory, launch app with `enabled=true`, verify main window appears without error dialogs

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T031 [P] [US1] Add test case to `LicenseValidatorTest.java`: valid license with matching deviceId and future expiry → ValidationResult.success()
- [ ] T032 [P] [US1] Add test case: valid license with multiple MAC addresses, one matching → ValidationResult.success()

### Implementation for User Story 1

- [ ] T033 [US1] Update `DefaultLicenseValidator.validate()` to implement full validation logic (after enabled check)
- [ ] T034 [US1] Implement: load config → load license file → verify HMAC → decrypt AES → compute deviceId → compare → check expiry
- [ ] T035 [US1] Add logging for validation steps (DEBUG level, no hardware IDs logged per security principle)
- [ ] T036 [US1] Run unit tests to verify validation succeeds with valid license
- [ ] T037 [US1] Test with real license file: set enabled=true, place valid license, verify app launches

**Checkpoint**: At this point, User Story 1 should be fully functional - valid licenses pass validation and app launches normally

---

## Phase 5: User Story 2 - Blocked Launch Due to Invalid/Missing License (Priority: P1)

**Goal**: Users without valid licenses are blocked from accessing the application with clear error dialog

**Independent Test**: Remove `license.json`, set enabled=true, launch app, verify blocking error dialog appears with message and logo, app exits on close

### Tests for User Story 2

- [ ] T038 [P] [US2] Add test case to `LicenseValidatorTest.java`: missing license file → ValidationResult.failure(FILE_NOT_FOUND)
- [ ] T039 [P] [US2] Add test case: invalid JSON format → ValidationResult.failure(INVALID_FORMAT)  
- [ ] T040 [P] [US2] Add test case: HMAC signature mismatch → ValidationResult.failure(SIGNATURE_MISMATCH)
- [ ] T041 [P] [US2] Add test case: incorrect deviceId → ValidationResult.failure(HARDWARE_MISMATCH)
- [ ] T042 [P] [US2] Add test case: expired license (past timestamp) → ValidationResult.failure(LICENSE_EXPIRED)

### Implementation for User Story 2

- [ ] T043 [US2] Create `LicenseErrorDialog.java` JavaFX controller in `src/main/java/.../license/`
- [ ] T044 [US2] Implement FXML layout in `src/main/resources/fxml/license_error_dialog.fxml` with ImageView (logo), Label (message), OK button
- [ ] T045 [US2] Implement dialog display logic: load config → show modal dialog → block main window → Platform.exit() on close
- [ ] T046 [US2] Handle missing logo file gracefully (hide ImageView if logo file doesn't exist)
- [ ] T047 [US2] Integrate error dialog into `MainApp.java`: if validation fails, show dialog and exit
- [ ] T048 [US2] Add error handling for all ValidationResult.ErrorCode cases
- [ ] T049 [US2] Run unit tests to verify all error cases trigger failure results

**Checkpoint**: At this point, User Stories 1 AND 2 should both work - valid licenses pass, invalid licenses block with error dialog

---

## Phase 6: User Story 3 - Configurable Error Message and Branding (Priority: P2)

**Goal**: Administrators can customize error message and company logo without rebuilding application

**Independent Test**: Edit `license_config.json` to change errorMessage and logoPath, trigger license failure, verify custom values appear in dialog

### Tests for User Story 3

- [ ] T050 [P] [US3] Create unit test `LicenseConfigTest.java` in `src/test/java/.../license/` to test JSON deserialization
- [ ] T051 [P] [US3] Add test case: custom errorMessage → dialog displays custom message
- [ ] T052 [P] [US3] Add test case: custom logoPath → ImageView loads custom logo
- [ ] T053 [P] [US3] Add test case: missing config file → fallback to default message
- [ ] T054 [P] [US3] Add test case: invalid logoPath → dialog displays without logo (no crash)

### Implementation for User Story 3

- [ ] T055 [US3] Implement `LicenseConfig.load(String filePath)` static method in `LicenseConfig.java` using Jackson
- [ ] T056 [US3] Add fallback to default values if config file missing or invalid
- [ ] T057 [US3] Update `LicenseErrorDialog.java` to load message from LicenseConfig
- [ ] T058 [US3] Update `LicenseErrorDialog.java` to load logo image from LicenseConfig.logoPath
- [ ] T059 [US3] Handle FileNotFoundException for logo file (hide ImageView, log warning)
- [ ] T060 [US3] Test with custom `license_config.json`: verify message and logo change without code rebuild

**Checkpoint**: All core user stories (US1, US2, US3, US4) should now be independently functional with customizable branding

---

## Phase 7A: HardwareInfoExtractor Utility

**Purpose**: Build customer-facing utility for extracting hardware information (NO secret key)

**Tool Context**: This is a SIMPLE utility that customers run to get their hardware info. It does NOT contain the secret key and cannot generate licenses.

- [ ] T061 [P] Create `tools/HardwareInfoExtractor/build.gradle` with OSHI dependency
- [ ] T062 [P] Create `HardwareExtractor.java` in `tools/HardwareInfoExtractor/src/main/java/`
- [ ] T063 Use OSHI to extract MAC addresses and motherboard serial number
- [ ] T064 Display hardware info in formatted console output for customer to send to admin
- [ ] T065 [P] Create `tools/HardwareInfoExtractor/README.md` with usage instructions for customers
- [ ] T066 Build `HardwareInfoExtractor.jar` and test extraction on real hardware

**Checkpoint**: Customer utility complete - customers can now extract their hardware info

---

## Phase 7B: LicenseGenerator Utility (Admin-Only)

**Purpose**: Build admin-only license generation tool with embedded secret key and full cryptography

**Tool Context**: This is the MOST CRITICAL utility. It contains the secret key and implements the SAME AES-256 + HMAC logic as the main application. Admin-only, never distribute to customers.

**⚠️ CRITICAL**: This phase contains complex cryptography matching Phase 2B - focus required

- [ ] T067 [P] Create `tools/LicenseGenerator/build.gradle` with Jackson and javax.crypto dependencies
- [ ] T068 [P] Create `SecretKeyHolder.java` in `tools/LicenseGenerator/src/main/java/` with embedded 256-bit secret key
- [ ] T069 [P] Create `EncryptionUtils.java` in `tools/LicenseGenerator/src/main/java/` with AES-256 + HMAC methods
- [ ] T070 Create `LicenseGeneratorCLI.java` in `tools/LicenseGenerator/src/main/java/` with CLI argument parsing
- [ ] T071 Implement deviceId hash computation: SHA-256(macAddresses.sorted().join("|") + motherboardSerial)
- [ ] T072 Implement payload generation: deviceId + "|" + expiryTimestamp
- [ ] T073 Implement AES-256 encryption with random IV
- [ ] T074 Implement HMAC-SHA256 signature over version + encryptedData
- [ ] T075 Output license file in JSON format: {"v": "1.0", "d": "<encrypted>", "s": "<signature>"}
- [ ] T076 Add validation: MAC format, non-empty serial, future expiry date
- [ ] T077 Support interactive mode (prompts for inputs) and command-line mode (--mac, --serial, --expiry, --output flags)
- [ ] T078 [P] Create `tools/LicenseGenerator/README.md` with usage instructions for administrators
- [ ] T079 Build `LicenseGenerator.jar` and test generating license for known hardware

**Checkpoint**: License generation tool complete - admins can now create encrypted licenses

---

## Phase 7C: LicenseRenewer Utility (Admin Renewal)

**Purpose**: Build admin-only license renewal tool for extending expiry dates

**Tool Context**: This utility decrypts existing licenses, updates the expiry date, then re-encrypts. Requires the same secret key as LicenseGenerator.

- [ ] T080 [P] Create `tools/LicenseRenewer/build.gradle` with Jackson and javax.crypto dependencies
- [ ] T081 [P] Create `LicenseRenewer.java` in `tools/LicenseRenewer/src/main/java/`
- [ ] T082 Implement decrypt existing license → extract deviceId → create new payload with same deviceId but new expiry → re-encrypt → re-sign
- [ ] T083 Support command-line mode: --license <existing> --new-expiry <date> --output <file>
- [ ] T084 [P] Create `tools/LicenseRenewer/README.md` with renewal workflow instructions
- [ ] T085 Build `LicenseRenewer.jar` and test renewing an existing license

**Checkpoint**: All three utility tools complete - full license management workflow ready

---

## Phase 8: Integration Testing & Validation

**Purpose**: End-to-end validation of all user stories working together

- [ ] T086 Create integration test: valid license → app launches → main window visible
- [ ] T087 Create integration test: missing license → error dialog → app exits
- [ ] T088 Create integration test: expired license → error dialog with default message
- [ ] T089 Create integration test: custom config (message + logo) → error dialog shows custom values
- [ ] T090 Create integration test: disabled license checking (US4) → app launches without license file
- [ ] T091 Generate license using LicenseGenerator → place in app directory → verify app launches
- [ ] T092 Extract hardware using HardwareInfoExtractor → generate matching license → verify validation succeeds
- [ ] T093 Renew license using LicenseRenewer → verify renewed license validates correctly

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Constitution compliance, documentation, and production readiness

### Constitution Compliance Tasks

- [ ] T094 [P] **[Principle I - UX]** Verify license validation completes within 2 seconds (SC-003)
- [ ] T095 [P] **[Principle I - UX]** Verify error dialog is modal and blocks main application completely
- [ ] T096 [P] **[Principle II - Modular]** Verify `license/` package has no circular dependencies with existing code
- [ ] T097 [P] **[Principle III - Config]** Verify `license_config.json` schema is documented and versioned
- [ ] T098 **[Principle IV - Testing]** Run final JaCoCo test coverage report - verify ≥80% for entire license module (should already be validated in Phase 2B checkpoint)
- [ ] T099 [P] **[Principle V - Docs]** Create `/docs/features/feature-license-verification.md` with feature overview and deployment guide
- [ ] T100 [P] **[Principle V - Docs]** Update `AI_CONTEXT.md` with license verification module information
- [ ] T101 [P] **[Principle VI - Open Source]** Verify using OSHI (open source), Jackson (existing), javax.crypto (built-in)
- [ ] T102 [P] **[Principle VII - Security]** Verify hardware IDs are NEVER logged (search codebase for MAC/serial in log statements)
- [ ] T103 [P] **[Principle VII - Security]** Verify secret key is obfuscated (recommend ProGuard/R8 for production builds)

### General Polish

- [ ] T104 [P] Code cleanup: remove debug logging, add Javadoc comments to public APIs
- [ ] T105 [P] Update quickstart.md with actual testing results and screenshots
- [ ] T106 [P] Create deployment guide in `/docs/deployment/license-generation-workflow.md`
- [ ] T107 Validate all quickstart.md test scenarios work as documented
- [ ] T108 [P] Package all three utility JARs for distribution to administrators and customers
- [ ] T109 Create demo video or screenshots showing valid license flow and error dialog flow

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational - Data Models (Phase 2A)**: Depends on Setup completion
- **Foundational - Services (Phase 2B)**: Depends on Phase 2A completion - BLOCKS all user stories
- **User Story 4 - Dev Mode (Phase 3)**: Depends on Phase 2B - IMPLEMENT FIRST to unblock development 🚀
- **User Stories 1, 2, 3 (Phases 4-6)**: All depend on Phase 2B (Foundational) completion
  - Can proceed in parallel (if staffed) OR sequentially after Phase 3 (US4)
  - Phase 3 (US4) allows you to test these features independently with `enabled=false`
- **License Utilities (Phases 7A-7C)**: Can proceed in parallel OR after Phase 2B (independent of user stories)
  - Phase 7A (HardwareInfoExtractor): Independent, can start anytime after Phase 2B
  - Phase 7B (LicenseGenerator): Independent, can start anytime after Phase 2B
  - Phase 7C (LicenseRenewer): Independent, can start anytime after Phase 2B
  - All three utilities can be built in parallel by different developers
- **Integration Testing (Phase 8)**: Depends on Phases 3-6 completion + Phases 7A-7C completion
- **Polish (Phase 9)**: Depends on all desired user stories + utilities being complete

### User Story Dependencies

- **User Story 4 (P3) - FIRST**: Can start after Foundational (Phase 2) - Enables development workflow
- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories  
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Extends US2 error dialog functionality

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models/data structures before services
- Services before UI/integration
- Core implementation before integration with MainApp
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, User Story 4 enables development
- After US4, all other user stories (US1, US2, US3) can proceed in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- All three utility tools can be built in parallel
- Polish/documentation tasks can run in parallel

---

## Parallel Example: User Story 4 (Dev Enabler)

```bash
# Launch all tests for User Story 4 together:
Task T020: "Create unit test LicenseValidatorTest.java"
Task T021: "Add test case: enabled=false → bypass"
Task T022: "Add test case: enabled=false + missing file → success"

# After tests fail, implement in sequence:
Task T023: "Implement LicenseValidator interface"
Task T024: "Implement DefaultLicenseValidator with config-check-first"
Task T025: "Check LicenseConfig.enabled first"
Task T026: "Return success() if disabled"
```

---

## Parallel Example: License Utilities (Phases 7A-7C)

```bash
# All three utilities can be built in parallel by different developers:
Developer A: Phase 7A - Tasks T061-T066 (HardwareInfoExtractor)
Developer B: Phase 7B - Tasks T067-T079 (LicenseGenerator - most complex)
Developer C: Phase 7C - Tasks T080-T085 (LicenseRenewer)
```

---

## Implementation Strategy

### Recommended: Dev-First Approach (US4 → US1 → US2)

1. Complete Phase 1: Setup (with `enabled=false` default config)
2. Complete Phase 2A: Core Data Models (POJOs)
3. Complete Phase 2B: Core Services & Cryptography (CRITICAL - blocks all stories)
4. **Complete Phase 3: User Story 4** (dev mode bypass) 🚀
   - **Result**: App launches with `enabled=false` - development unblocked!
5. Continue developing in parallel:
   - Work on Phase 4: User Story 1 (valid license)
   - Work on Phase 5: User Story 2 (error dialog)
   - Test with `enabled=false` throughout development
6. **STOP and VALIDATE**: Set `enabled=true`, test with real licenses
7. Build utilities (Phases 7A, 7B, 7C) to create test licenses
8. Deploy/demo if ready

### MVP for Production (US4 + US1 + US2)

1. Complete Setup + Foundational (Phases 1, 2A, 2B)
2. Implement US4 (dev bypass)
3. Implement US1 (valid license flow)
4. Implement US2 (error dialog flow)
5. Build LicenseGenerator utility
6. **MVP COMPLETE** 🎯 - Full license enforcement working, dev mode available

### Incremental Delivery

1. Complete Setup + Foundational (Phases 1, 2A, 2B) → Foundation ready
2. Add US4 (Phase 3) → **Development unblocked** (can work with `enabled=false`)
3. Add US1 + US2 (Phases 4-5) → Test independently (valid + invalid flows) → **MVP COMPLETE** 🎯
4. Add US3 (Phase 6) → Test customization → Deploy/Demo
5. Build utilities (Phases 7A, 7B, 7C) → Enable customer deployments
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup (Phase 1) together
2. Team completes Foundational (Phases 2A, then 2B) together
3. Developer A: Phase 3 - User Story 4 (dev bypass) - PRIORITY
4. Once Phase 3 (US4) is done, development unblocked:
   - Developer A continues: Phase 4 - User Story 1 (valid license)
   - Developer B: Phase 5 - User Story 2 (error dialog)
   - Developer C: Phase 6 - User Story 3 (configuration)
5. In parallel with user stories:
   - Developer D: Phase 7A - HardwareInfoExtractor utility
   - Developer E: Phase 7B - LicenseGenerator utility
   - Developer F: Phase 7C - LicenseRenewer utility
5. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- **US4 FIRST**: Set `enabled=false` in default config to unblock development immediately
- Secret key MUST be identical in application and LicenseGenerator (critical for signature validation)
- Hardware extraction must use OSHI consistently across app and HardwareInfoExtractor
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
