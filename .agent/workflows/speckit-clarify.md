---
description: Identify underspecified areas in the current feature spec by asking up to 5 highly targeted clarification questions and encoding answers back into the spec.
---

## Goal

Detect and reduce ambiguity or missing decision points in the active feature specification and record the clarifications directly in the spec file.

**Note**: This clarification workflow should run BEFORE invoking `/speckit-plan`.

## Execution Steps

### 1. Setup

Run the following command from repo root:

```powershell
.specify/scripts/powershell/check-prerequisites.ps1 -Json -PathsOnly
```

Parse minimal JSON payload fields:
- `FEATURE_DIR`
- `FEATURE_SPEC`
- (Optionally capture `IMPL_PLAN`, `TASKS` for future chained flows)

If JSON parsing fails, abort and instruct user to re-run `/speckit-specify`.

### 2. Load and Scan Spec

Load the current spec file. Perform a structured ambiguity & coverage scan using this taxonomy. For each category, mark status: Clear / Partial / Missing.

**Functional Scope & Behavior:**
- Core user goals & success criteria
- Explicit out-of-scope declarations
- User roles / personas differentiation

**Domain & Data Model:**
- Entities, attributes, relationships
- Identity & uniqueness rules
- Lifecycle/state transitions
- Data volume / scale assumptions

**Interaction & UX Flow:**
- Critical user journeys / sequences
- Error/empty/loading states
- Accessibility or localization notes

**Non-Functional Quality Attributes:**
- Performance (latency, throughput targets)
- Scalability (horizontal/vertical, limits)
- Reliability & availability
- Observability (logging, metrics, tracing)
- Security & privacy
- Compliance / regulatory constraints

**Integration & External Dependencies:**
- External services/APIs and failure modes
- Data import/export formats
- Protocol/versioning assumptions

**Edge Cases & Failure Handling:**
- Negative scenarios
- Rate limiting / throttling
- Conflict resolution

**Constraints & Tradeoffs:**
- Technical constraints
- Explicit tradeoffs or rejected alternatives

**Terminology & Consistency:**
- Canonical glossary terms
- Avoided synonyms / deprecated terms

**Completion Signals:**
- Acceptance criteria testability
- Measurable Definition of Done indicators

**Misc / Placeholders:**
- TODO markers / unresolved decisions
- Ambiguous adjectives lacking quantification

### 3. Generate Clarification Questions

Generate a prioritized queue of candidate clarification questions (maximum 5):
- Maximum of 10 total questions across the whole session
- Each question must be answerable with EITHER:
  - A short multiple-choice selection (2–5 options), OR
  - A one-word / short-phrase answer (<=5 words)
- Only include questions whose answers materially impact architecture, data modeling, task decomposition, or test design

### 4. Sequential Questioning Loop (Interactive)

- Present EXACTLY ONE question at a time
- For multiple-choice questions:
  - **Analyze all options** and determine the **most suitable option**
  - Present your **recommended option prominently** at the top with reasoning
  - Format as: `**Recommended:** Option [X] - <reasoning>`
  - Render all options as a Markdown table
  - After the table, add: `You can reply with the option letter, accept the recommendation by saying "yes", or provide your own short answer.`
- For short-answer style:
  - Provide your **suggested answer** based on best practices
  - Format as: `**Suggested:** <your proposed answer> - <brief reasoning>`
- After the user answers:
  - Validate the answer maps to one option or fits the <=5 word constraint
  - Record it in working memory and move to the next question
- Stop asking when:
  - All critical ambiguities resolved
  - User signals completion ("done", "good", "no more")
  - You reach 5 asked questions

### 5. Integration After Each Answer

- Ensure a `## Clarifications` section exists in the spec
- Under it, create a `### Session YYYY-MM-DD` subheading for today
- Append: `- Q: <question> → A: <final answer>`
- Apply the clarification to appropriate sections:
  - Functional ambiguity → Functional Requirements
  - Data shape / entities → Data Model
  - Non-functional constraint → Non-Functional / Quality Attributes
  - Edge case / negative flow → Edge Cases / Error Handling
  - Terminology conflict → Normalize term across spec
- Save the spec file AFTER each integration

### 6. Validation

After each write:
- Clarifications session contains exactly one bullet per accepted answer
- Total asked questions ≤ 5
- No contradictory earlier statement remains
- Markdown structure valid
- Terminology consistency across all updated sections

### 7. Write Updated Spec

Write the updated spec back to `FEATURE_SPEC`.

### 8. Report Completion

- Number of questions asked & answered
- Path to updated spec
- Sections touched
- Coverage summary table
- Suggested next command (e.g., `/speckit-plan`)
