---
description: Generate a custom checklist for the current feature based on user requirements - "Unit Tests for English" that validate requirements quality.
---

## Checklist Purpose: "Unit Tests for English"

**CRITICAL CONCEPT**: Checklists are **UNIT TESTS FOR REQUIREMENTS WRITING** - they validate the quality, clarity, and completeness of requirements in a given domain.

**NOT for verification/testing**:
- ❌ NOT "Verify the button clicks correctly"
- ❌ NOT "Test error handling works"
- ❌ NOT checking if code/implementation matches the spec

**FOR requirements quality validation**:
- ✅ "Are visual hierarchy requirements defined for all card types?" (completeness)
- ✅ "Is 'prominent display' quantified with specific sizing/positioning?" (clarity)
- ✅ "Are hover state requirements consistent across all interactive elements?" (consistency)

## Execution Steps

### 1. Setup

Run the following command from repo root:

```powershell
.specify/scripts/powershell/check-prerequisites.ps1 -Json
```

Parse JSON for FEATURE_DIR and AVAILABLE_DOCS list. All file paths must be absolute.

### 2. Clarify Intent (Dynamic)

Derive up to THREE initial contextual clarifying questions:
- Generated from the user's phrasing + extracted signals from spec/plan/tasks
- Only ask about information that materially changes checklist content
- Be skipped individually if already unambiguous

**Generation algorithm:**
1. Extract signals: feature domain keywords, risk indicators, stakeholder hints
2. Cluster signals into candidate focus areas (max 4)
3. Identify probable audience & timing
4. Detect missing dimensions: scope breadth, depth/rigor, risk emphasis
5. Formulate questions from these archetypes:
   - Scope refinement
   - Risk prioritization  
   - Depth calibration
   - Audience framing
   - Boundary exclusion

### 3. Understand User Request

Combine user arguments + clarifying answers:
- Derive checklist theme (e.g., security, review, deploy, ux)
- Consolidate explicit must-have items mentioned by user
- Map focus selections to category scaffolding

### 4. Load Feature Context

Read from FEATURE_DIR:
- spec.md: Feature requirements and scope
- plan.md (if exists): Technical details, dependencies
- tasks.md (if exists): Implementation tasks

### 5. Generate Checklist - "Unit Tests for Requirements"

- Create `FEATURE_DIR/checklists/` directory if it doesn't exist
- Generate unique checklist filename using short, descriptive name (e.g., `ux.md`, `api.md`, `security.md`)
- Number items sequentially starting from CHK001

**CORE PRINCIPLE - Test the Requirements, Not the Implementation:**

Every checklist item MUST evaluate the REQUIREMENTS THEMSELVES for:
- **Completeness**: Are all necessary requirements present?
- **Clarity**: Are requirements unambiguous and specific?
- **Consistency**: Do requirements align with each other?
- **Measurability**: Can requirements be objectively verified?
- **Coverage**: Are all scenarios/edge cases addressed?

**HOW TO WRITE CHECKLIST ITEMS:**

❌ **WRONG** (Testing implementation):
- "Verify landing page displays 3 episode cards"
- "Test hover states work on desktop"

✅ **CORRECT** (Testing requirements quality):
- "Are the exact number and layout of featured episodes specified?" [Completeness]
- "Is 'prominent display' quantified with specific sizing/positioning?" [Clarity]
- "Are hover state requirements consistent across all interactive elements?" [Consistency]

**ITEM STRUCTURE:**
- Question format asking about requirement quality
- Focus on what's WRITTEN (or not written) in the spec/plan
- Include quality dimension in brackets [Completeness/Clarity/Consistency/etc.]
- Reference spec section `[Spec §X.Y]` when checking existing requirements
- Use `[Gap]` marker when checking for missing requirements

### 6. Structure Reference

Use `.specify/templates/checklist-template.md` for title, meta section, category headings, and ID formatting.

### 7. Report

Output full path to created checklist, item count, and summarize:
- Focus areas selected
- Depth level
- Actor/timing
- Any explicit user-specified must-have items incorporated
