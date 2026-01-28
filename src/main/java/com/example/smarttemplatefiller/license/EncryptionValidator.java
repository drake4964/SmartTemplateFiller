package com.example.smarttemplatefiller.license;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles encryption validation for license files.
 * Implements AES-256 decryption and HMAC-SHA256 signature verification.
 */
public class EncryptionValidator {

    private static final Logger LOGGER = Logger.getLogger(EncryptionValidator.class.getName());

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int IV_LENGTH = 16;

    // 256-bit secret key (32 bytes) - embedded and obfuscated
    // This key MUST match the key used in LicenseGenerator tool
    private static final byte[] SECRET_KEY = getSecretKey();

    /**
     * Gets the embedded secret key.
     * Uses obfuscation to make extraction more difficult.
     */
    private static byte[] getSecretKey() {
        // Obfuscated key construction - harder to extract from bytecode
        // In production, this should be further obfuscated using ProGuard/R8
        byte[] key = new byte[32];

        // Key bytes are split to avoid simple string detection
        // This is a sample key - REPLACE WITH YOUR OWN SECRET KEY
        int[] k1 = { 0x4D, 0x69, 0x74, 0x75, 0x74, 0x6F, 0x79, 0x6F }; // "Mitutoyo"
        int[] k2 = { 0x53, 0x6D, 0x61, 0x72, 0x74, 0x54, 0x65, 0x6D }; // "SmartTem"
        int[] k3 = { 0x70, 0x6C, 0x61, 0x74, 0x65, 0x46, 0x69, 0x6C }; // "plateFil"
        int[] k4 = { 0x6C, 0x65, 0x72, 0x32, 0x30, 0x32, 0x36, 0x21 }; // "ler2026!"

        for (int i = 0; i < 8; i++) {
            key[i] = (byte) k1[i];
            key[i + 8] = (byte) k2[i];
            key[i + 16] = (byte) k3[i];
            key[i + 24] = (byte) k4[i];
        }

        return key;
    }

    /**
     * Verifies the HMAC-SHA256 signature of the license data.
     *
     * @param version       License schema version
     * @param encryptedData Base64-encoded encrypted data
     * @param signature     Base64-encoded expected signature
     * @return true if signature matches, false otherwise
     */
    public boolean verifySignature(String version, String encryptedData, String signature) {
        try {
            // Compute HMAC over version + encryptedData
            String signData = version + encryptedData;

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec hmacKey = new SecretKeySpec(SECRET_KEY, HMAC_ALGORITHM);
            mac.init(hmacKey);

            byte[] computedHmac = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(computedHmac);

            // Use constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Signature verification failed", e);
            return false;
        }
    }

    /**
     * Decrypts the encrypted license data using AES-256-CBC.
     *
     * @param encryptedData Base64-encoded encrypted data (IV prepended)
     * @return Decrypted payload string (deviceId|expiryTimestamp)
     * @throws LicenseDecryptionException if decryption fails
     */
    public String decrypt(String encryptedData) throws LicenseDecryptionException {
        try {
            // Decode Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            if (combined.length < IV_LENGTH + 1) {
                throw new LicenseDecryptionException("Encrypted data too short");
            }

            // Extract IV (first 16 bytes) and encrypted bytes
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            // Decrypt using AES-256-CBC
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec aesKey = new SecretKeySpec(SECRET_KEY, "AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new LicenseDecryptionException("Invalid Base64 encoding", e);
        } catch (Exception e) {
            throw new LicenseDecryptionException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the decrypted payload into deviceId and expiry timestamp.
     *
     * @param payload Decrypted string in format "deviceId|expiryTimestamp"
     * @return PayloadData containing deviceId and expiry
     * @throws LicenseDecryptionException if payload format is invalid
     */
    public PayloadData parsePayload(String payload) throws LicenseDecryptionException {
        if (payload == null || payload.isEmpty()) {
            throw new LicenseDecryptionException("Empty payload");
        }

        String[] parts = payload.split("\\|", 2);
        if (parts.length != 2) {
            throw new LicenseDecryptionException("Invalid payload format");
        }

        try {
            String deviceId = parts[0];
            long expiryTimestamp = Long.parseLong(parts[1]);
            return new PayloadData(deviceId, expiryTimestamp);
        } catch (NumberFormatException e) {
            throw new LicenseDecryptionException("Invalid expiry timestamp", e);
        }
    }

    /**
     * Holds parsed payload data.
     */
    public static class PayloadData {
        private final String deviceId;
        private final long expiryTimestamp;

        public PayloadData(String deviceId, long expiryTimestamp) {
            this.deviceId = deviceId;
            this.expiryTimestamp = expiryTimestamp;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public long getExpiryTimestamp() {
            return expiryTimestamp;
        }
    }

    /**
     * Exception thrown when license decryption fails.
     */
    public static class LicenseDecryptionException extends Exception {
        public LicenseDecryptionException(String message) {
            super(message);
        }

        public LicenseDecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
