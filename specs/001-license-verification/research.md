# Research: Hardware-Based License Verification

**Feature**: 001-license-verification  
**Date**: 2026-01-28  
**Purpose**: Research technical approaches for hardware detection, cryptographic validation, and license file design

## 1. Hardware Detection Library: OSHI vs JNA

### Decision: Use OSHI (oshi-core)

**Rationale**: OSHI (Operating System and Hardware Information) is the industry-standard Java library for cross-platform hardware detection.

**Key Advantages**:
- **Cross-platform**: Windows, macOS, Linux support with single API
- **JNA-based**: No native library installation required (uses Java Native Access)
- **Active maintenance**: Widely adopted, regularly updated
- **Proven reliability**: Battle-tested in production systems
- **Simple API**: `SystemInfo` → `NetworkIF` (MAC address), `ComputerSystem.getBaseboard()` (motherboard serial)

**Implementation Pattern**:
```java
SystemInfo si = new SystemInfo();

// MAC Address extraction (supports multiple adapters)
List<NetworkIF> networks = si.getHardware().getNetworkIFs();
List<String> macAddresses = networks.stream()
    .map(NetworkIF::getMacaddr)
    .filter(mac -> !mac.isEmpty())
    .collect(Collectors.toList());

// Motherboard serial number
String serialNumber = si.getHardware()
    .getComputerSystem()
    .getBaseboard()
    .getSerialNumber();
```

**Gradle Dependency**:
```gradle
implementation 'com.github.oshi:oshi-core:6.4.+'
```

**Alternatives Considered**:
- **Raw JNA**: More control, but significantly more complex and platform-specific code
- **Native OS commands** (`wmic`, `system_profiler`): Not portable, requires process execution, fragile

---

## 2. Cryptographic Approach: AES-256 Encryption + HMAC-SHA256

### Decision: Hybrid approach using AES-256 (confidentiality) + HMAC-SHA256 (integrity)

**Rationale**: Prevent users from seeing license structure while ensuring tamper detection

**Security Layers**:
1. **AES-256 Encryption**: Obfuscate hardware identifiers and expiry date
2. **HMAC-SHA256**: Detect any tampering with encrypted data
3. **Embedded Secret Key**: Single 256-bit key for both operations

**Why Encryption + HMAC?**
- **Problem with HMAC-only**: Users can see MAC addresses, serial numbers, and logic in plaintext JSON
- **Solution**: Encrypt sensitive data first, then sign the encrypted blob
- **Result**: Users cannot guess the license format or modify values

### Implementation Pattern

**License Generation (in LicenseGenerator tool)**:
```java
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

// Step 1: Create deviceId (SHA-256 hash of hardware)
String macsSorted = macAddresses.stream().sorted().collect(Collectors.joining("|"));
String deviceData = macsSorted + "|" + motherboardSerial;
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] deviceIdBytes = digest.digest(deviceData.getBytes(StandardCharsets.UTF_8));
String deviceId = Base64.getEncoder().encodeToString(deviceIdBytes);

// Step 2: Create payload (deviceId + expiry)
String payload = deviceId + "|" + expiryTimestamp;

// Step 3: Encrypt payload with AES-256
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
SecretKeySpec aesKey = new SecretKeySpec(SECRET_KEY_BYTES, "AES");

// Generate random IV for AES
byte[] iv = new byte[16];
SecureRandom random = new SecureRandom();
random.nextBytes(iv);
IvParameterSpec ivSpec = new IvParameterSpec(iv);

cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
byte[] encryptedBytes = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

// Combine IV + encrypted data
byte[] combined = new byte[iv.length + encryptedBytes.length];
System.arraycopy(iv, 0, combined, 0, iv.length);
System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
String encryptedData = Base64.getEncoder().encodeToString(combined);

// Step 4: Generate HMAC signature over version + encryptedData
String signData = version + encryptedData;
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec hmacKey = new SecretKeySpec(SECRET_KEY_BYTES, "HmacSHA256");
mac.init(hmacKey);
byte[] hmacBytes = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
String signature = Base64.getEncoder().encodeToString(hmacBytes);
```

