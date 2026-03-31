---
name: project-context
description: >
  Provides deep architectural context for the SmartTemplateFiller project —
  a JavaFX 17 desktop application that parses CMM/measurement TXT/ASC files
  and exports them to Excel via configurable JSON mappings. Covers tech stack,
  module layout, coding conventions, thread-safety rules, license-verification
  internals, and the two operating modes (Manual Export and Running/Folder-Watch).
  Read this before writing any new code or modifying existing classes.
---

# SmartTemplateFiller — Project Context

## 1. Core Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 (LTS) |
| UI framework | JavaFX (FXML + Controllers) | 17.0.15 |
| Build system | Gradle (wrapper included) | 8.5+ |
| Excel I/O | Apache POI (poi + poi-ooxml) | 5.2.3 |
| JSON serialization | Jackson Databind | 2.15.3 |
| Hardware detection | OSHI (oshi-core) | 6.4.13 |
| Crypto | `javax.crypto` (AES-CBC + HMAC-SHA256) | JDK built-in |
| Logging | `java.util.logging` (JUL) | JDK built-in |
| Fat-JAR packaging | Shadow Plugin (johnrengelman) | 8.1.1 |
| Native image / jpackage | Beryx Runtime Plugin | 1.13.1 |
| Testing | JUnit 5 + Mockito 5 | 5.10.0 / 5.7.0 |

**Run command:** `.\gradlew.bat run`  
**Main entry point:** `com.example.smarttemplatefiller.Launcher` (thin wrapper → `MainApp`)  
**JavaFX modules declared:** `javafx.controls`, `javafx.fxml`, `javafx.graphics`

---

## 2. Directory Structure

