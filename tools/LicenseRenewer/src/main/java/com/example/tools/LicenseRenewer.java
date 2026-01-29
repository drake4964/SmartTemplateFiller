package com.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;

/**
 * License Renewal Utility for extending license expiry dates.
 * 
 * ADMIN ONLY - This tool contains the secret key and can renew licenses.
 * Never distribute this tool to customers!
 * 
 * Workflow:
 * 1. Decrypt existing license to extract deviceId
 * 2. Create new payload with same deviceId but new expiry date
 * 3. Re-encrypt and re-sign with the same secret key
 * 4. Output renewed license file
 * 
 * Usage:
 * java -jar LicenseRenewer.jar --license <existing> --new-expiry <date>
 * --output <file>
 */
public class LicenseRenewer {

    private static final String VERSION = "1.0.0";
    private static final String SEPARATOR = "================================================================================";

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int IV_LENGTH = 16;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Same secret key as LicenseGenerator and main application
    private static final byte[] SECRET_KEY = getSecretKey();

    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;

    public LicenseRenewer() {
        this.objectMapper = new ObjectMapper();
        this.secureRandom = new SecureRandom();
    }

    public static void main(String[] args) {
        LicenseRenewer renewer = new LicenseRenewer();

        if (args.length == 0) {
            renewer.printHelp();
            System.exit(1);
        }

        renewer.run(args);
    }

