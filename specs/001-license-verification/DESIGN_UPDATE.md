# Design Update Summary: Enhanced License Security

**Date**: 2026-01-28  
**Feature**: 001-license-verification  
**Change Type**: Security Enhancement

## What Changed

Based on user feedback, the license verification design was updated from a simple HMAC-only approach to a more secure **AES-256 encryption + HMAC** hybrid approach.

## Key Changes

### 1. License Format Changed

**Before** (Plaintext JSON):
```json
{
  "version": "1.0",
  "hardware": {
    "macAddresses": ["00:1A:2B:3C:4D:5E"],
    "motherboardSerial": "ABC123"
  },
  "expiry": "2027-12-31T23:59:59Z",
  "signature": "..."
}
```

**After** (Encrypted JSON):
```json
{
  "v": "1.0",
  "d": "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdw...",
  "s": "dGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIEhNQUM="
}
```

**Benefits**:
- Users cannot see MAC addresses or serial numbers
- Cannot reverse-engineer license logic
- obfuscated field names (`v`, `d`, `s` instead of descriptive names)
- Professional-grade security

### 2. DeviceId Concept Introduced

Instead of storing raw hardware identifiers, we now use a SHA-256 hash:

```
deviceId = SHA256(macAddresses.sorted().join("|") + motherboardSerial)
```

This abstracts away specific hardware details into a single identifier.

### 3. Three-Utility Architecture

**Before**: Single LicenseGenerator tool

**After**: Three separate tools with different purposes:

| Tool | Audience | Contains Secret Key? | Purpose |
|------|----------|----------------------|---------|
| **HardwareInfoExtractor** | Customers | ❌ No | Extract MAC + serial, display for sending to admin |
| **LicenseGenerator** | Admin only | ✅ Yes | Generate encrypted licenses from hardware info |
| **License Renewer** | Admin only | ✅ Yes | Extend expiry dates without re-entering hardware |

### 4. Recommended Workflow

**Centralized License Management**:
1. Send customer `HardwareInfoExtractor.jar`
2. Customer runs it → sends output to you
3. You run `LicenseGenerator.jar` with their info
4. Send generated `license.json` to customer

**Security**: Secret key never leaves your control; customers cannot generate unlimited licenses.

## Updated Documents

| Document | Changes |
|----------|---------|
| `research.md` | Added AES-256 + HMAC implementation patterns, three-utility design |
| `data-model.md` | Changed LicenseData to use `encryptedData` field, added DeviceId concept, updated error codes |
| `plan.md` | Needs manual update to reflect EncryptionValidator class and three tools |
| `quickstart.md` | Needs update for new encrypted format and HardwareInfoExtractor usage |

## Implementation Impact

**New Classes Needed**:
- `EncryptionValidator.java` - AES-256 + HMAC logic (replaces `HmacValidator.java`)
- `EncryptionUtils.java` - Shared encryption utilities
- `HardwareExtractor.java` - Customer-facing tool
- ` LicenseRenewer.java` - Renewal utility

**Dependencies**:
- No new external dependencies (javax.crypto is built-in)
- Same OSHI library for hardware detection

## Next Steps

1. ✅ research.md - Updated
2. ✅ data-model.md - Updated
3. ⚠️ plan.md - Needs manual review/update for source structure
4. ⚠️ quickstart.md - Needs update for new workflows
5. Ready for `/speckit-tasks` to generate implementation tasks
