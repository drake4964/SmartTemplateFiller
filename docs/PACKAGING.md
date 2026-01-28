# Packaging SmartTemplateFiller

This project is configured to produce two types of distributable artifacts:
1. **FatJar**: A single `.jar` file containing all dependencies.
2. **Native Application Image**: A standalone folder containing an `.exe` file and a bundled Java Runtime Environment (JRE).

## Prerequisites
- JDK 17+ installed.
- (Optional) Wix Toolset (3.x) installed if you want to generate MSI installers (currently configured to skip installer generation).

## Validating the Packaging
We have verified that the packaging tasks work successfully.

### 1. Generating the FatJar
Run the following command:
```powershell
./gradlew shadowJar
```
- **Output**: `build/libs/SmartTemplateFiller-1.0.0-all.jar`
- **Usage**: You can run this file anywhere that has Java installed:
  ```powershell
  java -jar build/libs/SmartTemplateFiller-1.0.0-all.jar
  ```

### 2. Generating the Native Executable (Best for Distribution)
Run the following command:
```powershell
./gradlew jpackageImage
```
- **Output**: `build/jpackage/SmartTemplateFiller/`
- **Content**:
    - `SmartTemplateFiller.exe` (The main executable)
    - `runtime/` (Bundled JRE)
    - `app/` (Application code)
- **Usage**:
    - Ensure you copy the entire `SmartTemplateFiller` folder.
    - Double-click `SmartTemplateFiller.exe` to run.
    - The user **does not** need Java installed on their machine.

## Protection Note
This packaging method bundles your code into JAR files inside the `app` folder. While this deters casual users from seeing your source code, it does not encrypt it. A determined developer can still decompile the JARs. This is standard for Java applications.

## Troubleshooting
If the application fails to start:
- Open a terminal in the folder.
- Run `.\SmartTemplateFiller.exe`.
- Check for console errors (we enabled console output in `build.gradle` for debugging).
