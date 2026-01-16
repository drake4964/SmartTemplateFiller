---
description: Create or update the feature specification from a natural language feature description.
---

## Goal

Given a natural language feature description, create a complete feature specification following the spec template.

## Execution Steps

### 1. Generate a Concise Short Name (2-4 words)

- Analyze the feature description and extract the most meaningful keywords
- Create a 2-4 word short name that captures the essence of the feature
- Use action-noun format when possible (e.g., "add-user-auth", "fix-payment-bug")
- Preserve technical terms and acronyms (OAuth2, API, JWT, etc.)

**Examples:**
- "I want to add user authentication" → "user-auth"
- "Implement OAuth2 integration for the API" → "oauth2-api-integration"
- "Create a dashboard for analytics" → "analytics-dashboard"

### 2. Check for Existing Branches

a. First, fetch all remote branches:
   ```powershell
   git fetch --all --prune
   ```

b. Find the highest feature number across all sources for the short-name:
   - Remote branches: `git ls-remote --heads origin | grep -E 'refs/heads/[0-9]+-<short-name>$'`
   - Local branches: `git branch | grep -E '^[* ]*[0-9]+-<short-name>$'`
   - Specs directories: Check for directories matching `specs/[0-9]+-<short-name>`

c. Determine the next available number (N+1)

d. Run the script with calculated number and short-name:
   ```powershell
   .specify/scripts/powershell/create-new-feature.ps1 -Json "<feature description>" -Number 5 -ShortName "user-auth"
   ```

### 3. Load Template

Load `.specify/templates/spec-template.md` to understand required sections.

### 4. Execute Specification Flow

1. Parse user description from Input
   - If empty: ERROR "No feature description provided"
2. Extract key concepts from description
   - Identify: actors, actions, data, constraints
3. For unclear aspects:
   - Make informed guesses based on context and industry standards
   - Only mark with [NEEDS CLARIFICATION: specific question] if:
     - The choice significantly impacts feature scope or user experience
     - Multiple reasonable interpretations exist
     - No reasonable default exists
   - **LIMIT: Maximum 3 [NEEDS CLARIFICATION] markers total**
   - Prioritize: scope > security/privacy > user experience > technical details
4. Fill User Scenarios & Testing section
5. Generate Functional Requirements (each must be testable)
6. Define Success Criteria (measurable, technology-agnostic outcomes)
7. Identify Key Entities (if data involved)
8. Return: SUCCESS (spec ready for planning)

### 5. Write Specification

Write to SPEC_FILE using the template structure, replacing placeholders with concrete details.

### 6. Specification Quality Validation

a. **Create Spec Quality Checklist** at `FEATURE_DIR/checklists/requirements.md`:

```markdown
# Specification Quality Checklist: [FEATURE NAME]

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: [DATE]
**Feature**: [Link to spec.md]

## Content Quality

- [ ] No implementation details (languages, frameworks, APIs)
- [ ] Focused on user value and business needs
- [ ] Written for non-technical stakeholders
- [ ] All mandatory sections completed

## Requirement Completeness

- [ ] No [NEEDS CLARIFICATION] markers remain
- [ ] Requirements are testable and unambiguous
- [ ] Success criteria are measurable
- [ ] Success criteria are technology-agnostic
- [ ] All acceptance scenarios are defined
- [ ] Edge cases are identified
- [ ] Scope is clearly bounded
- [ ] Dependencies and assumptions identified

## Feature Readiness

- [ ] All functional requirements have clear acceptance criteria
- [ ] User scenarios cover primary flows
- [ ] Feature meets measurable outcomes defined in Success Criteria
- [ ] No implementation details leak into specification
```

b. **Run Validation Check**: Review the spec against each checklist item

c. **Handle Validation Results**:
   - If all items pass: Mark checklist complete
   - If items fail: Update the spec to address issues (max 3 iterations)
   - If [NEEDS CLARIFICATION] markers remain: Present questions with options table

d. **Update Checklist**: After each validation iteration, update pass/fail status

### 7. Report Completion

- Branch name
- Spec file path
- Checklist results
- Readiness for next phase (`/speckit-clarify` or `/speckit-plan`)

## General Guidelines

- Focus on **WHAT** users need and **WHY**
- Avoid HOW to implement (no tech stack, APIs, code structure)
- Written for business stakeholders, not developers
- DO NOT create any checklists embedded in the spec

### Section Requirements

- **Mandatory sections**: Must be completed for every feature
- **Optional sections**: Include only when relevant
- Remove sections that don't apply (don't leave as "N/A")

### Success Criteria Guidelines

Success criteria must be:
1. **Measurable**: Include specific metrics (time, percentage, count, rate)
2. **Technology-agnostic**: No mention of frameworks, languages, databases
3. **User-focused**: Describe outcomes from user/business perspective
4. **Verifiable**: Can be tested without knowing implementation details

**Good examples:**
- "Users can complete checkout in under 3 minutes"
- "System supports 10,000 concurrent users"
- "95% of searches return results in under 1 second"

**Bad examples (implementation-focused):**
- "API response time is under 200ms"
- "Database can handle 1000 TPS"
- "React components render efficiently"
