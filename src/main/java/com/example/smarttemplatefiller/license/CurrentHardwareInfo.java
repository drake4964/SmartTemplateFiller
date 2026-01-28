package com.example.smarttemplatefiller.license;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents hardware information extracted from the running system.
 * Contains MAC addresses and motherboard serial used for device identification.
 */
public class CurrentHardwareInfo {

    private final List<String> macAddresses;
    private final String motherboardSerial;

    /**
     * Creates a new CurrentHardwareInfo instance.
     *
     * @param macAddresses      List of MAC addresses from active network interfaces
     * @param motherboardSerial Motherboard serial number
     */
    public CurrentHardwareInfo(List<String> macAddresses, String motherboardSerial) {
        this.macAddresses = macAddresses != null ? List.copyOf(macAddresses) : Collections.emptyList();
        this.motherboardSerial = motherboardSerial != null ? motherboardSerial : "";
    }

    /**
     * Gets the list of MAC addresses.
     *
     * @return Unmodifiable list of MAC addresses
     */
    public List<String> getMacAddresses() {
        return macAddresses;
    }

    /**
     * Gets the motherboard serial number.
     *
     * @return Motherboard serial or empty string if not available
     */
    public String getMotherboardSerial() {
        return motherboardSerial;
    }

    /**
     * Computes the device ID hash from hardware information.
     * Uses SHA-256 hash of sorted MAC addresses and motherboard serial.
     *
     * @return Base64-encoded SHA-256 hash of hardware identifiers
     */
    public String computeDeviceId() {
        try {
            // Sort and join MAC addresses
            String macsSorted = macAddresses.stream()
                    .sorted()
                    .collect(Collectors.joining("|"));

            // Combine with motherboard serial
            String deviceData = macsSorted + "|" + motherboardSerial;

            // Compute SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(deviceData.getBytes(StandardCharsets.UTF_8));

            // Return Base64-encoded hash
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in Java
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks if any of the MAC addresses match those in another hardware info.
     *
     * @param other The other hardware info to compare
     * @return true if at least one MAC address matches
     */
    public boolean hasMatchingMac(CurrentHardwareInfo other) {
        if (other == null || other.macAddresses.isEmpty()) {
            return false;
        }
        return macAddresses.stream().anyMatch(other.macAddresses::contains);
    }

    /**
     * Checks if the hardware info is complete (has at least one MAC and a serial).
     *
     * @return true if hardware info appears valid
     */
    public boolean isComplete() {
        return !macAddresses.isEmpty() && !motherboardSerial.isEmpty();
    }

    @Override
    public String toString() {
        // Note: Hardware IDs are not logged for security - only show counts
        return "CurrentHardwareInfo{macCount=" + macAddresses.size() +
                ", hasSerial=" + !motherboardSerial.isEmpty() + "}";
    }
}
