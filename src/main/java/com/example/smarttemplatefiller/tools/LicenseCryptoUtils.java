package com.example.smarttemplatefiller.tools;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cryptographic utilities for license tools.
 * Contains the secret key and methods for encryption/decryption/signing.
 * Duplicates logic from EncryptionValidator/TestLicenseGenerator to keep tools
 * standalone.
 */
public class LicenseCryptoUtils {

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

    public static String encrypt(String payload) throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY, "AES"), new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedData) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        if (combined.length < 17)
            throw new IllegalArgumentException("Invalid encrypted data");

        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, 16, combined.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY, "AES"), new IvParameterSpec(iv));

        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }

    public static String sign(String version, String encryptedData) throws Exception {
        String signData = version + encryptedData;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET_KEY, "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(signData.getBytes(StandardCharsets.UTF_8)));
    }
}
