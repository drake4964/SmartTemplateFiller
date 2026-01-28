package com.example.smarttemplatefiller.license;

import java.util.List;

/**
 * Extracts hardware identifiers (MAC addresses, motherboard serial)
 * from the current system for license validation.
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
     */
    List<String> getMacAddresses();

    /**
     * Extracts the motherboard serial number.
     *
     * @return Motherboard serial number (empty string if unavailable)
     */
    String getMotherboardSerial();

    /**
     * Extracts all hardware information in a single call (optimized).
     * Reuses a single SystemInfo instance to avoid double initialization.
     *
     * @return CurrentHardwareInfo containing all extracted hardware data
     */
    CurrentHardwareInfo getCurrentHardware();
}
