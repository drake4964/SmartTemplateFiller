package com.example.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Command-line interface for generating license files.
 * 
 * ADMIN ONLY - This tool contains the secret key and can generate valid
 * licenses.
 * Never distribute this tool to customers!
 * 
 * Supports two modes:
 * 1. Interactive mode: Prompts for all inputs
 * 2. Command-line mode: Accepts --mac, --serial, --expiry, --output flags
 * 
 * Usage:
 * Interactive: java -jar LicenseGenerator.jar
 * Command-line: java -jar LicenseGenerator.jar --mac 00:1A:2B:3C:4D:5E --serial
 * ABC123 --expiry 2027-01-01 --output license.json
 */
public class LicenseGeneratorCLI {

    private static final String VERSION = "1.0.0";
    private static final String SEPARATOR = "================================================================================";

    // MAC address format: XX:XX:XX:XX:XX:XX (case insensitive)
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    // Date format for expiry: yyyy-MM-dd
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BufferedReader reader;
    private final EncryptionUtils encryptionUtils;

    public LicenseGeneratorCLI() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.encryptionUtils = new EncryptionUtils();
    }

    public static void main(String[] args) {
        LicenseGeneratorCLI cli = new LicenseGeneratorCLI();

        if (args.length == 0) {
            cli.runInteractiveMode();
        } else {
            cli.runCommandLineMode(args);
        }
    }

    /**
     * Runs the interactive mode with prompts.
     */
    private void runInteractiveMode() {
        printHeader();

        try {
            // Get MAC addresses
            List<String> macAddresses = promptForMacAddresses();

            // Get motherboard serial
            String motherboardSerial = promptForSerial();

            // Get expiry date
            long expiryTimestamp = promptForExpiry();

            // Get output file
            String outputPath = promptForOutputPath();

            // Generate and save license
            generateAndSaveLicense(macAddresses, motherboardSerial, expiryTimestamp, outputPath);

        } catch (Exception e) {
            printError("License generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs command-line mode with flags.
     */
    private void runCommandLineMode(String[] args) {
        List<String> macAddresses = new ArrayList<>();
        String motherboardSerial = null;
        String expiryDate = null;
        String outputPath = "license.json";

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mac", "-m" -> {
                    if (i + 1 < args.length) {
                        // Support comma-separated MACs or multiple --mac flags
                        String macArg = args[++i];
                        if (macArg.contains(",")) {
                            macAddresses.addAll(Arrays.asList(macArg.split(",")));
                        } else {
                            macAddresses.add(macArg);
                        }
                    }
                }
                case "--serial", "-s" -> {
                    if (i + 1 < args.length) {
                        motherboardSerial = args[++i];
                    }
                }
                case "--expiry", "-e" -> {
                    if (i + 1 < args.length) {
                        expiryDate = args[++i];
                    }
                }
                case "--output", "-o" -> {
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                }
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
                case "--version", "-v" -> {
                    System.out.println("LicenseGenerator v" + VERSION);
                    return;
                }
            }
        }

        // Validate required arguments
        if (macAddresses.isEmpty()) {
            printError("Missing required argument: --mac");
            printHelp();
            System.exit(1);
        }
        if (motherboardSerial == null || motherboardSerial.isEmpty()) {
            printError("Missing required argument: --serial");
            printHelp();
            System.exit(1);
        }
        if (expiryDate == null) {
            printError("Missing required argument: --expiry");
            printHelp();
            System.exit(1);
        }

        // Validate MAC addresses
        for (String mac : macAddresses) {
            if (!isValidMac(mac.trim())) {
                printError("Invalid MAC address format: " + mac + " (expected XX:XX:XX:XX:XX:XX)");
                System.exit(1);
            }
        }

        // Parse expiry date
        long expiryTimestamp;
        try {
            expiryTimestamp = parseExpiryDate(expiryDate);
        } catch (DateTimeParseException e) {
            printError("Invalid expiry date format: " + expiryDate + " (expected yyyy-MM-dd)");
            System.exit(1);
            return;
        }

        // Validate expiry is in the future
        if (expiryTimestamp <= System.currentTimeMillis()) {
            printError("Expiry date must be in the future: " + expiryDate);
            System.exit(1);
        }

        // Generate license
        try {
            generateAndSaveLicense(
                    macAddresses.stream().map(String::trim).map(String::toUpperCase).toList(),
                    motherboardSerial,
                    expiryTimestamp,
                    outputPath);
        } catch (Exception e) {
            printError("License generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prompts the user for MAC addresses.
     */
    private List<String> promptForMacAddresses() throws IOException {
        List<String> macs = new ArrayList<>();

        System.out.println("Enter MAC addresses (one per line, blank line when done):");
        System.out.println("Format: XX:XX:XX:XX:XX:XX");
        System.out.println();

        while (true) {
            System.out.print("MAC [" + (macs.size() + 1) + "]: ");
            String line = reader.readLine();

            if (line == null || line.trim().isEmpty()) {
                if (macs.isEmpty()) {
                    System.out.println("At least one MAC address is required.");
                    continue;
                }
                break;
            }

            String mac = line.trim().toUpperCase();
            if (!isValidMac(mac)) {
                System.out.println("Invalid MAC format. Expected XX:XX:XX:XX:XX:XX");
                continue;
            }

            macs.add(mac);
        }

        System.out.println("Collected " + macs.size() + " MAC address(es)");
        System.out.println();
        return macs;
    }

    /**
     * Prompts the user for motherboard serial.
     */
    private String promptForSerial() throws IOException {
        while (true) {
            System.out.print("Motherboard Serial: ");
            String line = reader.readLine();

            if (line == null || line.trim().isEmpty()) {
                System.out.println("Serial number is required.");
                continue;
            }

            System.out.println();
            return line.trim();
        }
    }

    /**
     * Prompts the user for expiry date.
     */
    private long promptForExpiry() throws IOException {
        while (true) {
            System.out.print("Expiry Date (yyyy-MM-dd): ");
            String line = reader.readLine();

            if (line == null || line.trim().isEmpty()) {
                System.out.println("Expiry date is required.");
                continue;
            }

            try {
                long timestamp = parseExpiryDate(line.trim());
                if (timestamp <= System.currentTimeMillis()) {
                    System.out.println("Expiry date must be in the future.");
                    continue;
                }
                System.out.println();
                return timestamp;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd");
            }
        }
    }

    /**
     * Prompts the user for output file path.
     */
    private String promptForOutputPath() throws IOException {
        System.out.print("Output file [license.json]: ");
        String line = reader.readLine();

        if (line == null || line.trim().isEmpty()) {
            return "license.json";
        }

        System.out.println();
        return line.trim();
    }

    /**
     * Generates and saves the license file.
     */
    private void generateAndSaveLicense(List<String> macAddresses, String motherboardSerial,
            long expiryTimestamp, String outputPath) throws Exception {

        System.out.println(SEPARATOR);
        System.out.println("Generating license...");
        System.out.println();

        // Generate license JSON
        String licenseJson = encryptionUtils.generateLicenseJson(
                macAddresses, motherboardSerial, expiryTimestamp);

        // Save to file
        File outputFile = new File(outputPath);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(licenseJson);
        }

        // Print summary
        printSuccess(macAddresses, motherboardSerial, expiryTimestamp, outputFile.getAbsolutePath());
    }

    /**
     * Prints the application header.
     */
    private void printHeader() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  License Generator v" + VERSION);
        System.out.println("  ADMIN ONLY - Do not distribute to customers");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("This tool generates encrypted license files for SmartTemplateFiller.");
        System.out.println("Use the HardwareInfoExtractor tool to obtain customer hardware info.");
        System.out.println();
    }

    /**
     * Prints help information.
     */
    private void printHelp() {
        System.out.println("Usage: java -jar LicenseGenerator.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --mac, -m <MAC>      MAC address (can be specified multiple times,");
        System.out.println("                       or comma-separated)");
        System.out.println("  --serial, -s <SN>    Motherboard serial number");
        System.out.println("  --expiry, -e <DATE>  Expiry date (yyyy-MM-dd)");
        System.out.println("  --output, -o <FILE>  Output file (default: license.json)");
        System.out.println("  --help, -h           Show this help message");
        System.out.println("  --version, -v        Show version");
        System.out.println();
        System.out.println("Examples:");
        System.out.println(
                "  java -jar LicenseGenerator.jar --mac 00:1A:2B:3C:4D:5E --serial ABC123 --expiry 2027-01-01");
        System.out.println(
                "  java -jar LicenseGenerator.jar -m 00:1A:2B:3C:4D:5E,AA:BB:CC:DD:EE:FF -s XYZ789 -e 2028-06-30 -o customer.json");
        System.out.println();
        System.out.println("Interactive mode:");
        System.out.println("  java -jar LicenseGenerator.jar");
        System.out.println();
    }

    /**
     * Prints success message.
     */
    private void printSuccess(List<String> macAddresses, String motherboardSerial,
            long expiryTimestamp, String outputPath) {

        LocalDate expiryDate = Instant.ofEpochMilli(expiryTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        System.out.println(SEPARATOR);
        System.out.println("  LICENSE GENERATED SUCCESSFULLY");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("License Details:");
        System.out.println("  MAC Addresses: " + String.join(", ", macAddresses));
        System.out.println("  Serial:        " + motherboardSerial);
        System.out.println("  Expires:       " + expiryDate.format(DATE_FORMAT));
        System.out.println("  Output File:   " + outputPath);
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("Send the generated license.json file to the customer.");
        System.out.println("Customer should place it in the SmartTemplateFiller directory.");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    /**
     * Prints an error message.
     */
    private void printError(String message) {
        System.err.println();
        System.err.println("ERROR: " + message);
        System.err.println();
    }

    /**
     * Validates MAC address format.
     */
    private boolean isValidMac(String mac) {
        return MAC_PATTERN.matcher(mac).matches();
    }

    /**
     * Parses an expiry date string to milliseconds timestamp.
     */
    private long parseExpiryDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
        // Set to end of day for expiry
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1;
    }
}
