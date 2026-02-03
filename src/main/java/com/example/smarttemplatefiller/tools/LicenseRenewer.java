package com.example.smarttemplatefiller.tools;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Scanner;

public class LicenseRenewer {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      License Renewer Tool");
        System.out.println("==========================================");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter path to existing license.json: ");
            String path = scanner.nextLine().trim();
            path = path.replace("\"", "");

            File licenseFile = new File(path);
            if (!licenseFile.exists()) {
                System.err.println("File not found: " + licenseFile.getAbsolutePath());
                return;
            }

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(licenseFile, Map.class);
            String encrypted = map.get("d");

            // Decrypt
            String payload = LicenseCryptoUtils.decrypt(encrypted);
            String[] parts = payload.split("\\|");
            String deviceId = parts[0];
            long currentExpiry = Long.parseLong(parts[1]);

            System.out.println("Current Expiry: " + Instant.ofEpochMilli(currentExpiry));

            // Extend
            System.out.print("Enter number of DAYS to extend from TODAY (default 365): ");
            String daysStr = scanner.nextLine().trim();
            int days = daysStr.isEmpty() ? 365 : Integer.parseInt(daysStr);

            long newExpiry = Instant.now().plus(days, ChronoUnit.DAYS).toEpochMilli();

            // Generate New
            String newPayload = deviceId + "|" + newExpiry;
            String newEncrypted = LicenseCryptoUtils.encrypt(newPayload);
            String version = "1.0";
            String signature = LicenseCryptoUtils.sign(version, newEncrypted);

            String newJson = String.format("{\n  \"v\": \"%s\",\n  \"d\": \"%s\",\n  \"s\": \"%s\"\n}",
                    version, newEncrypted, signature);

            File outFile = new File("license_renewed.json");
            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(newJson);
            }

            System.out.println("\nSUCCESS! Renewed license saved to: " + outFile.getAbsolutePath());
            System.out.println("New Expiry: " + Instant.ofEpochMilli(newExpiry));

        } catch (Exception e) {
            System.err.println("Error renewing license: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nPress Enter to exit...");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }
    }
}
