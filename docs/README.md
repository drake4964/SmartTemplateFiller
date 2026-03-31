# SmartTemplateFiller Documentation

> **Auto-generated documentation** for AI agents and human developers.
> 
> *Last updated: 2026-02-03*

## Quick Links

| Document | Description |
|----------|-------------|
| **[AI Context](AI_CONTEXT.md)** | **Start here** - Single entry point for AI agents |
| [Project Overview](overview/project-overview.md) | Purpose, capabilities, quick start |
| [Architecture](overview/architecture-overview.md) | MVC pattern, component diagram |
| [Source Code Map](overview/SOURCE_CODE_MAP.md) | Class responsibilities and relationships |
| [Technology Stack](overview/technology-stack.md) | Java 17, JavaFX, Apache POI |
| [Packaging](PACKAGING.md) | Build instructions & distribution |

## Documentation Index

### For AI Agents
- **[AI_CONTEXT.md](AI_CONTEXT.md)** - Single-file project summary (start here!)
- [Source Code Map](overview/SOURCE_CODE_MAP.md) - Class-by-class reference

### Overview
- [Project Overview](overview/project-overview.md) - What the application does
- [Architecture Overview](overview/architecture-overview.md) - System design and components
- [Technology Stack](overview/technology-stack.md) - Languages, frameworks, libraries

### Features
- [Feature List](features/feature-list.md) - All features with status
- [File Parsing](features/feature-file-parsing.md) - Multi-strategy TXT/ASC parser
- [Teach Mode](features/feature-teach-mode.md) - Interactive mapping with Excel preview
- [Running Mode](features/feature-running-mode.md) - Automated folder watching
- [Excel Export](features/feature-excel-export.md) - XLSX generation
- [Excel Append Mode](features/excel-append-mode.md) - Append data to existing Excel files
- [License Verification](features/feature-license-verification.md) - Hardware-bound licensing system
- [FileChooserBuilder](features/feature-file-chooser.md) - Fluent file dialogs
- [Feature Dependencies](features/feature-dependencies.md) - Component interactions

### Workflows
- [Workflow Summary](workflows/workflow-summary.md) - User workflow catalog
- [Data to Excel Sequence](workflows/data-to-excel-sequence.md) - Main export flow
- [Mapping Creation Sequence](workflows/mapping-creation-sequence.md) - Teach mode flow

### Deployment & Packaging
- [Packaging Guide](PACKAGING.md) - Building FatJars and JRE bundling
- [License Generation](deployment/license-generation-workflow.md) - Admin workflow for creating licenses

### Diagrams (Mermaid)
- [System Context](diagrams/system-context.mmd) - C4 Level 1
- [Container Diagram](diagrams/container-diagram.mmd) - C4 Level 2
- [Component Diagram](diagrams/component-diagram.mmd) - Class relationships
- [Export Sequence](diagrams/sequence-export-workflow.mmd) - Export flow
- [Teach Mode Sequence](diagrams/sequence-teach-mode.mmd) - Mapping flow
- [ERD](diagrams/erd.mmd) - Data model

### Decisions
- [Architectural Decisions](decisions/architectural-decisions.md) - Key ADRs
- [Assumptions](decisions/assumptions.md) - Analysis assumptions + change log

### Meta
- [Documentation Plan](Documentations/DOC-OUTPUT-PLAN.md) - Strategy for docs
- [Doc Structure](Documentations/DOC-STRUCTURE.md) - Folder layout guide

## For AI Agents

This documentation is structured for programmatic consumption:
- **Start with [AI_CONTEXT.md](AI_CONTEXT.md)** - Contains everything needed to understand the project
- Consistent file naming: `<category>/<topic>.md`
- Mermaid diagrams in `.mmd` files
- Tables for structured data
- Cross-references via relative links

## Generation Summary

| Category | Files |
|----------|-------|
| AI Context | 1 |
| Overview | 4 |
| Features | 9 |
| Workflows | 3 |
| Deployment | 2 |
| Diagrams | 6 |
| Decisions | 2 |
| Meta | 2 |
| Index | 1 |
| **Total** | **30** |

## Change Log

| Date | Changes |
|------|---------|
| Initial | 20 documentation files generated |
| Update 1 | 8 bugs fixed, 4 enhancements implemented |
| Update 2 | Teach Mode UX: drag-drop, Excel preview added |
| Update 3 | Fixed row pattern logic, removed emoji encoding issues |
| Update 4 | Running Mode implementation documented |
| 2026-01-28 | Added AI_CONTEXT.md, SOURCE_CODE_MAP.md, updated component diagram |
| **2026-02-03** | Added License Verification, Excel Append Mode, and Packaging docs |