    /**
     * Runs the license renewal process.
     */
    private void run(String[] args) {
        String licensePath = null;
        String newExpiryStr = null;
        String outputPath = null;

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--license", "-l" -> {
                    if (i + 1 < args.length) {
                        licensePath = args[++i];
                    }
                }
                case "--new-expiry", "-e" -> {
                    if (i + 1 < args.length) {
                        newExpiryStr = args[++i];
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
                    System.out.println("LicenseRenewer v" + VERSION);
                    return;
                }
            }
        }

        // Validate required arguments
        if (licensePath == null) {
            printError("Missing required argument: --license");
            printHelp();
            System.exit(1);
        }
        if (newExpiryStr == null) {
            printError("Missing required argument: --new-expiry");
            printHelp();
            System.exit(1);
        }

        // Set default output path if not specified
        if (outputPath == null) {
            outputPath = "renewed_license.json";
        }

        // Validate license file exists
        File licenseFile = new File(licensePath);
        if (!licenseFile.exists()) {
            printError("License file not found: " + licensePath);
            System.exit(1);
        }

        // Parse new expiry date
        long newExpiryTimestamp;
        try {
            newExpiryTimestamp = parseExpiryDate(newExpiryStr);
        } catch (DateTimeParseException e) {
            printError("Invalid expiry date format: " + newExpiryStr + " (expected yyyy-MM-dd)");
            System.exit(1);
            return;
        }

        // Validate expiry is in the future
        if (newExpiryTimestamp <= System.currentTimeMillis()) {
            printError("New expiry date must be in the future: " + newExpiryStr);
            System.exit(1);
        }

        // Renew the license
        try {
            renewLicense(licenseFile, newExpiryTimestamp, outputPath);
        } catch (Exception e) {
            printError("License renewal failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Renews a license by decrypting, updating expiry, and re-encrypting.
     */
    private void renewLicense(File licenseFile, long newExpiryTimestamp, String outputPath) throws Exception {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  License Renewer v" + VERSION);
        System.out.println("  ADMIN ONLY - Do not distribute to customers");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Processing license file: " + licenseFile.getAbsolutePath());
        System.out.println();

        // Step 1: Read existing license
        JsonNode licenseNode = objectMapper.readTree(licenseFile);
        String version = licenseNode.get("v").asText();
        String encryptedData = licenseNode.get("d").asText();
        String signature = licenseNode.get("s").asText();

        System.out.println("Step 1: Reading existing license...");
        System.out.println("  Version: " + version);

        // Step 2: Verify signature
        System.out.println("Step 2: Verifying signature...");
        if (!verifySignature(version, encryptedData, signature)) {
            throw new Exception("Invalid license signature - license may have been tampered with");
        }
        System.out.println("  Signature: VALID");

        // Step 3: Decrypt to extract deviceId
        System.out.println("Step 3: Decrypting payload...");
        String payload = decrypt(encryptedData);
        String[] parts = payload.split("\\|", 2);
        if (parts.length != 2) {
            throw new Exception("Invalid payload format");
        }
        String deviceId = parts[0];
        long oldExpiry = Long.parseLong(parts[1]);

        LocalDate oldExpiryDate = Instant.ofEpochMilli(oldExpiry)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("  Device ID: " + deviceId.substring(0, 16) + "...");
        System.out.println("  Old Expiry: " + oldExpiryDate.format(DATE_FORMAT));

        // Step 4: Create new payload with same deviceId but new expiry
        System.out.println("Step 4: Creating new payload...");
        String newPayload = deviceId + "|" + newExpiryTimestamp;
        LocalDate newExpiryDate = Instant.ofEpochMilli(newExpiryTimestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("  New Expiry: " + newExpiryDate.format(DATE_FORMAT));

        // Step 5: Encrypt new payload
        System.out.println("Step 5: Encrypting new payload...");
        String newEncryptedData = encrypt(newPayload);

        // Step 6: Generate new signature
        System.out.println("Step 6: Generating new signature...");
        String newSignature = sign(version, newEncryptedData);

        // Step 7: Build and save new license
        System.out.println("Step 7: Saving renewed license...");
        ObjectNode newLicense = objectMapper.createObjectNode();
        newLicense.put("v", version);
        newLicense.put("d", newEncryptedData);
        newLicense.put("s", newSignature);

        File outputFile = new File(outputPath);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(newLicense));
        }

        // Success
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  LICENSE RENEWED SUCCESSFULLY");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Renewal Details:");
        System.out.println("  Original File:   " + licenseFile.getAbsolutePath());
        System.out.println("  Old Expiry:      " + oldExpiryDate.format(DATE_FORMAT));
        System.out.println("  New Expiry:      " + newExpiryDate.format(DATE_FORMAT));
        System.out.println("  Output File:     " + outputFile.getAbsolutePath());
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("Send the renewed license file to the customer.");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    /**
     * Verifies the HMAC-SHA256 signature.
     */
    private boolean verifySignature(String version, String encryptedData, String signature) {
        try {
            String signData = version + encryptedData;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(SECRET_KEY, HMAC_ALGORITHM));
            byte[] computedHmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(computedHmac);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Decrypts the encrypted license data.
     */
    private String decrypt(String encryptedData) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        if (combined.length < IV_LENGTH + 1) {
            throw new Exception("Encrypted data too short");
        }

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(SECRET_KEY, "AES"),
                new IvParameterSpec(iv));

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Encrypts the payload with a new random IV.
     */
    private String encrypt(String payload) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(SECRET_KEY, "AES"),
                new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Signs version + encryptedData using HMAC-SHA256.
     */
    private String sign(String version, String encryptedData) throws Exception {
        String signData = version + encryptedData;
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(SECRET_KEY, HMAC_ALGORITHM));
        byte[] hmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

    /**
     * Parses an expiry date string to milliseconds timestamp.
     */
    private long parseExpiryDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1;
    }

    /**
     * Prints help information.
     */
    private void printHelp() {
        System.out.println("LicenseRenewer v" + VERSION + " - Renew license expiry dates");
        System.out.println();
        System.out.println("Usage: java -jar LicenseRenewer.jar [options]");
        System.out.println();
        System.out.println("Required Options:");
        System.out.println("  --license, -l <FILE>     Path to existing license file");
        System.out.println("  --new-expiry, -e <DATE>  New expiry date (yyyy-MM-dd)");
        System.out.println();
        System.out.println("Optional:");
        System.out.println("  --output, -o <FILE>      Output file (default: renewed_license.json)");
        System.out.println("  --help, -h               Show this help message");
        System.out.println("  --version, -v            Show version");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar LicenseRenewer.jar --license license.json --new-expiry 2028-12-31");
        System.out.println("  java -jar LicenseRenewer.jar -l old.json -e 2029-06-30 -o renewed.json");
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
     * Gets the embedded secret key (same as LicenseGenerator and main application).
     */
    private static byte[] getSecretKey() {
        byte[] key = new byte[32];
        int[] k1 = { 0x4D, 0x69, 0x74, 0x75, 0x74, 0x6F, 0x79, 0x6F };
        int[] k2 = { 0x53, 0x6D, 0x61, 0x72, 0x74, 0x54, 0x65, 0x6D };
        int[] k3 = { 0x70, 0x6C, 0x61, 0x74, 0x65, 0x46, 0x69, 0x6C };
        int[] k4 = { 0x6C, 0x65, 0x72, 0x32, 0x30, 0x32, 0x36, 0x21 };

        for (int i = 0; i < 8; i++) {
            key[i] = (byte) k1[i];
            key[i + 8] = (byte) k2[i];
            key[i + 16] = (byte) k3[i];
            key[i + 24] = (byte) k4[i];
        }
        return key;
    }
}
