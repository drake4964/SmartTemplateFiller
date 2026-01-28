package com.example.smarttemplatefiller.license;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * OSHI-based implementation of HardwareIdentifier.
 * Uses the OSHI library to extract MAC addresses and motherboard serial.
 */
public class OshiHardwareIdentifier implements HardwareIdentifier {

    private static final Logger LOGGER = Logger.getLogger(OshiHardwareIdentifier.class.getName());

    // Lazy-initialized SystemInfo instance
    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hardware;

    /**
     * Creates a new OshiHardwareIdentifier instance.
     * SystemInfo is lazily initialized on first hardware extraction call.
     */
    public OshiHardwareIdentifier() {
        // Lazy initialization to defer OSHI startup cost
    }

    /**
     * Initializes OSHI SystemInfo if not already initialized.
     */
    private synchronized void ensureInitialized() {
        if (systemInfo == null) {
            LOGGER.log(Level.FINE, "Initializing OSHI SystemInfo");
            systemInfo = new SystemInfo();
            hardware = systemInfo.getHardware();
        }
    }

    @Override
    public List<String> getMacAddresses() {
        try {
            ensureInitialized();

            List<NetworkIF> networks = hardware.getNetworkIFs();

            List<String> macs = networks.stream()
                    // Filter out loopback interfaces
                    .filter(net -> !isLoopback(net))
                    // Filter out virtual adapters (common virtual adapter names)
                    .filter(net -> !isVirtualAdapter(net))
                    // Get MAC address
                    .map(NetworkIF::getMacaddr)
                    // Filter out empty MAC addresses
                    .filter(mac -> mac != null && !mac.isEmpty() && !mac.equals("00:00:00:00:00:00"))
                    // Normalize to uppercase
                    .map(String::toUpperCase)
                    .distinct()
                    .collect(Collectors.toList());

            LOGGER.log(Level.FINE, "Extracted {0} MAC addresses", macs.size());
            return macs;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to extract MAC addresses", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getMotherboardSerial() {
        try {
            ensureInitialized();

            String serial = hardware.getComputerSystem()
                    .getBaseboard()
                    .getSerialNumber();

            // Handle "unknown" or similar placeholder values
            if (serial == null || serial.isEmpty() ||
                    serial.equalsIgnoreCase("unknown") ||
                    serial.equalsIgnoreCase("to be filled by o.e.m.") ||
                    serial.equalsIgnoreCase("default string")) {
                LOGGER.log(Level.FINE, "Motherboard serial unavailable, using fallback");
                // Try system serial as fallback
                serial = hardware.getComputerSystem().getSerialNumber();
            }

            if (serial == null || serial.isEmpty() ||
                    serial.equalsIgnoreCase("unknown")) {
                return "";
            }

            LOGGER.log(Level.FINE, "Extracted motherboard serial (length={0})", serial.length());
            return serial;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to extract motherboard serial", e);
            return "";
        }
    }

    @Override
    public CurrentHardwareInfo getCurrentHardware() {
        return new CurrentHardwareInfo(getMacAddresses(), getMotherboardSerial());
    }

    /**
     * Checks if a network interface is a loopback interface.
     */
    private boolean isLoopback(NetworkIF network) {
        String name = network.getName().toLowerCase();
        String displayName = network.getDisplayName().toLowerCase();

        return name.contains("loopback") ||
                displayName.contains("loopback") ||
                name.equals("lo") ||
                network.getMacaddr().equals("00:00:00:00:00:00");
    }

    /**
     * Checks if a network interface is a virtual adapter.
     */
    private boolean isVirtualAdapter(NetworkIF network) {
        String name = network.getName().toLowerCase();
        String displayName = network.getDisplayName().toLowerCase();

        // Common virtual adapter patterns
        return name.contains("virtual") ||
                displayName.contains("virtual") ||
                name.contains("vmware") ||
                displayName.contains("vmware") ||
                name.contains("vbox") ||
                displayName.contains("virtualbox") ||
                name.contains("docker") ||
                displayName.contains("docker") ||
                name.contains("hyper-v") ||
                displayName.contains("hyper-v") ||
                name.contains("vpn") ||
                name.contains("tunnel");
    }
}
