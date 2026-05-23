# Packaging & Distribution Guide

This project is configured to produce standalone native Windows executables and distributable artifacts. It automates the generation of lightweight Java Runtime Environments (JREs) and self-contained application images using the Java **`jpackage`** tool.

---

## 1. Quick Build Commands (PowerShell Syntax)

### Generation Option A: Native Executable Directory (Recommended)
Generates a standalone folder containing `SmartTemplateFiller.exe` and a bundled lightweight JRE.
```powershell
./gradlew.bat clean jpackageImage
```
- **Output Location**: `build/jpackage/SmartTemplateFiller/`
- **Distribution**: Zip and distribute this entire folder. Users **do not** need Java installed to run it.

### Generation Option B: Installer Package
Generates a standard Windows setup installation wizard (`.exe` installer).
```powershell
./gradlew.bat clean jpackage
```
- **Prerequisite**: Requires [WiX Toolset (3.x)](https://wixtoolset.org/) installed and available in the system PATH.
- **Output Location**: `build/jpackage/` (e.g. `SmartTemplateFiller-1.0.0.exe`)

### Generation Option C: FatJar (Legacy)
Generates a single executable `.jar` containing all classes and dependencies.
```powershell
./gradlew.bat shadowJar
```
- **Output Location**: `build/libs/SmartTemplateFiller-1.0.0-all.jar`
- **Requirement**: Users must have Java 17+ installed on their local machine. Run with:
  ```powershell
  java -jar build/libs/SmartTemplateFiller-1.0.0-all.jar
  ```

---

## 2. Troubleshooting Windows File Locks

On Windows systems, if you attempt to rebuild the application while an instance of `SmartTemplateFiller.exe` or Java is running, the packaging process will fail with an error:
> `java.io.IOException: Unable to delete directory ... build/jpackage`

To resolve file locks and enforce a successful rebuild, execute this combined command in your PowerShell terminal:
```powershell
Stop-Process -Name SmartTemplateFiller -Force -ErrorAction SilentlyContinue
Stop-Process -Name javaw -Force -ErrorAction SilentlyContinue
./gradlew.bat clean jpackageImage
```

---

## 3. How jpackage Configuration Works

The build uses the **Beryx Runtime Gradle Plugin** (`org.beryx.runtime`) to manage dependencies, construct a lightweight custom JRE (via `jlink`), and invoke `jpackage`.

### Gradle Configuration (`build.gradle`)
The plugin is configured with the following block:
```groovy
runtime {
    // Optimizations to minimize distribution size
    options = ['--strip-debug', '--compress', '2', '--no-header-files', '--no-man-pages']
    
    launcher {
        noConsole = true // Prevents command console popping up behind JavaFX UI
    }
    
    jpackage {
        imageName = 'SmartTemplateFiller'
        skipInstaller = false 
        installerType = 'exe'
        installerOptions = ['--win-per-user-install', '--win-menu', '--win-shortcut']
        
        // Adds secondary launchers for licensing tools
        imageOptions = [
            '--add-launcher', 'HardwareInfoExtractor=hw.properties', 
            '--add-launcher', 'LicenseGenerator=lg.properties', 
            '--add-launcher', 'LicenseRenewer=lr.properties'
        ]
    }
}
```

### Under-The-Hood jpackage CLI Mapping
When Gradle executes `jpackageImage`, it translates the configuration above into the following standard JDK CLI call:
```bash
jpackage --type app-image \
         --name SmartTemplateFiller \
         --dest build/jpackage \
         --input build/install/SmartTemplateFiller-shadow/lib \
         --main-jar SmartTemplateFiller-1.0.0-all.jar \
         --main-class com.example.smarttemplatefiller.Launcher \
         --runtime-image build/jre \
         --add-launcher HardwareInfoExtractor=hw.properties \
         --add-launcher LicenseGenerator=lg.properties \
         --add-launcher LicenseRenewer=lr.properties
```

---

## 4. Standalone Output Directory Layout

The output directory `build/jpackage/SmartTemplateFiller/` contains the following layout:
```text
SmartTemplateFiller/
├── SmartTemplateFiller.exe      # Primary GUI desktop application
├── HardwareInfoExtractor.exe    # License helper utility
├── LicenseGenerator.exe         # Developer licensing generator
├── LicenseRenewer.exe           # Customer license renewal tool
├── app/                         # Shadow jar and runtime application resources
└── runtime/                     # Stripped-down modular Java 17 JRE
```