**License Validation (in SmartTemplateFiller)**:
```java
// Step 1: Verify HMAC signature (tamper detection)
String signData = licenseData.getVersion() + licenseData.getEncryptedData();
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec hmacKey = new SecretKeySpec(SECRET_KEY_BYTES, "HmacSHA256");
mac.init(hmacKey);
byte[] computedHmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
String computedSignature = Base64.getEncoder().encodeToString(computedHmac);

if (!MessageDigest.isEqual(
    computedSignature.getBytes(StandardCharsets.UTF_8),
    licenseData.getSignature().getBytes(StandardCharsets.UTF_8)
)) {
    return ValidationResult.failure(ErrorCode.SIGNATURE_MISMATCH);
}

// Step 2: Decrypt data
byte[] combined = Base64.getDecoder().decode(licenseData.getEncryptedData());
byte[] iv = Arrays.copyOfRange(combined, 0, 16);
byte[] encryptedBytes = Arrays.copyOfRange(combined, 16, combined.length);

Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
SecretKeySpec aesKey = new SecretKeySpec(SECRET_KEY_BYTES, "AES");
cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
String payload = new String(decryptedBytes, StandardCharsets.UTF_8);

// Step 3: Parse payload (deviceId|expiryTimestamp)
String[] parts = payload.split("\\|");
String storedDeviceId = parts[0];
long expiryTimestamp = Long.parseLong(parts[1]);

// Step 4: Compute current deviceId
CurrentHardwareInfo current = hardwareIdentifier.getCurrentHardware();
String currentMacsSorted = current.getMacAddresses().stream()
    .sorted().collect(Collectors.joining("|"));
String currentDeviceData = currentMacsSorted + "|" + current.getMotherboardSerial();
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] currentDeviceIdBytes = digest.digest(currentDeviceData.getBytes(StandardCharsets.UTF_8));
String currentDeviceId = Base64.getEncoder().encodeToString(currentDeviceIdBytes);

// Step 5: Validate deviceId match
if (!storedDeviceId.equals(currentDeviceId)) {
    return ValidationResult.failure(ErrorCode.HARDWARE_MISMATCH);
}

// Step 6: Validate expiry
if (Instant.now().toEpochMilli() > expiryTimestamp) {
    return ValidationResult.failure(ErrorCode.LICENSE_EXPIRED);
}

return ValidationResult.success();
```

**Security Benefits**:
✅ Hardware identifiers hidden (cannot see MAC addresses or serial numbers)  
✅ Users cannot guess license format or logic  
✅ Double protection: AES (confidentiality) + HMAC (integrity)  
✅ deviceId hashing prevents direct hardware comparison  
✅ Random IV for each license (prevents pattern analysis)  

**Key Management**:
- **Single 256-bit secret key**: Used for both AES and HMAC
- **Embedded in application**: Obfuscated using ProGuard/R8
- **Never distributed**: LicenseGenerator.jar kept by administrator only

---

## 3. License File Format

### Decision: Obfuscated JSON with encrypted data blob

**Rationale**: Prevent users from understanding license structure while maintaining JSON parsability

**Schema** (Option A - Encrypted JSON):
```json
{
  "v": "1.0",
  "d": "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdAAQMDA6MUE6MkI6M0M6NEQ6NUV0ABBB...",
  "s": "dGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIEhNQUM="
}
```

**Field Descriptions** (obfuscated keys):
- `v`: Version (schema version for backward compatibility)
- `d`: Encrypted data blob (Base64-encoded AES-256 encrypted payload)
- `s`: Signature (Base64-encoded HMAC-SHA256 for tamper detection)

