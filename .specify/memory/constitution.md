<!--
Sync Impact Report
==================
Version change: 1.2.0 → 1.3.0 (MINOR - Added Principle VII, enhanced Principle I)
Added principles:
  - I. User Experience First (enhanced with accessibility requirements)
  - II. Modular Design
  - III. Configuration-Driven
  - IV. Quality Testing
  - V. Documentation Excellence
  - VI. Reusable Components & Open Source
  - VII. Security & Data Handling (NEW)
Added sections:
  - Technology Standards
  - Development Workflow
Templates updated:
  ✅ plan-template.md (added Constitution Check gates table)
  ✅ spec-template.md (added Constitution Alignment section)
  ✅ tasks-template.md (added Constitution Compliance Tasks in Polish phase)
-->

# SmartTemplateFiller Constitution

## Core Principles

### I. User Experience First

The desktop application MUST prioritize intuitive user interactions above all else.

- All UI operations MUST provide immediate visual feedback
- Error messages MUST be clear, actionable, and non-technical
- The Teach Mode drag-and-drop interface MUST feel responsive and natural
- Smart Preview MUST accurately reflect final export output
- Loading states MUST be visible for any operation exceeding 200ms
- **Accessibility**: All interactive elements MUST be keyboard navigable; color MUST NOT be the only indicator of state

**Rationale**: As a desktop productivity tool, user frustration directly impacts adoption. A smooth UX is non-negotiable.

### VII. Security & Data Handling

User data and configurations MUST be handled securely.

- File paths and user data MUST NOT be logged in production
- Temporary files MUST be cleaned up after export operations
- JSON mappings MUST NOT contain sensitive data (passwords, API keys)
- Input files MUST be validated before parsing (prevent directory traversal, malformed paths)
- Exception stack traces MUST NOT be shown to end users

**Rationale**: Even desktop apps must protect user data and prevent information leakage.

### II. Modular Design

Each major capability MUST be implemented as an independent, testable module.

- **Parsers**: TXT/ASC file parsers (CMM, fixed-column, flat table) MUST be standalone
- **Mappers**: JSON mapping logic MUST be decoupled from UI
- **Exporters**: Excel generation MUST be independent of parsing
- Modules MUST communicate via well-defined interfaces
- No circular dependencies between modules

**Rationale**: Modular design enables independent testing, easier maintenance, and future format extensions.

### III. Configuration-Driven

User-defined mappings MUST be stored as portable JSON configurations.

- All column-to-cell mappings MUST be serializable to JSON
- JSON schemas MUST be versioned using a `"schemaVersion": "1.0"` field for backward compatibility
- Default configurations SHOULD be provided for common file formats
- Configuration changes MUST NOT require code modifications
- Mappings MUST be shareable across users/machines

**Rationale**: Empowers users to create, share, and reuse mappings without developer intervention.

### IV. Quality Testing

Testing MUST cover critical paths and edge cases.

- Parser tests MUST validate all supported file formats
- Mapping logic MUST have unit test coverage ≥80% (measured by line coverage via JaCoCo)
- UI integration tests SHOULD cover primary workflows (Load → Teach → Export)
- Edge cases MUST be tested: empty files, malformed data, special characters
- Regression tests MUST be added for each bug fix

**Rationale**: A data transformation tool requires high confidence that outputs are correct.

### V. Documentation Excellence

Documentation MUST be maintained for both developers and end users.

- README MUST provide quick start instructions
- Each feature MUST have user-facing documentation in `/docs/features/`
- Architecture decisions MUST be recorded in `/docs/decisions/`
- Code comments MUST explain "why", not "what"
- CHANGELOG MUST track user-visible changes

**Rationale**: Good documentation reduces support burden and enables self-service troubleshooting.

### VI. Reusable Components & Open Source

Prefer established open source libraries over custom implementations. Build reusable components.

- **Open Source First**: Use proven libraries (e.g., Lombok, Apache Commons, Guava) before writing custom code
- Components MUST be designed for reuse across features
- Avoid reinventing the wheel - check Maven Central/GitHub before implementing
- Library choices MUST be documented in `build.gradle` with version pinning
- Evaluate libraries for: active maintenance, license compatibility (Apache 2.0, MIT preferred), community adoption
- Custom utilities SHOULD be extracted to shared packages when used in 2+ places

**Recommended Libraries for SmartTemplateFiller**:

| Purpose | Library | Why |
|---------|---------|-----|
| Boilerplate reduction | Lombok | `@Data`, `@Builder`, `@Slf4j` |
| Logging | SLF4J + Logback | Industry standard, already with Lombok |
| Validation | Hibernate Validator | Bean validation annotations |
| String utilities | Apache Commons Lang | `StringUtils`, `ObjectUtils` |
| Collections | Guava | Immutable collections, caching |
| Date/Time | java.time (built-in) | Modern API, no external dep |

**Rationale**: Leveraging battle-tested libraries reduces bugs, improves maintainability, and accelerates development.

## Technology Standards

The following technology stack is established for this project:

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Core runtime (LTS) |
| JavaFX | 17.0.15 | Desktop UI framework |
| Gradle | 8.5+ | Build automation |
| Apache POI | 5.2.3 | Excel file generation |
| Jackson | 2.15.3 | JSON serialization |

- Technology upgrades MUST be justified and tested
- Breaking changes MUST follow semantic versioning
- Dependencies MUST be reviewed for security vulnerabilities quarterly (owner: lead developer)

## Development Workflow

### Code Review Requirements

- All changes MUST be reviewed before merge
- Reviews MUST verify compliance with Core Principles
- UI changes MUST include a screenshot or screen recording

### Quality Gates

- Build MUST pass before merge
- Tests MUST pass before merge
- No new compiler warnings allowed

### Release Process

- CHANGELOG MUST be updated for each release
- Version numbers follow MAJOR.MINOR.PATCH format
- Releases MUST be tagged in git

## Governance

This constitution is the authoritative source for project standards. All development decisions MUST align with these principles.

- Constitution amendments require documented rationale and approval
- Exceptions to principles MUST be justified in code comments or PRs
- Complexity MUST be justified; default to simplicity (YAGNI)
- See `/docs/` for runtime development guidance

**Version**: 1.3.0 | **Ratified**: 2026-01-16 | **Last Amended**: 2026-01-16
