# Feature: FileChooserBuilder

## Summary

Fluent builder for JavaFX file chooser dialogs with automatic last-directory memory.

## Entry Point

- **Class**: `FileChooserBuilder`
- **Constructor**: `new FileChooserBuilder(Stage stage)`

## Features

### Fluent API

```java
File file = new FileChooserBuilder(stage)
    .withTitle("Select File")
    .withExtension("Text Files", "*.txt", "*.asc")
    .open();
```

### Last Directory Memory (ENH-003)

The builder automatically remembers the last used directory:
- Stored in a static field `lastDirectory`
- Persists across all instances within the session
- Automatically sets initial directory on new dialogs
- Updates after each file selection

## API Reference

| Method | Description |
|--------|-------------|
| `withTitle(String title)` | Set dialog title |
| `withExtension(String label, String... extensions)` | Add file extension filter |
| `withInitialDirectory(File directory)` | Override initial directory |
| `open()` | Show open dialog, return selected file |
| `save(String defaultFileName)` | Show save dialog with default filename |
| `resetLastDirectory()` | Static method to clear directory memory |

## Usage in MainController

```java
// Load TXT file
File selectedFile = new FileChooserBuilder(stage)
    .withTitle("Open Text File")
    .withExtension("Text Files", "*.txt", "*.asc")
    .open();

// Save Excel file
File outputFile = new FileChooserBuilder(stage)
    .withTitle("Save Excel File")
    .withExtension("Excel Files", "*.xlsx")
    .save("output.xlsx");
```

## Benefits

| Benefit | Description |
|---------|-------------|
| **Cleaner code** | Fluent API reduces boilerplate |
| **Consistency** | All dialogs configured the same way |
| **User convenience** | Dialogs open in last used directory |
| **Testability** | `resetLastDirectory()` for test isolation |
