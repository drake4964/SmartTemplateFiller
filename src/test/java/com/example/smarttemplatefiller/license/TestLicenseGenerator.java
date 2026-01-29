package com.example.smarttemplatefiller.license;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test utility for generating valid license files for testing.
 * Uses the same cryptographic parameters as the production code.
 */
public class TestLicenseGenerator {

    // Same secret key as EncryptionValidator (must match for tests to work)
    private static final byte[] SECRET_KEY = getSecretKey();

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

    /**
     * Generates a valid encrypted license JSON string.
     *
     * @param macAddresses      List of MAC addresses
     * @param motherboardSerial Motherboard serial number
     * @param expiryTimestamp   Expiry timestamp in milliseconds
     * @return JSON string of the license
     */
    public static String generateLicenseJson(List<String> macAddresses, String motherboardSerial,
            long expiryTimestamp) throws Exception {
        // Step 1: Compute deviceId (same as CurrentHardwareInfo.computeDeviceId())
        String deviceId = computeDeviceId(macAddresses, motherboardSerial);

        // Step 2: Create payload
        String payload = deviceId + "|" + expiryTimestamp;

        // Step 3: Encrypt with AES-256-CBC
        String encryptedData = encrypt(payload);

        // Step 4: Generate HMAC signature
        String version = "1.0";
        String signature = sign(version, encryptedData);

        // Step 5: Build JSON
        return String.format("""
                {
                  "v": "%s",
                  "d": "%s",
                  "s": "%s"
                }
                """, version, encryptedData, signature);
    }

    /**
     * Generates a license that expires in the future (valid license).
     */
    public static String generateValidLicense(List<String> macAddresses, String motherboardSerial)
            throws Exception {
        long futureExpiry = Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli();
        return generateLicenseJson(macAddresses, motherboardSerial, futureExpiry);
    }

    /**
     * Generates a license that has already expired.
     */
    public static String generateExpiredLicense(List<String> macAddresses, String motherboardSerial)
            throws Exception {
        long pastExpiry = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        return generateLicenseJson(macAddresses, motherboardSerial, pastExpiry);
    }

    /**
     * Generates a license with tampered signature (for testing signature
     * verification).
     */
    public static String generateTamperedSignatureLicense(List<String> macAddresses,
            String motherboardSerial) throws Exception {
        String validLicense = generateValidLicense(macAddresses, motherboardSerial);
        // Replace signature with invalid one
        return validLicense.replaceFirst("\"s\": \"[^\"]+\"",
                "\"s\": \"aW52YWxpZFNpZ25hdHVyZQ==\"");
    }

    /**
     * Computes device ID using SHA-256 (same algorithm as CurrentHardwareInfo).
     */
    public static String computeDeviceId(List<String> macAddresses, String motherboardSerial)
            throws Exception {
        String sortedMacs = macAddresses.stream()
                .sorted()
                .collect(Collectors.joining("|"));
        String deviceData = sortedMacs + "|" + motherboardSerial;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(deviceData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Encrypts payload using AES-256-CBC.
     */
    private static String encrypt(String payload) throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(SECRET_KEY, "AES"),
                new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Combine IV + encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Signs version + encryptedData using HMAC-SHA256.
     */
    private static String sign(String version, String encryptedData) throws Exception {
        String signData = version + encryptedData;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET_KEY, "HmacSHA256"));
        byte[] hmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hmac);
    }
}
