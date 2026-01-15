# Technology Stack

## Core Technologies

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Language** | Java | 17 (LTS) | Primary development language |
| **UI Framework** | JavaFX | 17.0.15 | Desktop GUI framework |
| **Build Tool** | Gradle | 8.5 | Build automation and dependency management |

## Dependencies

### UI Layer
| Library | Version | Purpose |
|---------|---------|---------|
| `javafx-controls` | 17.0.15 | UI controls (Button, TableView, ComboBox) |
| `javafx-fxml` | 17.0.15 | FXML scene loading and controller binding |

### Data Processing
| Library | Version | Purpose |
|---------|---------|---------|
| `jackson-databind` | 2.15.3 | JSON serialization/deserialization for mappings |
| `poi-ooxml` | 5.2.3 | Excel XLSX file generation |
| `poi` | 5.2.3 | Core Apache POI spreadsheet support |

## Build Configuration

```groovy
plugins {
    id 'application'
    id 'java'
    id 'org.openjfx.javafxplugin' version '0.0.13'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

## Runtime Requirements

| Requirement | Details |
|-------------|---------|
| **JDK** | Java 17 or higher with JavaFX modules |
| **OS** | Windows, macOS, or Linux with GUI support |
| **Memory** | Minimum 256MB heap (for large Excel files) |

## File Format Support

| Format | Library Used | Read | Write |
|--------|--------------|------|-------|
| TXT/ASC | Java BufferedReader | ✓ | ✗ |
| JSON | Jackson | ✓ | ✓ |
| XLSX | Apache POI | ✗ | ✓ |

## Technology Rationale

| Choice | Rationale |
|--------|-----------|
| **Java 17** | LTS release with modern features, broad tooling support |
| **JavaFX** | Cross-platform desktop UI with FXML separation of concerns |
| **Apache POI** | Industry-standard Java library for Excel manipulation |
| **Jackson** | Fast, flexible JSON processing with minimal boilerplate |
| **Gradle** | Modern build system with excellent JavaFX plugin support |
