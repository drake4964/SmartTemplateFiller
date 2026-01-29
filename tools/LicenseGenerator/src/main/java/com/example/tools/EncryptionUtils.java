package com.example.tools;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encryption utilities for license generation.
 * Implements AES-256-CBC encryption and HMAC-SHA256 signing.
 * 
 * This class implements the SAME cryptographic logic as the main application's
 * EncryptionValidator, but for ENCRYPTION rather than decryption.
 */
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int IV_LENGTH = 16;

    private final byte[] secretKey;
    private final SecureRandom secureRandom;

    /**
     * Creates an EncryptionUtils instance with the embedded secret key.
     */
    public EncryptionUtils() {
        this.secretKey = SecretKeyHolder.getSecretKey();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Computes the device ID from MAC addresses and motherboard serial.
     * Uses SHA-256 hash of sorted MAC addresses joined with "|" plus motherboard
     * serial.
     * 
     * @param macAddresses      List of MAC addresses
     * @param motherboardSerial Motherboard serial number
     * @return Base64-encoded SHA-256 hash as deviceId
     * @throws Exception if hashing fails
     */
    public String computeDeviceId(List<String> macAddresses, String motherboardSerial) throws Exception {
        // Sort and join MAC addresses
        String sortedMacs = macAddresses.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.joining("|"));

        // Combine with motherboard serial
        String deviceData = sortedMacs + "|" + motherboardSerial;

        // Hash with SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(deviceData.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generates the payload string from device ID and expiry timestamp.
     * 
     * @param deviceId        The computed device ID
     * @param expiryTimestamp Expiry time in milliseconds since epoch
     * @return Payload string in format "deviceId|expiryTimestamp"
     */
    public String generatePayload(String deviceId, long expiryTimestamp) {
        return deviceId + "|" + expiryTimestamp;
    }

    /**
     * Encrypts the payload using AES-256-CBC with a random IV.
     * The IV is prepended to the encrypted data.
     * 
     * @param payload The plaintext payload to encrypt
     * @return Base64-encoded encrypted data (IV + ciphertext)
     * @throws Exception if encryption fails
     */
    public String encrypt(String payload) throws Exception {
        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Create cipher
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec aesKey = new SecretKeySpec(secretKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));

        // Encrypt
        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Combine IV + encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Signs the version and encrypted data using HMAC-SHA256.
     * 
     * @param version       License schema version (e.g., "1.0")
     * @param encryptedData Base64-encoded encrypted data
     * @return Base64-encoded HMAC signature
     * @throws Exception if signing fails
     */
    public String sign(String version, String encryptedData) throws Exception {
        String signData = version + encryptedData;

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec hmacKey = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
        mac.init(hmacKey);

        byte[] hmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

    /**
     * Generates a complete license JSON object.
     * 
     * @param macAddresses      List of MAC addresses
     * @param motherboardSerial Motherboard serial number
     * @param expiryTimestamp   Expiry time in milliseconds since epoch
     * @return JSON string containing the encrypted license
     * @throws Exception if any cryptographic operation fails
     */
    public String generateLicenseJson(List<String> macAddresses, String motherboardSerial,
            long expiryTimestamp) throws Exception {

        // Step 1: Compute device ID
        String deviceId = computeDeviceId(macAddresses, motherboardSerial);

        // Step 2: Generate payload
        String payload = generatePayload(deviceId, expiryTimestamp);

        // Step 3: Encrypt payload
        String encryptedData = encrypt(payload);

        // Step 4: Generate signature
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
}
