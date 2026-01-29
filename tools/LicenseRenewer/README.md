# License Renewer

**ADMIN ONLY** - This tool renews license expiry dates for SmartTemplateFiller.

## ⚠️ Security Warning

This tool contains the secret encryption key and can renew licenses.
**Never distribute this tool to customers!**

## Requirements

- Java 17 or later
- A valid existing license file

## Usage

```bash
java -jar LicenseRenewer.jar --license <existing> --new-expiry <date> [--output <file>]
```

### Options

| Option | Short | Required | Description |
|--------|-------|----------|-------------|
| `--license` | `-l` | Yes | Path to existing license file |
| `--new-expiry` | `-e` | Yes | New expiry date in yyyy-MM-dd format |
| `--output` | `-o` | No | Output file (default: renewed_license.json) |
| `--help` | `-h` | No | Show help message |
| `--version` | `-v` | No | Show version |

### Examples

Renew a license to a new date:
```bash
java -jar LicenseRenewer.jar --license license.json --new-expiry 2028-12-31
```

Specify output file:
```bash
java -jar LicenseRenewer.jar -l customer_license.json -e 2029-06-30 -o renewed_customer.json
```

## How It Works

The renewal process:

1. **Read** - Load the existing license JSON file
2. **Verify** - Check the HMAC signature is valid (detect tampering)
3. **Decrypt** - Decrypt to extract the device ID
4. **Update** - Create new payload with same device ID but new expiry
5. **Encrypt** - Encrypt with new random IV
6. **Sign** - Generate new HMAC signature
7. **Save** - Write the renewed license file

> [!IMPORTANT]
> The device ID remains unchanged during renewal. This ensures the license still works only on the original customer's hardware.

## Workflow

### Renewing a Customer License

1. Ask the customer to send their current `license.json` file
2. Run LicenseRenewer with desired new expiry date
3. Send the renewed license file back to the customer
4. Customer replaces their old license file

### Example Session

```text
================================================================================
  License Renewer v1.0.0
  ADMIN ONLY - Do not distribute to customers
================================================================================

Processing license file: C:\licenses\customer_123.json

Step 1: Reading existing license...
  Version: 1.0
Step 2: Verifying signature...
  Signature: VALID
Step 3: Decrypting payload...
  Device ID: k9Xp2mL8nQ4rT7wY...
  Old Expiry: 2027-01-01
Step 4: Creating new payload...
  New Expiry: 2028-12-31
Step 5: Encrypting new payload...
Step 6: Generating new signature...
Step 7: Saving renewed license...

================================================================================
  LICENSE RENEWED SUCCESSFULLY
================================================================================

Renewal Details:
  Original File:   C:\licenses\customer_123.json
  Old Expiry:      2027-01-01
  New Expiry:      2028-12-31
  Output File:     C:\licenses\renewed_license.json

================================================================================
Send the renewed license file to the customer.
================================================================================
```

## Building from Source

```bash
cd tools/LicenseRenewer
gradle wrapper --gradle-version 8.5
./gradlew jar
```

The JAR will be in `build/libs/LicenseRenewer-1.0.0.jar`

## Troubleshooting

### "Invalid license signature"
The license file has been modified or corrupted. Request a fresh copy from the customer.

### "Encrypted data too short"
The license file is malformed. It may be a different format or corrupted.

### "New expiry date must be in the future"
You cannot renew a license to a date that has already passed.