**What's Hidden Inside Encrypted Data**:
```
Payload before encryption:
deviceId|expiryTimestamp

Where:
- deviceId = SHA256(macAddresses.sorted().join("|") + motherboardSerial)
- expiryTimestamp = Unix timestamp (milliseconds)
```

**Encryption Details**:
- **Algorithm**: AES-256 in CBC mode with PKCS5 padding
- **IV**: Random 16-byte Initialization Vector prepended to encrypted data
- **Key**: 256-bit secret key embedded in application
- **Output**: `IV (16 bytes) + Encrypted Payload` → Base64 encoded

**HMAC Signature**:
```
Signature Input = version + encryptedData
Signature = Base64(HMAC-SHA256(signatureInput, SECRET_KEY))
```

**File Location**: Application directory, named `license.json`

**Jackson Mapping**:
```java
@Data
@JsonPropertyOrder({"v", "d", "s"})  // Maintain consistent field order
public class LicenseData {
    @JsonProperty("v")
    private String version;
    
    @JsonProperty("d")
    private String encryptedData;  // Base64 encoded
    
    @JsonProperty("s")
    private String signature;  // Base64 encoded HMAC
}
```

**Advantages Over Plaintext**:
✅ Users cannot see MAC addresses, serial numbers, or expiry dates  
✅ Cannot reverse-engineer the license logic  
✅ Professional/enterprise-grade obfuscation  
✅ Still parsable by Jackson (existing dependency)  
✅ Compact format (shorter field names)

---

## 4. Error Dialog UI Design

### Decision: JavaFX Dialog with FXML layout

**Rationale**: Consistent with existing JavaFX UI, configurable via external resources

**FXML Structure**:
```fxml
<DialogPane xmlns:fx="http://javafx.com/fxml">
    <header>
        <HBox>
            <ImageView fx:id="companyLogo" fitWidth="60" fitHeight="60" />
            <Label text="License Validation Failed" styleClass="header-title" />
        </HBox>
    </header>
    <content>
        <VBox spacing="10" alignment="CENTER">
            <Label fx:id="errorMessageLabel" wrapText="true" />
        </VBox>
    </content>
    <buttonTypes>
        <ButtonType text="OK" />
    </buttonTypes>
</DialogPane>
```

**Configuration JSON** (`license_config.json`):
```json
{
  "enabled": true,
  "errorMessage": "Kindly contact Mitutoyo for more details.",
  "logoPath": "resources/mitutoyo_logo.png"
}
```

**Loading Pattern**:
```java
LicenseConfig config = LicenseConfig.load("license_config.json");
errorMessageLabel.setText(config.getErrorMessage());
if (config.getLogoPath() != null && Files.exists(Paths.get(config.getLogoPath()))) {
    companyLogo.setImage(new Image(config.getLogoPath()));
}
```

**UI Behavior**:
- **Modal**: Blocks all interaction with main application
- **No close button**: User must click "OK" to dismiss
- **Application exit**: On dialog close, call `Platform.exit()` to terminate

---

## 5. License Utility Tools Design

### Decision: Three-utility architecture for centralized license management

**Rationale**: Separate concerns between customer (hardware extraction) and administrator (license generation)

---

### Tool 1: HardwareInfoExtractor (Distribute to Customers)

**Purpose**: Extract hardware identifiers without exposing secret key

**User Flow**:
```bash
java -jar HardwareInfoExtractor.jar

# Output:
========================================
SmartTemplateFiller Hardware Information
========================================
MAC Addresses:
  - 00:1A:2B:3C:4D:5E
  - AA:BB:CC:DD:EE:FF

Motherboard Serial: ABC123456789

Please send this information to Mitutoyo
to receive your license file.
========================================
```

**Security**: No secret key embedded; safe to distribute

**Gradle Structure**:
```text
tools/HardwareInfoExtractor/
├── build.gradle
├── src/main/java/
│   └── HardwareExtractor.java  // Uses OSHI library
└── README.md
```

---

### Tool 2: LicenseGenerator (Administrator Only - KEEP PRIVATE)

