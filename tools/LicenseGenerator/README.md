# License Generator

**ADMIN ONLY** - This tool generates encrypted license files for SmartTemplateFiller.

## ⚠️ Security Warning

This tool contains the secret encryption key and can generate valid licenses. 
**Never distribute this tool to customers!**

Keep this tool secure:
- Store it on admin-only systems
- Do not commit the JAR to public repositories
- Use strong access controls

## Requirements

- Java 17 or later

## Usage

### Interactive Mode

Run without arguments to use interactive prompts:

```bash
java -jar LicenseGenerator.jar
```

You will be prompted for:
1. MAC addresses (one per line, blank line when done)
2. Motherboard serial number
3. Expiry date (yyyy-MM-dd)
4. Output file path

### Command-Line Mode

For automation or scripting:

```bash
java -jar LicenseGenerator.jar --mac <MAC> --serial <SERIAL> --expiry <DATE> [--output <FILE>]
```

#### Options

| Option | Short | Required | Description |
|--------|-------|----------|-------------|
| `--mac` | `-m` | Yes | MAC address (can be repeated or comma-separated) |
| `--serial` | `-s` | Yes | Motherboard serial number |
| `--expiry` | `-e` | Yes | Expiry date in yyyy-MM-dd format |
| `--output` | `-o` | No | Output file (default: license.json) |
| `--help` | `-h` | No | Show help message |
| `--version` | `-v` | No | Show version |

#### Examples

Single MAC address:
```bash
java -jar LicenseGenerator.jar --mac 00:1A:2B:3C:4D:5E --serial ABC123 --expiry 2027-01-01
```

Multiple MAC addresses (comma-separated):
```bash
java -jar LicenseGenerator.jar -m 00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF -s XYZ789 -e 2028-06-30
```

Multiple MAC addresses (multiple flags):
```bash
java -jar LicenseGenerator.jar --mac 00:1A:2B:3C:4D:5E --mac AA:BB:CC:DD:EE:FF --serial XYZ789 --expiry 2028-06-30
```

Custom output file:
```bash
java -jar LicenseGenerator.jar -m 00:1A:2B:3C:4D:5E -s ABC123 -e 2027-01-01 -o customer_license.json
```

## Workflow

### Generating a License for a Customer

1. **Customer runs HardwareInfoExtractor** on their machine
2. **Customer sends you** the extracted MAC addresses and serial number
3. **You run LicenseGenerator** with their hardware info and desired expiry date
4. **Send the generated license.json** file to the customer
5. **Customer places license.json** in the SmartTemplateFiller directory

### Example Workflow

Customer provides:
```
MAC_ADDRESSES=00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF
MOTHERBOARD_SERIAL=PF123ABC
```

You run:
```bash
java -jar LicenseGenerator.jar \
  --mac 00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF \
  --serial PF123ABC \
  --expiry 2027-12-31 \
  --output PF123ABC_license.json
```

## Output Format

The generated license file is a JSON document:

```json
{
  "v": "1.0",
  "d": "<Base64-encoded encrypted data>",
  "s": "<Base64-encoded HMAC signature>"
}
```

- `v`: Schema version
- `d`: AES-256-CBC encrypted payload (device ID + expiry)
- `s`: HMAC-SHA256 signature for tamper detection

## Building from Source

```bash
cd tools/LicenseGenerator
gradle wrapper --gradle-version 8.5
./gradlew jar
```

The JAR will be in `build/libs/LicenseGenerator-1.0.0.jar`

## Troubleshooting

### Invalid MAC address format
MAC addresses must be in format `XX:XX:XX:XX:XX:XX` where X is a hexadecimal digit (0-9 or A-F).

### Expiry date must be in the future
The license expiry date must be after today's date.

### License validation fails after generation
Ensure the hardware info (MAC addresses and serial) exactly matches what the customer's machine reports. MAC addresses are case-insensitive but will be normalized to uppercase.
