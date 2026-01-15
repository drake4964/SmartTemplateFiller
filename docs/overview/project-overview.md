# Project Overview

## Purpose

**SmartTemplateFiller** is a desktop application that transforms raw measurement data from text files into structured Excel reports using customizable mapping rules.

## Target Users

- Quality Control Engineers
- CMM (Coordinate Measuring Machine) Operators  
- Manufacturing Data Analysts

## Key Capabilities

| Capability | Description |
|------------|-------------|
| **Multi-Format Parsing** | Reads TXT and ASC files with automatic format detection |
| **Teach Mode** | Interactive UI to visually map data columns to Excel cells |
| **Flexible Mapping** | Supports vertical/horizontal layouts, row patterns (odd/even/all), manual row selection |
| **Excel Export** | Generates XLSX files with mapped data using Apache POI |
| **JSON Persistence** | Saves/loads mapping configurations as reusable JSON files |

## Input/Output Summary

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│  Input Files    │     │  SmartTemplateFiller │     │    Outputs      │
├─────────────────┤     ├──────────────────────┤     ├─────────────────┤
│ • TXT data      │────▶│ • Parse file         │────▶│ • XLSX reports  │
│ • ASC data      │     │ • Apply mappings     │     │ • JSON mappings │
│ • JSON mappings │     │ • Generate Excel     │     │                 │
└─────────────────┘     └──────────────────────┘     └─────────────────┘
```

## Supported File Formats

### Input
- **TXT/ASC**: Fixed-column tables, multi-line grouped blocks (CMM output), flat delimited tables
- **JSON**: Mapping configuration files

### Output
- **XLSX**: Microsoft Excel Open XML format
- **JSON**: Mapping definitions (for reuse)

## Quick Start

1. **Load Data**: Click "Load TXT File" and select a measurement file
2. **Configure Mapping**: Enter "Teach Mode" to map columns to Excel cells
3. **Export**: Click "Export to Excel" to generate the output file
