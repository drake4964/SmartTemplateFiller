package com.example.smarttemplatefiller.tools;

import com.example.smarttemplatefiller.license.CurrentHardwareInfo;
import com.example.smarttemplatefiller.license.OshiHardwareIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;

public class HardwareInfoExtractor {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Hardware Info Extractor Tool");
        System.out.println("==========================================");

        try {
            System.out.println("Analyzing hardware...");
            OshiHardwareIdentifier identifier = new OshiHardwareIdentifier();
            CurrentHardwareInfo info = identifier.getCurrentHardware();

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(info);

            System.out.println("\n--- Hardware Information for License ---");
            System.out.println(json);
            System.out.println("----------------------------------------");

            // Save to file
            java.io.File file = new java.io.File("hardware_info.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, info);

            System.out.println("SUCCESS! Hardware info saved to: " + file.getAbsolutePath());
            System.out.println("You can use this file with the LicenseGenerator tool.");

        } catch (Exception e) {
            System.err.println("Error extracting hardware info: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nPress Enter to exit...");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }
    }
}