**Purpose**: Generate encrypted license files from hardware info

**User Flow (Interactive)**:
```bash
java -jar LicenseGenerator.jar

Enter MAC addresses (comma-separated): 00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF
Enter motherboard serial: ABC123456789
Enter expiry date (YYYY-MM-DD): 2027-12-31
Output file name: customer_license.json

✓ License generated successfully: customer_license.json
```

**User Flow (Command-line)**:
```bash
java -jar LicenseGenerator.jar \
  --mac "00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF" \
  --serial "ABC123456789" \
  --expiry "2027-12-31" \
  --output "mitutoyo_japan_license.json"
```

**Security**: Contains embedded secret key (256-bit AES/HMAC key)

**CLI Features**:
- Validates MAC address format
- Validates serial number (non-empty)
- Validates expiry date format and ensures future date
- Encrypts data with AES-256 + CBC + random IV
- Signs with HMAC-SHA256

**Gradle Structure**:
```text
tools/LicenseGenerator/
├── build.gradle
├── src/main/java/
│   ├── LicenseGeneratorCLI.java
│   ├── EncryptionUtils.java      // AES + HMAC logic
│   └── SecretKeyHolder.java      // Embedded 256-bit key
└── README.md
```

---

### Tool 3: LicenseRenewer (Optional - Administrator Only)

**Purpose**: Extend expiry date of existing license without re-entering hardware info

**User Flow**:
```bash
java -jar LicenseRenewer.jar \
  --license "existing_license.json" \
  --new-expiry "2028-12-31" \
  --output "renewed_license.json"

✓ License renewed: renewed_license.json
```

**How it Works**:
1. Decrypt existing license to extract deviceId
2. Create new payload with same deviceId but new expiry
3. Re-encrypt and re-sign
4. Output new license file

**Gradle Structure**:
```text
tools/LicenseRenewer/
├── build.gradle
├── src/main/java/
│   └── LicenseRenewer.java
└── README.md
```

---

### Recommended Workflow

**For New Customers**:
1. Send customer `HardwareInfoExtractor.jar`
2. Customer runs it and sends you output via email
3. You run `LicenseGenerator.jar` with their hardware info
4. Send generated `license.json` to customer

**For License Renewals**:
1. You run `LicenseRenewer.jar` on existing license file
2. Send new `license.json` to customer (hardware info unchanged)

**Security Benefits**:
✅ Secret key never leaves your control  
✅ Customers cannot generate unlimited licenses  
✅ You control all expiry dates  
✅ Professional licensing model

---

## 6. Integration with MainApp Startup

### Decision: Validate before `start()` method shows primary stage

**Pattern**:
```java
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // STEP 1: License validation
        LicenseValidator validator = new LicenseValidator();
        ValidationResult result = validator.validate();
        
        if (!result.isValid()) {
            // Show error dialog (blocking)
            LicenseErrorDialog.show(result.getErrorMessage());
            Platform.exit(); // Terminate application
            return;
        }
        
        // STEP 2: Normal application startup
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        // ... continue normal flow
    }
}
```

**Performance**:
- OSHI initialization: ~500ms
- HMAC computation: <10ms
- File I/O: <50ms
- **Total**: <2 seconds (meets SC-003 requirement)

---

## Summary of Decisions

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| Hardware Detection | OSHI (`oshi-core:6.4.+`) | Cross-platform, JNA-based, widely adopted |
| Cryptographic Validation | javax.crypto.Mac (built-in) | No external deps, proven, HMAC-SHA256 |
| License File Format | JSON (Jackson) | Human-readable, extensible, existing dependency |
| Error Dialog | JavaFX FXML | Consistent with existing UI patterns |
| Key Generator | Standalone Java CLI tool | Simple for administrators, separate concern |
| Integration Point | MainApp.start() before UI loads | Blocks unauthorized access immediately |

**No further clarifications needed** - all technical approaches resolved.