```
SmartTemplateFiller/
├── build.gradle                    # Single-project Gradle build (no subprojects in main app)
├── settings.gradle                 # rootProject.name = 'SmartTemplateFiller'
├── gradlew / gradlew.bat           # Gradle wrapper
│
├── src/
│   ├── main/
│   │   ├── java/com/example/smarttemplatefiller/
│   │   │   ├── Launcher.java                   # Entry point (delegates to MainApp)
│   │   │   ├── MainApp.java                    # JavaFX Application; license gate → FXML load
│   │   │   ├── MainController.java             # Root screen controller (tab switching)
│   │   │   ├── TeachModeController.java        # Interactive mapping-builder UI
│   │   │   ├── RunningModeController.java      # Folder-watch automation UI
│   │   │   ├── FolderWatcher.java              # Background scheduler (ScheduledExecutorService)
│   │   │   ├── TxtParser.java                  # Multi-format TXT/ASC parser (3 strategies)
│   │   │   ├── ExcelWriter.java                # Apache POI export + append logic
│   │   │   ├── AppendResult.java               # Result VO for append operations
│   │   │   ├── ExportConfiguration.java        # POJO persisted to ~/.smarttemplatefiller/
│   │   │   ├── RunningModeConfig.java          # POJO persisted to ~/.smarttemplatefiller/
│   │   │   ├── FileChooserBuilder.java         # Fluent builder; remembers last directory (static)
│   │   │   │
│   │   │   ├── license/                        # License verification subsystem
│   │   │   │   ├── LicenseValidator.java           # Interface (validate() / validate(path))
│   │   │   │   ├── DefaultLicenseValidator.java    # Impl: config-check-first, 6-step flow
│   │   │   │   ├── ValidationResult.java           # Immutable result VO + ErrorCode enum
│   │   │   │   ├── LicenseConfig.java              # Reads license_config.json from classpath
│   │   │   │   ├── LicenseData.java                # Parsed license.json POJO (version/encrypted/sig)
│   │   │   │   ├── LicenseFileReader.java          # Reads & JSON-parses license.json file
│   │   │   │   ├── EncryptionValidator.java        # AES-CBC decrypt + HMAC-SHA256 verify
│   │   │   │   ├── HardwareIdentifier.java         # Interface: getMacAddresses() / getMotherboardSerial()
│   │   │   │   ├── OshiHardwareIdentifier.java     # OSHI impl (lazy-init, filters virtual/loopback)
│   │   │   │   ├── CurrentHardwareInfo.java        # Value object: MACs + mobo serial → deviceId hash
│   │   │   │   └── LicenseErrorDialog.java         # JavaFX dialog shown on validation failure
│   │   │   │
│   │   │   └── tools/                          # In-process CLI tools (not shipped to end users)
│   │   │       ├── HardwareInfoExtractor.java      # Prints current device identifiers
│   │   │       ├── LicenseCryptoUtils.java         # Standalone AES+HMAC util (duplicates EncryptionValidator)
│   │   │       ├── LicenseGenerator.java           # Generates license.json files
│   │   │       └── LicenseRenewer.java             # Updates expiry on existing license files
│   │   │
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── main.fxml                   # Root layout (tab container)
│   │       │   ├── teach_mode.fxml             # Teach Mode screen layout
│   │       │   ├── running_mode.fxml           # Running Mode screen layout
│   │       │   └── license_error_dialog.fxml   # License failure dialog
│   │       ├── license_config.json             # Controls license checking (enabled flag)
│   │       ├── column_config.json              # Fixed-column widths for TxtParser
│   │       └── advanced_mapping_example.json   # Example mapping file structure
│   │
│   └── test/
│       └── java/com/example/smarttemplatefiller/
│           ├── AppendIntegrationTest.java          # Integration tests for append mode
│           ├── ExcelWriterAppendTest.java          # Unit tests for ExcelWriter append
│           └── license/
│               ├── LicenseIntegrationTest.java     # End-to-end license flow
│               ├── LicenseValidatorTest.java       # DefaultLicenseValidator unit tests
│               ├── EncryptionValidatorTest.java    # Crypto unit tests
│               ├── LicenseConfigTest.java          # Config loading tests
│               ├── LicenseDataTest.java            # POJO/serialization tests
│               ├── MockHardwareIdentifier.java     # Test double for HardwareIdentifier
│               └── TestLicenseGenerator.java       # In-test license generation helper
│
├── tools/                           # Standalone Gradle subprojects for operator tooling
│   ├── HardwareInfoExtractor/       # Extracts device ID for license creation
│   ├── LicenseGenerator/           # Generates signed license.json files
│   └── LicenseRenewer/             # Renews license expiry
│
├── docs/                            # Architecture docs, feature specs, decisions
├── specs/                           # Feature specification files (.specify workflow)
├── .agent/                          # Antigravity agent workflows and skills
├── .specify/                        # SpecKit memory and constitution files
│
├── mapping.json                     # Sample mapping file (root-level, used for dev/testing)
└── column_config.json               # Sample column config (root-level override)
```

---

## 3. Application Boot Flow

```
Launcher.main()
  └─ MainApp.start(Stage)
       ├─ 1. LicenseValidator.validate()          ← blocks startup
       │    ├─ load license_config.json (classpath resource)
       │    ├─ if !enabled  → ValidationResult.success()  [bypass]
       │    ├─ LicenseFileReader.read(licenseFilePath)
       │    ├─ EncryptionValidator.verifySignature(version, encrypted, sig)
       │    ├─ EncryptionValidator.decrypt(encryptedData)
       │    ├─ HardwareIdentifier.getCurrentHardware() → computeDeviceId()
       │    ├─ compare deviceId
       │    └─ check expiry (epoch millis)
       ├─ 2. if !valid → LicenseErrorDialog.show() + Platform.exit()
       └─ 3. FXMLLoader("/fxml/main.fxml") → MainController → primaryStage.show()
```

---

## 4. Operating Modes

### Manual Export (Teach Mode)
- User loads a `.txt`/`.asc` file → `TxtParser.parseFile()` auto-detects format
- User interactively builds column→cell mappings in `TeachModeController`
- Mapping saved as JSON array; exported via `ExcelWriter.writeAdvancedMappedFile()`

