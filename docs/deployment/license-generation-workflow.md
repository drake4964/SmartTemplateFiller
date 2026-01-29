# License Generation Workflow

This guide covers the complete workflow for generating, distributing, and renewing licenses.

## Prerequisites

Build the utility tools:

```bash
# Customer utility
cd tools/HardwareInfoExtractor
.\gradlew.bat jar

# Admin utilities
cd ../LicenseGenerator
.\gradlew.bat jar

cd ../LicenseRenewer
.\gradlew.bat jar
```

## New License Workflow

### Step 1: Customer Extracts Hardware Info

Customer runs:
```bash
java -jar HardwareInfoExtractor-1.0.0.jar
```

Output:
```
================================================================================
  SmartTemplateFiller Hardware Information
================================================================================

MAC Addresses:
  1. 00:1A:2B:3C:4D:5E
  2. AA:BB:CC:DD:EE:FF

Motherboard Serial:
  XYZ789012345

================================================================================
```

Customer sends this information to admin.

### Step 2: Admin Generates License

Admin runs:
```bash
java -jar LicenseGenerator-1.0.0.jar \
    --mac 00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF \
    --serial XYZ789012345 \
    --expiry 2027-12-31 \
    --output customer_license.json
```

### Step 3: Deliver License to Customer

Admin sends `customer_license.json` to customer.

### Step 4: Customer Installs License

Customer places `license.json` in the SmartTemplateFiller directory and launches the app.

## License Renewal Workflow

### Step 1: Customer Sends Existing License

Customer sends their current `license.json` to admin.

### Step 2: Admin Renews License

Admin runs:
```bash
java -jar LicenseRenewer-1.0.0.jar \
    --license customer_license.json \
    --new-expiry 2029-12-31 \
    --output renewed_license.json
```

### Step 3: Deliver Renewed License

Admin sends `renewed_license.json` to customer to replace their old license.

## Troubleshooting

### "License file not found"
Ensure `license.json` is in the correct directory and path matches `license_config.json`.

### "Hardware mismatch"
License was generated for different hardware. Customer must re-run HardwareInfoExtractor and request a new license.

### "License expired"
Customer needs a renewed license. Use LicenseRenewer to extend expiry.

### "Invalid signature"
License file is corrupted or tampered. Customer must request a new license.

## Security Reminders

⚠️ **Never distribute** LicenseGenerator or LicenseRenewer to customers  
⚠️ **Keep secret key** secure - it's embedded in admin tools  
✅ **Safe to distribute** HardwareInfoExtractor to all customers
