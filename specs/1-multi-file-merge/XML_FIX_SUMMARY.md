# XML Syntax Errors Fixed - Running Mode FXML

**Date**: 2026-01-21  
**File**: `src/main/resources/fxml/running_mode.fxml`  
**Status**: ✅ **FIXED - BUILD SUCCESSFUL**

## Errors Found and Fixed

### Error 1: Unescaped Ampersand Characters (Line 70-71)

**Error Message**:
```
javax.xml.stream.XMLStreamException: ParseError at [row,col]:[71,39]
Message: The entity name must immediately follow the '&' in the entity reference.
```

**Root Cause**: Ampersand (`&`) is a special character in XML that must be escaped as `&amp;` or avoided in text/attributes.

**Location**: Line 70-71
```xml
❌ Before:
<!-- Section 3: Status & Activity Log -->
<TitledPane text="Status & Activity Log" collapsible="false" VBox.vgrow="ALWAYS">

✅ After:
<!-- Section 3: Status and Activity Log -->
<TitledPane text="Status and Activity Log" collapsible="false" VBox.vgrow="ALWAYS">
```

**Fix**: Replaced `&` with `and` in both the comment and the text attribute.

### Error 2: Missing Closing Bracket (Line 52)

**Root Cause**: User accidentally removed the closing `>` from a self-closing tag.

**Location**: Line 52
```xml
❌ Before:
<Insets top="5" right="5" bottom="5" left="5"/

✅ After:
<Insets top="5" right="5" bottom="5" left="5"/>
```

**Fix**: Restored the missing `>` character to properly close the self-closing tag.

## Build Validation

```powershell
.\gradlew build
# BUILD SUCCESSFUL in 6s
# 8 actionable tasks: 6 executed, 2 up-to-date
```

## Next Steps

The Running Mode window should now load correctly. Please:

1. **Restart the application** (stop current `./gradlew run` and restart)
2. Click **"Running Mode"** button
3. The multi-folder watching UI should now appear without errors

## Common XML Special Characters

For future reference, these characters must be escaped in XML:

| Character | Escape Sequence | Alternative |
|-----------|----------------|-------------|
| `&` | `&amp;` | Use "and" in text |
| `<` | `&lt;` | - |
| `>` | `&gt;` | - |
| `"` | `&quot;` | Use `'` for attributes |
| `'` | `&apos;` | Use `"` for attributes |

## Files Modified

- `src/main/resources/fxml/running_mode.fxml` (Lines 52, 70-71)

---

**Status**: ✅ All XML syntax errors resolved
