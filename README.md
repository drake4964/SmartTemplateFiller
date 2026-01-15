# SmartTemplateFiller

A JavaFX desktop application that parses TXT/ASC measurement data files and maps them to Excel templates using configurable JSON mappings.

## Features

- **Multi-format file parsing** - Supports CMM output, fixed-column tables, and generic flat tables
- **Teach Mode** - Interactive UI with drag-and-drop mapping configuration
- **Smart Preview** - See actual data values before export
- **Excel Export** - Generate XLSX files with flexible column mappings

---

## Prerequisites (Windows)

Before running this application, install the following:

### 1. Java 17 (Required)

Download and install Java 17 JDK:
- **Option A**: [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Option B**: [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17) (recommended, free)

After installation, verify:
```powershell
java -version
```
Should show `openjdk version "17.x.x"` or similar.

### 2. Set JAVA_HOME (Required)

Set the `JAVA_HOME` environment variable:

1. Open **System Properties** → **Advanced** → **Environment Variables**
2. Under **System variables**, click **New**
3. Variable name: `JAVA_HOME`
4. Variable value: `C:\Java\Java17` (or your JDK installation path)
5. Click **OK**

Also add to **Path**:
```
%JAVA_HOME%\bin
```

### 3. Gradle (Optional)

Gradle will auto-download via the wrapper, but if you want system Gradle:
- Download from [gradle.org](https://gradle.org/releases/)
- Or install via [Chocolatey](https://chocolatey.org/): `choco install gradle`

---

## How to Run

### Option 1: Using Gradle (Recommended)

Open PowerShell or Command Prompt in the project folder and run:

```powershell
gradle run
```

This will:
1. Download dependencies automatically
2. Configure JavaFX module path
3. Launch the application

### Option 2: Using Gradle Wrapper

If you don't have Gradle installed:

```powershell
.\gradlew.bat run
```

> **Note**: If `gradlew.bat` doesn't exist, you can generate it:
> ```powershell
> gradle wrapper
> ```

---

## Quick Start Guide

1. **Load a data file**: Click "Load TXT" and select your `.txt` or `.asc` file
2. **Create mappings**: Click "Teach Mode" to configure column-to-cell mappings
   - Select source column
   - Enter target Excel cell (e.g., `A2`, `B5`)
   - Choose direction and row pattern
   - Click "Add Mapping"
   - Drag to reorder if needed
3. **Save mapping**: Click "Save Mapping" to export as JSON for reuse
4. **Export to Excel**: Click "Export to Excel" and select:
   - Source TXT file
   - Mapping JSON file
   - Output XLSX location

---

## Project Structure

```
SmartTemplateFiller/
├── src/main/java/          # Java source code
├── src/main/resources/     # FXML layouts, config files
├── docs/                   # Documentation (features, architecture, diagrams)
├── build.gradle            # Gradle build configuration
└── README.md               # This file
```

---

## Troubleshooting

### Error: "Unable to initialize main class" / "NoClassDefFoundError: Stage"

This means JavaFX is not on the classpath. Always use `gradle run` instead of running `java` directly.

### Error: "JAVA_HOME is not set"

Set the JAVA_HOME environment variable as described above.

### Build warnings about "non-project file"

These are VS Code/IDE warnings, not actual errors. The code compiles and runs correctly.

---

## Documentation

Full documentation available in the `/docs` folder:
- [Project Overview](docs/overview/project-overview.md)
- [Feature List](docs/features/feature-list.md)
- [Teach Mode Guide](docs/features/feature-teach-mode.md)
- [Architecture Decisions](docs/decisions/architectural-decisions.md)

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| JavaFX | 17.0.15 |
| Gradle | 8.5+ |
| Apache POI | 5.2.3 |
| Jackson | 2.15.3 |
