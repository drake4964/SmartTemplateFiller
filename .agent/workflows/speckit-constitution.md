---
description: Create or update the project constitution from interactive or provided principle inputs, ensuring all dependent templates stay in sync.
---

## Goal

Update the project constitution at `.specify/memory/constitution.md`. This file is a TEMPLATE containing placeholder tokens in square brackets (e.g. `[PROJECT_NAME]`, `[PRINCIPLE_1_NAME]`). Your job is to:
1. Collect/derive concrete values
2. Fill the template precisely
3. Propagate any amendments across dependent artifacts

## Execution Steps

### 1. Load Existing Constitution

Load the existing constitution template at `.specify/memory/constitution.md`.
- Identify every placeholder token of the form `[ALL_CAPS_IDENTIFIER]`

**IMPORTANT**: The user might require less or more principles than the ones used in the template. If a number is specified, respect that.

### 2. Collect/Derive Values for Placeholders

- If user input supplies a value, use it
- Otherwise infer from existing repo context (README, docs, prior constitution versions)
- For governance dates:
  - `RATIFICATION_DATE` is the original adoption date
  - `LAST_AMENDED_DATE` is today if changes are made
- `CONSTITUTION_VERSION` must increment according to semantic versioning:
  - MAJOR: Backward incompatible governance/principle removals or redefinitions
  - MINOR: New principle/section added or materially expanded guidance
  - PATCH: Clarifications, wording, typo fixes, non-semantic refinements

### 3. Draft the Updated Constitution Content

- Replace every placeholder with concrete text (no bracketed tokens left)
- Preserve heading hierarchy
- Ensure each Principle section: succinct name line, paragraph capturing non-negotiable rules, explicit rationale
- Ensure Governance section lists amendment procedure, versioning policy, and compliance review expectations

### 4. Consistency Propagation Checklist

Read and validate alignment with:
- `.specify/templates/plan-template.md` - ensure "Constitution Check" aligns with updated principles
- `.specify/templates/spec-template.md` - update if constitution adds/removes mandatory sections
- `.specify/templates/tasks-template.md` - ensure task categorization reflects new principle-driven task types
- Command files in `.specify/templates/commands/*.md`
- Runtime guidance docs (`README.md`, `docs/quickstart.md`)

### 5. Produce Sync Impact Report

Prepend as an HTML comment at top of the constitution file:
- Version change: old → new
- List of modified principles (old title → new title if renamed)
- Added sections
- Removed sections
- Templates requiring updates (✅ updated / ⚠ pending)
- Follow-up TODOs if any placeholders intentionally deferred

### 6. Validation Before Final Output

- No remaining unexplained bracket tokens
- Version line matches report
- Dates ISO format YYYY-MM-DD
- Principles are declarative, testable, and free of vague language

### 7. Write the Completed Constitution

Write back to `.specify/memory/constitution.md` (overwrite).

### 8. Output Final Summary

- New version and bump rationale
- Any files flagged for manual follow-up
- Suggested commit message (e.g., `docs: amend constitution to vX.Y.Z`)

## Formatting & Style Requirements

- Use Markdown headings exactly as in the template
- Wrap long rationale lines to keep readability (<100 chars ideally)
- Keep a single blank line between sections
- Avoid trailing whitespace
