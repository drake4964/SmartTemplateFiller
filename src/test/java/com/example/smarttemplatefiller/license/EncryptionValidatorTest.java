package com.example.smarttemplatefiller.license;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EncryptionValidator with AES-256 + HMAC-SHA256 test vectors.
 */
class EncryptionValidatorTest {

    private EncryptionValidator validator;

    // Test secret key (same as in EncryptionValidator)
    private static final byte[] TEST_SECRET_KEY = getTestSecretKey();

    private static byte[] getTestSecretKey() {
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

    @BeforeEach
    void setUp() {
        validator = new EncryptionValidator();
    }

    @Nested
    @DisplayName("HMAC-SHA256 Signature Verification")
    class SignatureVerificationTests {

        @Test
        @DisplayName("Valid signature should verify successfully")
        void testValidSignature() throws Exception {
            String version = "1.0";
            String encryptedData = "dGVzdEVuY3J5cHRlZERhdGE=";

            // Compute expected signature
            String signData = version + encryptedData;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_SECRET_KEY, "HmacSHA256"));
            byte[] hmacBytes = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            String validSignature = Base64.getEncoder().encodeToString(hmacBytes);

            assertTrue(validator.verifySignature(version, encryptedData, validSignature));
        }

        @Test
        @DisplayName("Invalid signature should fail verification")
        void testInvalidSignature() {
            String version = "1.0";
            String encryptedData = "dGVzdEVuY3J5cHRlZERhdGE=";
            String invalidSignature = "aW52YWxpZFNpZ25hdHVyZQ==";

            assertFalse(validator.verifySignature(version, encryptedData, invalidSignature));
        }

        @Test
        @DisplayName("Tampered data should fail verification")
        void testTamperedData() throws Exception {
            String version = "1.0";
            String originalData = "dGVzdEVuY3J5cHRlZERhdGE=";

            // Compute signature for original data
            String signData = version + originalData;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_SECRET_KEY, "HmacSHA256"));
            byte[] hmacBytes = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hmacBytes);

            // Tampered data should fail
            String tamperedData = "dGFtcGVyZWREYXRh";
            assertFalse(validator.verifySignature(version, tamperedData, signature));
        }

        @Test
        @DisplayName("Empty signature should fail verification")
        void testEmptySignature() {
            assertFalse(validator.verifySignature("1.0", "someData", ""));
            assertFalse(validator.verifySignature("1.0", "someData", null));
        }
    }

    @Nested
    @DisplayName("AES-256 Decryption")
    class DecryptionTests {

        @Test
        @DisplayName("Valid encrypted data should decrypt successfully")
        void testValidDecryption() throws Exception {
            String originalPayload = "testDeviceId123|1735689599000";

            // Encrypt the test payload
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(TEST_SECRET_KEY, "AES"),
                    new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(originalPayload.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            String encryptedBase64 = Base64.getEncoder().encodeToString(combined);

            // Decrypt and verify
            String decrypted = validator.decrypt(encryptedBase64);
            assertEquals(originalPayload, decrypted);
        }

        @Test
        @DisplayName("Invalid Base64 should throw exception")
        void testInvalidBase64() {
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.decrypt("not-valid-base64!!!"));
        }

        @Test
        @DisplayName("Too short data should throw exception")
        void testTooShortData() {
            String shortData = Base64.getEncoder().encodeToString(new byte[5]);
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.decrypt(shortData));
        }

        @Test
        @DisplayName("Corrupted encrypted data should throw exception")
        void testCorruptedData() {
            // Create properly sized but corrupted data
            byte[] corrupted = new byte[32];
            new SecureRandom().nextBytes(corrupted);
            String corruptedBase64 = Base64.getEncoder().encodeToString(corrupted);

            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.decrypt(corruptedBase64));
        }
    }

    @Nested
    @DisplayName("Payload Parsing")
    class PayloadParsingTests {

        @Test
        @DisplayName("Valid payload should parse successfully")
        void testValidPayload() throws Exception {
            String payload = "deviceId123|1735689599000";

            EncryptionValidator.PayloadData data = validator.parsePayload(payload);

            assertEquals("deviceId123", data.getDeviceId());
            assertEquals(1735689599000L, data.getExpiryTimestamp());
        }

        @Test
        @DisplayName("Payload with Base64 deviceId should parse successfully")
        void testBase64DeviceId() throws Exception {
            String base64DeviceId = "j8h3k2l9m4n5p7q1r6s0t2u8v3w9x1y4z5a7b2c3d6e8=";
            String payload = base64DeviceId + "|1735689599000";

            EncryptionValidator.PayloadData data = validator.parsePayload(payload);

            assertEquals(base64DeviceId, data.getDeviceId());
            assertEquals(1735689599000L, data.getExpiryTimestamp());
        }

        @Test
        @DisplayName("Empty payload should throw exception")
        void testEmptyPayload() {
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.parsePayload(""));
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.parsePayload(null));
        }

        @Test
        @DisplayName("Invalid format should throw exception")
        void testInvalidFormat() {
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.parsePayload("noSeparator"));
        }

        @Test
        @DisplayName("Invalid timestamp should throw exception")
        void testInvalidTimestamp() {
            assertThrows(EncryptionValidator.LicenseDecryptionException.class,
                    () -> validator.parsePayload("deviceId|notANumber"));
        }
    }
}
