package com.example.tools;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer-facing hardware information extraction utility.
 * 
 * This tool extracts MAC addresses and motherboard serial number from the
 * customer's machine. The customer sends this information to the administrator,
 * who uses it to generate a license file.
 * 
 * IMPORTANT: This utility does NOT contain any secret keys and cannot generate
 * licenses.
 * It is safe to distribute to customers.
 * 
 * Usage: java -jar HardwareInfoExtractor.jar
 */
public class HardwareExtractor {

    private static final String SEPARATOR = "================================================================================";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        HardwareExtractor extractor = new HardwareExtractor();
        extractor.run();
    }

    /**
     * Runs the hardware extraction and displays the results.
     */
    public void run() {
        printHeader();

        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardware = systemInfo.getHardware();

            // Extract MAC addresses
            List<String> macAddresses = extractMacAddresses(hardware);

            // Extract motherboard serial
            String motherboardSerial = extractMotherboardSerial(hardware);

            // Display results in a format suitable for sending to admin
            printResults(macAddresses, motherboardSerial);

        } catch (Exception e) {
            printError("Failed to extract hardware information: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints the application header.
     */
    private void printHeader() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  Hardware Information Extractor v" + VERSION);
        System.out.println("  For License Registration");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Extracting hardware information...");
        System.out.println();
    }

    /**
     * Extracts MAC addresses from network interfaces.
     * Filters out loopback and virtual adapters.
     */
    private List<String> extractMacAddresses(HardwareAbstractionLayer hardware) {
        List<NetworkIF> networks = hardware.getNetworkIFs();

        return networks.stream()
                // Filter out loopback interfaces
                .filter(net -> !isLoopback(net))
                // Filter out virtual adapters
                .filter(net -> !isVirtualAdapter(net))
                // Get MAC address
                .map(NetworkIF::getMacaddr)
                // Filter out empty MAC addresses
                .filter(mac -> mac != null && !mac.isEmpty() && !mac.equals("00:00:00:00:00:00"))
                // Normalize to uppercase
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Extracts the motherboard serial number.
     * Falls back to system serial if motherboard serial is unavailable.
     */
    private String extractMotherboardSerial(HardwareAbstractionLayer hardware) {
        String serial = hardware.getComputerSystem()
                .getBaseboard()
                .getSerialNumber();

        // Handle "unknown" or placeholder values
        if (isInvalidSerial(serial)) {
            // Try system serial as fallback
            serial = hardware.getComputerSystem().getSerialNumber();
        }

        if (isInvalidSerial(serial)) {
            return "[NOT AVAILABLE]";
        }

        return serial;
    }

    /**
     * Checks if a serial number is invalid or a placeholder.
     */
    private boolean isInvalidSerial(String serial) {
        if (serial == null || serial.isEmpty()) {
            return true;
        }

        String lower = serial.toLowerCase();
        return lower.equals("unknown") ||
                lower.equals("to be filled by o.e.m.") ||
                lower.equals("default string") ||
                lower.equals("none") ||
                lower.equals("not specified");
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

    /**
     * Prints the hardware information in a formatted output.
     */
    private void printResults(List<String> macAddresses, String motherboardSerial) {
        System.out.println(SEPARATOR);
        System.out.println("  HARDWARE INFORMATION - SEND THIS TO YOUR ADMINISTRATOR");
        System.out.println(SEPARATOR);
        System.out.println();

        // MAC Addresses
        System.out.println("MAC Addresses:");
        if (macAddresses.isEmpty()) {
            System.out.println("  [No MAC addresses found]");
        } else {
            for (int i = 0; i < macAddresses.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + macAddresses.get(i));
            }
        }
        System.out.println();

        // Motherboard Serial
        System.out.println("Motherboard Serial:");
        System.out.println("  " + motherboardSerial);
        System.out.println();

        System.out.println(SEPARATOR);
        System.out.println();

        // Copy-paste friendly output
        System.out.println("--- Copy-Paste Friendly Format ---");
        System.out.println();
        System.out.println("MAC_ADDRESSES=" + String.join(",", macAddresses));
        System.out.println("MOTHERBOARD_SERIAL=" + motherboardSerial);
        System.out.println();

        System.out.println(SEPARATOR);
        System.out.println("Please copy the information above and send it to your administrator.");
        System.out.println("They will generate a license file for your machine.");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    /**
     * Prints an error message.
     */
    private void printError(String message) {
        System.err.println();
        System.err.println(SEPARATOR);
        System.err.println("  ERROR");
        System.err.println(SEPARATOR);
        System.err.println();
        System.err.println(message);
        System.err.println();
        System.err.println("Please ensure you have the necessary permissions to access hardware information.");
        System.err.println("Try running as Administrator if the problem persists.");
        System.err.println();
    }
}
