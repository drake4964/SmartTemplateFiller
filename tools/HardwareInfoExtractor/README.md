# Hardware Information Extractor

A customer-facing utility for extracting hardware information required for license generation.

## Overview

This tool extracts your computer's unique hardware identifiers (MAC addresses and motherboard serial number) which are needed to generate a license for the SmartTemplateFiller application.

**Important**: This utility is safe to run - it does NOT contain any secret keys and cannot generate licenses. It only reads your hardware information.

## Requirements

- Java 17 or later

## Usage

### Option 1: Run the JAR file

```bash
java -jar HardwareInfoExtractor.jar
```

### Option 2: Build from source

```bash
cd tools/HardwareInfoExtractor
./gradlew jar
java -jar build/libs/HardwareInfoExtractor-1.0.0.jar
```

## Example Output

```text
================================================================================
  Hardware Information Extractor v1.0.0
  For License Registration
================================================================================

Extracting hardware information...

================================================================================
  HARDWARE INFORMATION - SEND THIS TO YOUR ADMINISTRATOR
================================================================================

MAC Addresses:
  1. 00:1A:2B:3C:4D:5E
  2. AA:BB:CC:DD:EE:FF

Motherboard Serial:
  ABC123456789

================================================================================

--- Copy-Paste Friendly Format ---

MAC_ADDRESSES=00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF
MOTHERBOARD_SERIAL=ABC123456789

================================================================================
Please copy the information above and send it to your administrator.
They will generate a license file for your machine.
================================================================================
```

## What to Do Next

1. Run this utility on the computer where you want to use SmartTemplateFiller
2. Copy the hardware information from the output
3. Send this information to your administrator
4. Your administrator will generate a `license.json` file
5. Place the `license.json` file in the same directory as SmartTemplateFiller

## Troubleshooting

### "Failed to extract hardware information"

- **Windows**: Try running as Administrator
- **Linux/macOS**: Ensure you have read permissions for system files

### "No MAC addresses found"

- Ensure you have at least one active network adapter
- Virtual network adapters (VPN, Docker, VMware) are filtered out

### "Motherboard Serial: [NOT AVAILABLE]"

Some systems don't provide motherboard serial numbers. In this case, the system serial will be used as a fallback. If neither is available, contact your administrator.

## Security Notes

- This utility only **reads** hardware information - it does not modify anything
- No data is sent over the network - all information is displayed locally
- The extracted information is NOT sufficient to generate a valid license (the secret key is required)