### Running Mode (Folder Watch)
- `RunningModeController` configures and starts/stops a `FolderWatcher`
- `FolderWatcher` uses `ScheduledExecutorService` (single thread, fixed-rate poll)
- On new file detection → `ExcelWriter.writeAdvancedMappedFile()` or `ExcelWriter.appendToMappedFile()`
- Output archived to `outputFolder/<mappingName>/<timestamp>/archive/`
- Session state (`lastGeneratedFilePath`) persisted via `RunningModeConfig`

---

## 5. Key Data Contracts

### Mapping JSON (array format — legacy/simple)
```json
[
  {
    "sourceColumn": 4,
    "startCell": "A2",
    "direction": "vertical",
    "title": "",
    "rowIndexes": [1, 3, 5, 7]
  }
]
```

### Mapping JSON (object format — advanced)
```json
{
  "colMappings": [
    { "sourceColumn": 1, "startCell": "B2", "direction": "vertical", "rowIndexes": [1, 3, 5] }
  ]
}
```

Both formats are supported by `ExcelWriter`. The advanced format wraps entries in a `colMappings` key.

### Config Files (persisted to `~/.smarttemplatefiller/`)
- `running_mode_config.json` — watch folder, output folder, mapping path, interval, append mode
- `export_configuration.json` — append mode flag, last target path

### License JSON (`license.json`, external to JAR)
```json
{ "version": "1", "encryptedData": "<base64>", "signature": "<base64-hmac>" }
```

---

## 6. TxtParser — Format Auto-Detection

`TxtParser.parseFile()` reads the first 10 lines to classify format:

| Detection rule | Parser invoked |
|---|---|
| Line matches `(Circle\|Line\|Plane...).*\(ID:.*\)` | `parseMultiLineGroupedBlock()` — CMM block format |
| Line matches `\s*\d+\s+N\d+\s+.*\s+\*+` | `parseFixedColumnTable()` — fixed-width columns from `column_config.json` |
| Otherwise | `parseFlatTable()` — split on 2+ whitespace |

`generateIndexes(totalRows, patternType, startIndex)` generates row index lists supporting `"odd"`, `"even"`, `"all"` patterns.

---

## 7. License Subsystem Internals

### Crypto (AES-128-CBC + HMAC-SHA256)
- Secret key: 32-byte hardcoded key assembled from integer arrays (obfuscation via split ints)
- Encryption: `AES/CBC/PKCS5Padding` with random 16-byte IV prepended to ciphertext, then Base64
- Signature: `HmacSHA256(version + encryptedData)` → Base64
- `EncryptionValidator` in main app; `LicenseCryptoUtils` in `tools` package (intentionally duplicated to keep tools standalone)

### Hardware Fingerprint
- `OshiHardwareIdentifier` extracts physical MAC addresses (filters loopback, virtual, VPN adapters) + motherboard serial
- Falls back to system serial if motherboard serial is `"unknown"` / `"to be filled by o.e.m."`
- `CurrentHardwareInfo.computeDeviceId()` produces a deterministic hash from MACs + serial
- `OshiHardwareIdentifier` uses **lazy, synchronized initialization** of `SystemInfo` to defer OSHI startup cost

### Dev Bypass
- Set `"enabled": false` in `src/main/resources/license_config.json` → all validation skipped at startup
- Used during development/testing; must be `true` in production builds

---

## 8. Coding Conventions Observed

### API / Architecture Patterns
- **Interface → Implementation** separation for all subsystem boundaries:  
  `LicenseValidator` ← `DefaultLicenseValidator`  
  `HardwareIdentifier` ← `OshiHardwareIdentifier` (injectable for testing)
- **Factory method pattern** on result VOs: `ValidationResult.success()`, `ValidationResult.failure(...)`, `AppendResult.success(...)`, `AppendResult.failure(...)`
- **Fluent builder pattern**: `FileChooserBuilder` with method chaining
- **POJO + Jackson** for all Config classes: `@JsonIgnoreProperties(ignoreUnknown = true)` always present; `load()` / `save()` static factory/IO methods on the class itself
- **FXML + Controller**: All UI defined in FXML; controllers implement `Initializable`; stage injected via `setStage(Stage)` after `FXMLLoader.getController()`

