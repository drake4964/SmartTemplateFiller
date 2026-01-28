# Interface Contract: HardwareIdentifier

**Feature**: 001-license-verification  
**Date**: 2026-01-28  
**Purpose**: Extract hardware identifiers from the current system

## Interface Definition

```java
package com.example.smarttemplatefiller.license;

/**
 * Extracts hardware identifiers (MAC addresses, motherboard serial) 
 * from the current system using OSHI library.
 */
public interface HardwareIdentifier {
    
    /**
     * Extracts all MAC addresses from active network interfaces.
     * 
     * Filters:
     * - Excludes loopback interfaces
     * - Excludes virtual/tunneling adapters
     * - Includes only interfaces with non-empty MAC addresses
     * 
     * @return List of MAC addresses in format "XX:XX:XX:XX:XX:XX"
     * @throws HardwareExtractionException if OSHI initialization fails
     */
    List<String> getMacAddresses();
    
    /**
     * Extracts the motherboard serial number.
     * 
     * Uses OSHI's ComputerSystem → Baseboard → SerialNumber.
     * 
     * @return Motherboard serial number (empty string if unavailable)
     * @throws HardwareExtractionException if OSHI initialization fails
     */
    String getMotherboardSerial();
    
    /**
     * Extracts all hardware information in a single call (optimized).
     * 
     * Reuses a single SystemInfo instance to avoid double initialization.
     * 
     * @return CurrentHardwareInfo containing all extracted hardware data
     * @throws HardwareExtractionException if OSHI initialization fails
     */
    CurrentHardwareInfo getCurrentHardware();
}
```

## Implementation Notes

**Default Implementation**: `OshiHardwareIdentifier`

**Dependencies**:
- `oshi-core:6.4.+` (Gradle dependency)
- No other external dependencies

**OSHI Usage Pattern**:
```java
SystemInfo si = new SystemInfo();
HardwareAbstractionLayer hal = si.getHardware();

// MAC Addresses
List<NetworkIF> networks = hal.getNetworkIFs();
List<String> macs = networks.stream()
    .filter(net -> !net.isLoopback())
    .map(NetworkIF::getMacaddr)
    .filter(mac -> !mac.isEmpty())
    .collect(Collectors.toList());

// Motherboard Serial
String serial = hal.getComputerSystem()
    .getBaseboard()
    .getSerialNumber();
```

**Error Handling**:
- OSHI initialization failure → throw `HardwareExtractionException`
- Empty MAC list → return empty list (validation will fail gracefully)
- Empty motherboard serial → return empty string (validation will fail gracefully)

**Performance**:
- SystemInfo initialization: ~500ms
- Hardware extraction: <50ms
- **Optimization**: Cache SystemInfo instance if called multiple times (not recommended; validate once per startup)

## Edge Cases

| Scenario | Behavior |
|----------|----------|
| Virtual Machine | Returns virtual NIC MAC addresses (expected) |
| No Network Adapters | Returns empty MAC list → validation fails |
| Missing Motherboard Serial | Returns empty string → validation fails |
| Multiple Network Adapters | Returns all MACs → any match succeeds |

## Testing Strategy

**Unit Tests**:
- **Challenge**: Cannot mock OSHI easily
- **Solution**: Extract interface, create `MockHardwareIdentifier` for tests
- Test that mock returns controlled values

**Integration Tests**:
- Run on real hardware, extract MAC and serial
- Verify format: MAC matches regex `([0-9A-F]{2}:){5}[0-9A-F]{2}`
- Verify serial is non-empty string

**Test Implementation**:
```java
public class MockHardwareIdentifier implements HardwareIdentifier {
    private final List<String> macs;
    private final String serial;
    
    public MockHardwareIdentifier(List<String> macs, String serial) {
        this.macs = macs;
        this.serial = serial;
    }
    
    @Override
    public List<String> getMacAddresses() { return macs; }
    
    @Override
    public String getMotherboardSerial() { return serial; }
    
    @Override
    public CurrentHardwareInfo getCurrentHardware() {
        return new CurrentHardwareInfo(macs, serial);
    }
}
```

## Usage Example

```java
HardwareIdentifier identifier = new OshiHardwareIdentifier();
CurrentHardwareInfo current = identifier.getCurrentHardware();

System.out.println("MACs: " + current.getMacAddresses());
System.out.println("Serial: " + current.getMotherboardSerial());
```
