# Generating Windows EXE and Installers with jpackage

This guide explains how to package SmartTemplateFiller as a native Windows application. There are two ways to do this depending on whether you want a portable folder or a full installer.

## 1. Comparison of Packaging Options

| Feature | App Image (Portable) | Installer (.exe / .msi) |
|---------|-----------------------|-------------------------|
| **Task** | `./gradlew jpackageImage` | `./gradlew jpackage` |
| **Prerequisites** | None (JDK only) | **WiX Toolset** required |
| **Output** | A folder containing an `.exe` | A single `.exe` or `.msi` setup file |
| **Installation** | Copy folder and run | Run installer to install to Program Files |

---

## 2. Option 1: Generate App Image (No WiX Required)

This is the easiest way to generate a runnable `.exe`. It creates a folder containing your application, a private Java runtime, and the launcher.

### Steps:
1. Ensure `build.gradle` has `skipInstaller = true` in the `jpackage` block.
2. Run the following command:
   ```powershell
   ./gradlew jpackageImage
   ```
3. Locate the output in:
   `build/jpackage/SmartTemplateFiller/`

The folder will contain `SmartTemplateFiller.exe` along with all necessary runtime files.

---

## 3. Option 2: Generate Installer (Requires WiX)

This creates a setup file that users can run to install the application on their system.

### Prerequisites:
- **WiX Toolset v3.11 or later**: 
  - Download from: [wixtoolset.org](https://wixtoolset.org/releases/)
  - Add the WiX `bin` folder to your system **PATH**.
  - Verify with: `candle --version`

### Steps:
1. Set `skipInstaller = false` and `installerType = 'exe'` (or `'msi'`) in `build.gradle`.
2. Run the following command:
   ```powershell
   ./gradlew jpackage
   ```
3. Locate the installer in:
   `build/jpackage/`

---

## 4. Configuration Details (`build.gradle`)

The `runtime` plugin is configured to bundle JavaFX and add secondary launchers for utility tools:

```gradle
jpackage {
    imageName = 'SmartTemplateFiller'
    skipInstaller = true // Change to false for installers
    installerType = 'exe'
    imageOptions = [
        '--add-launcher', 'HardwareInfoExtractor=hw.properties', 
        '--add-launcher', 'LicenseGenerator=lg.properties', 
        '--add-launcher', 'LicenseRenewer=lr.properties'
    ]
}
```

## 5. Troubleshooting

- **"Can't find WiX tools"**: If running the `jpackage` task fails with this error, it means WiX is not installed or not in your PATH. Use `jpackageImage` instead if you don't need a formal installer.
- **Missing DLLs**: If the `.exe` fails to start, ensure you are running it from within its generated folder (the `runtime` and `app` subfolders must be present).
