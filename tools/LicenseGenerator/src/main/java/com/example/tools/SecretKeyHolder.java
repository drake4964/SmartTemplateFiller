package com.example.tools;

/**
 * Holds the embedded 256-bit secret key for license encryption.
 * 
 * SECURITY WARNING: This key is embedded in the binary. In production:
 * 1. Use ProGuard/R8 to obfuscate this class
 * 2. Never distribute this tool to customers
 * 3. Store this tool securely - it can generate valid licenses
 * 
 * This key MUST match the key in the main application's EncryptionValidator.
 */
public final class SecretKeyHolder {

    // Private constructor to prevent instantiation
    private SecretKeyHolder() {
    }

    /**
     * Gets the 256-bit (32 byte) secret key.
     * Key is constructed in a way that makes simple string detection more
     * difficult.
     * 
     * @return The secret key bytes
     */
    public static byte[] getSecretKey() {
        byte[] key = new byte[32];

        // Key bytes are split to avoid simple string detection in bytecode
        // This matches the key in SmartTemplateFiller's EncryptionValidator
        // Key value: "MitutoyoSmartTemplateFilller2026!"
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
     * Gets the key as a displayable string (for debugging only - masked).
     * Never log or display the actual key!
     * 
     * @return Masked key representation
     */
    public static String getMaskedKeyInfo() {
        return "AES-256 Key: ********[" + getSecretKey().length + " bytes]";
    }
}
