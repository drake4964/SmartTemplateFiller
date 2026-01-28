package com.example.smarttemplatefiller.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LicenseData JSON deserialization and validation.
 */
class LicenseDataTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Deserialize valid license JSON with short field names")
    void testDeserializeValidJson() throws Exception {
        String json = """
                {
                    "v": "1.0",
                    "d": "dGVzdEVuY3J5cHRlZERhdGE=",
                    "s": "dGVzdFNpZ25hdHVyZQ=="
                }
                """;

        LicenseData license = mapper.readValue(json, LicenseData.class);

        assertEquals("1.0", license.getVersion());
        assertEquals("dGVzdEVuY3J5cHRlZERhdGE=", license.getEncryptedData());
        assertEquals("dGVzdFNpZ25hdHVyZQ==", license.getSignature());
        assertTrue(license.isComplete());
    }

    @Test
    @DisplayName("Deserialize JSON with missing fields returns incomplete")
    void testDeserializeMissingFields() throws Exception {
        String json = """
                {
                    "v": "1.0"
                }
                """;

        LicenseData license = mapper.readValue(json, LicenseData.class);

        assertEquals("1.0", license.getVersion());
        assertNull(license.getEncryptedData());
        assertNull(license.getSignature());
        assertFalse(license.isComplete());
    }

    @Test
    @DisplayName("Deserialize empty JSON creates empty LicenseData")
    void testDeserializeEmptyJson() throws Exception {
        String json = "{}";

        LicenseData license = mapper.readValue(json, LicenseData.class);

        assertNull(license.getVersion());
        assertNull(license.getEncryptedData());
        assertNull(license.getSignature());
        assertFalse(license.isComplete());
    }

    @Test
    @DisplayName("isComplete returns true only when all fields present")
    void testIsComplete() {
        LicenseData complete = new LicenseData("1.0", "data", "sig");
        assertTrue(complete.isComplete());

        LicenseData missingVersion = new LicenseData(null, "data", "sig");
        assertFalse(missingVersion.isComplete());

        LicenseData emptyVersion = new LicenseData("", "data", "sig");
        assertFalse(emptyVersion.isComplete());

        LicenseData missingData = new LicenseData("1.0", null, "sig");
        assertFalse(missingData.isComplete());

        LicenseData missingSig = new LicenseData("1.0", "data", null);
        assertFalse(missingSig.isComplete());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void testSetters() {
        LicenseData license = new LicenseData();

        license.setVersion("2.0");
        license.setEncryptedData("newData");
        license.setSignature("newSig");

        assertEquals("2.0", license.getVersion());
        assertEquals("newData", license.getEncryptedData());
        assertEquals("newSig", license.getSignature());
    }

    @Test
    @DisplayName("toString does not expose sensitive data")
    void testToStringSecure() {
        LicenseData license = new LicenseData("1.0", "sensitiveData", "sensitiveSignature");

        String str = license.toString();

        // Should indicate presence but not expose actual values
        assertTrue(str.contains("version='1.0'"));
        assertTrue(str.contains("hasData=true"));
        assertTrue(str.contains("hasSignature=true"));
        assertFalse(str.contains("sensitiveData"));
        assertFalse(str.contains("sensitiveSignature"));
    }
}
