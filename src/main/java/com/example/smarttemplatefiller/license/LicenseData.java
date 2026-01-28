package com.example.smarttemplatefiller.license;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the parsed content of an encrypted license key file.
 * Contains version, encrypted data, and HMAC signature.
 */
public class LicenseData {

    @JsonProperty("v")
    private String version;

    @JsonProperty("d")
    private String encryptedData;

    @JsonProperty("s")
    private String signature;

    /**
     * Default constructor for Jackson deserialization.
     */
    public LicenseData() {
    }

    /**
     * Creates a new LicenseData with specified values.
     *
     * @param version       Schema version (e.g., "1.0")
     * @param encryptedData Base64-encoded AES-256 encrypted payload
     * @param signature     Base64-encoded HMAC-SHA256 signature
     */
    public LicenseData(String version, String encryptedData, String signature) {
        this.version = version;
        this.encryptedData = encryptedData;
        this.signature = signature;
    }

    /**
     * Gets the schema version.
     *
     * @return Schema version string (e.g., "1.0")
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the schema version.
     *
     * @param version Schema version string
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the Base64-encoded encrypted data.
     *
     * @return Encrypted payload containing deviceId and expiry timestamp
     */
    public String getEncryptedData() {
        return encryptedData;
    }

    /**
     * Sets the encrypted data.
     *
     * @param encryptedData Base64-encoded AES-256 encrypted payload
     */
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    /**
     * Gets the HMAC-SHA256 signature.
     *
     * @return Base64-encoded signature for tamper detection
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the HMAC signature.
     *
     * @param signature Base64-encoded HMAC-SHA256 signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Checks if all required fields are present and non-empty.
     *
     * @return true if version, encryptedData, and signature are all present
     */
    public boolean isComplete() {
        return version != null && !version.isEmpty()
                && encryptedData != null && !encryptedData.isEmpty()
                && signature != null && !signature.isEmpty();
    }

    @Override
    public String toString() {
        return "LicenseData{version='" + version + "', hasData=" + (encryptedData != null) + ", hasSignature="
                + (signature != null) + "}";
    }
}