### Thread-Safety Rules (Critical)
- `FolderWatcher` runs on a **`ScheduledExecutorService` background thread**
- All UI updates from background threads MUST use `Platform.runLater()`  
  ✅ See: `RunningModeController.logMessage()` → `Platform.runLater(() -> activityLogArea.appendText(...))`
- `processingFiles` `HashSet` in `FolderWatcher` is guarded with `synchronized(processingFiles)` blocks — do not remove synchronization
- `OshiHardwareIdentifier.ensureInitialized()` is `synchronized` to prevent double-init
- `FolderWatcher.running` is declared `volatile`

### Logging
- Use `java.util.logging.Logger` (JUL) — **not** Log4j or SLF4J — for all application code
- Logger instantiation: `private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());`
- Use parameterized logging: `LOGGER.log(Level.INFO, "msg: {0}", value)` — avoid string concatenation
- `FolderWatcher` has an additional in-UI log callback `Consumer<String>` for the activity log area

### Jackson Serialization
- All config POJOs have a no-arg constructor (required for Jackson)
- Fields that should not be serialized: `@JsonIgnore` + `transient` (e.g., `lastGeneratedFilePath`)
- `ObjectMapper` is instantiated locally per read/write call (not a shared singleton)

### Error Handling
- Catch specific exceptions, not bare `Exception`, where possible
- Return result objects (`ValidationResult`, `AppendResult`) rather than throwing from core logic
- Background thread errors logged via the `logCallback` consumer to keep them visible in the UI
- File I/O in controllers uses `showAlert()` pattern for user-facing errors

### Naming
- Controller classes: `<Feature>Controller` (e.g., `TeachModeController`, `RunningModeController`)
- Config POJOs: `<Feature>Config` (e.g., `RunningModeConfig`, `ExportConfiguration`)
- Result VOs: `<Operation>Result` (e.g., `ValidationResult`, `AppendResult`)
- FXML files: `snake_case.fxml` matching the controller name (`running_mode.fxml`)
- Task tags in comments: `T018`, `T019`, etc. trace to feature task IDs in `.specify/`

### Testing Patterns
- JUnit 5 with `@Test`, `@BeforeEach`; Mockito for mocking external dependencies
- `MockHardwareIdentifier` — hand-rolled test double (not Mockito) for hardware injection
- `TestLicenseGenerator` — in-test helper that generates valid license JSON for integration tests
- Integration tests generate temp files; expect cleanup via `@TempDir` or manual delete

---

## 9. Config & Persistence Locations

| File | Location | Notes |
|---|---|---|
| `license_config.json` | Classpath resource (`/license_config.json`) | Bundled in JAR; `enabled` flag controls validation |
| `license.json` | Beside the application executable (configurable) | External; not bundled |
| `running_mode_config.json` | `~/.smarttemplatefiller/running_mode_config.json` | Auto-created on first save |
| `export_configuration.json` | `~/.smarttemplatefiller/export_configuration.json` | Auto-created on first save |
| `column_config.json` | Working directory (PWD) | Required for fixed-column parsing; fallback values built-in |

---

## 10. Build & Packaging

```powershell
# Run in development
.\gradlew.bat run

# Run tests
.\gradlew.bat test

# Build fat-JAR (Shadow)
.\gradlew.bat shadowJar

# Build native image folder (jpackage, skips installer)
.\gradlew.bat runtime
```

- `jpackage` target: `skipInstaller = true`; produces a self-contained app folder named `SmartTemplateFiller`
- `noConsole = true` in launcher config (Windows GUI app, no terminal window)
- `--strip-debug --compress 2 --no-header-files --no-man-pages` for smaller runtime image

---

## 11. External Tooling (`tools/` subprojects)

These are **operator-only tools** not shipped to end users:

| Tool | Purpose |
|---|---|
| `HardwareInfoExtractor` | Run on customer machine to get device fingerprint for license generation |
| `LicenseGenerator` | Generate a signed `license.json` for a specific device + expiry |
| `LicenseRenewer` | Decrypt existing license, update expiry, re-encrypt and re-sign |

All share `LicenseCryptoUtils` (deliberately duplicated from `EncryptionValidator` to keep tools standalone and independently runnable).
