package com.example.smarttemplatefiller.license;

import java.util.List;

/**
 * Mock implementation of HardwareIdentifier for testing.
 * Allows controlled simulation of hardware values in unit tests.
 */
public class MockHardwareIdentifier implements HardwareIdentifier {

    private final List<String> macAddresses;
    private final String motherboardSerial;

    /**
     * Creates a MockHardwareIdentifier with specified values.
     *
     * @param macAddresses      List of MAC addresses to return
     * @param motherboardSerial Motherboard serial to return
     */
    public MockHardwareIdentifier(List<String> macAddresses, String motherboardSerial) {
        this.macAddresses = macAddresses != null ? List.copyOf(macAddresses) : List.of();
        this.motherboardSerial = motherboardSerial != null ? motherboardSerial : "";
    }

    /**
     * Creates a MockHardwareIdentifier with a single MAC and serial.
     *
     * @param mac    Single MAC address
     * @param serial Motherboard serial
     */
    public MockHardwareIdentifier(String mac, String serial) {
        this(List.of(mac), serial);
    }

    /**
     * Creates an empty MockHardwareIdentifier (no hardware found).
     */
    public static MockHardwareIdentifier empty() {
        return new MockHardwareIdentifier(List.of(), "");
    }

    /**
     * Creates a MockHardwareIdentifier with typical test values.
     */
    public static MockHardwareIdentifier withDefaults() {
        return new MockHardwareIdentifier(
                List.of("00:1A:2B:3C:4D:5E", "AA:BB:CC:DD:EE:FF"),
                "ABC123456789");
    }

    @Override
    public List<String> getMacAddresses() {
        return macAddresses;
    }

    @Override
    public String getMotherboardSerial() {
        return motherboardSerial;
    }

    @Override
    public CurrentHardwareInfo getCurrentHardware() {
        return new CurrentHardwareInfo(macAddresses, motherboardSerial);
    }
}
