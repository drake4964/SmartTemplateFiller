package com.example.smarttemplatefiller.tools;

import com.example.smarttemplatefiller.license.CurrentHardwareInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class LicenseGenerator {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      License Generator Tool");
        System.out.println("==========================================");

        try (Scanner scanner = new Scanner(System.in)) {
            String macsInput = "";
            String serialInput = "";

            System.out.println("1. Enter Hardware Info manually");
            System.out.println("2. Read from JSON file (from HardwareInfoExtractor)");
            System.out.print("Select option (1/2): ");
            String option = scanner.nextLine().trim();

            if ("2".equals(option)) {
                System.out.print("Enter path to JSON file: ");
                String jsonPath = scanner.nextLine().trim();
                // Remove quotes if user dragged file
                jsonPath = jsonPath.replace("\"", "");

                File jsonFile = new File(jsonPath);
                if (!jsonFile.exists()) {
                    System.err.println("File not found: " + jsonFile.getAbsolutePath());
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(jsonFile, Map.class);

                List<String> macs = (List<String>) map.get("macAddresses");
                macsInput = String.join(",", macs);
                serialInput = (String) map.get("motherboardSerial");

                System.out.println("Loaded MACs: " + macsInput);
                System.out.println("Loaded Serial: " + serialInput);

            } else {
                System.out.print("Enter MAC Addresses (comma separated): ");
                macsInput = scanner.nextLine().trim();

                System.out.print("Enter Motherboard Serial: ");
                serialInput = scanner.nextLine().trim();
            }

            // Expiry Option
            System.out.println("\nSelect Expiry Option:");
            System.out.println("1. Duration (in days)");
            System.out.println("2. Explicit Date (yyyyMMdd)");
            System.out.print("Select option (1/2): ");
            String expiryOption = scanner.nextLine().trim();

            long expiryTime;

            if ("2".equals(expiryOption)) {
                System.out.print("Enter expiry date (yyyyMMdd): ");
                String dateStr = scanner.nextLine().trim();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    // Set expiry to end of that day (23:59:59)
                    expiryTime = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                } catch (Exception e) {
                    System.err.println("Invalid date format. Using default 365 days.");
                    expiryTime = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();
                }
            } else {
                System.out.print("Enter validity in days (default 365): ");
                String daysStr = scanner.nextLine().trim();
                int days = daysStr.isEmpty() ? 365 : Integer.parseInt(daysStr);
                expiryTime = Instant.now().plus(days, ChronoUnit.DAYS).toEpochMilli();
            }

            // Generate
            List<String> macList = Arrays.stream(macsInput.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            CurrentHardwareInfo info = new CurrentHardwareInfo(macList, serialInput);
            String deviceId = info.computeDeviceId();

            // Create License content
            String payload = deviceId + "|" + expiryTime;
            String encrypted = LicenseCryptoUtils.encrypt(payload);
            String version = "1.0";
            String signature = LicenseCryptoUtils.sign(version, encrypted);

            // Build JSON manually to avoid complicated POJOs
            String licenseJson = String.format("{\n  \"v\": \"%s\",\n  \"d\": \"%s\",\n  \"s\": \"%s\"\n}",
                    version, encrypted, signature);

            // Write
            File outFile = new File("license.json");
            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(licenseJson);
            }

            System.out.println("\nSUCCESS! License generated at: " + outFile.getAbsolutePath());
            System.out.println("Expires: " + Instant.ofEpochMilli(expiryTime));

        } catch (Exception e) {
            System.err.println("Error generating license: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nPress Enter to exit...");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }
    }
}
