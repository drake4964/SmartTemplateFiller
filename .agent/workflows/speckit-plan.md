---
description: Execute the implementation planning workflow using the plan template to generate design artifacts.
---

## Goal

Execute the implementation planning workflow to generate design artifacts including research.md, data-model.md, API contracts, and quickstart.md.

## Execution Steps

### 1. Setup

Run the following command from repo root:

```powershell
.specify/scripts/powershell/setup-plan.ps1 -Json
```

Parse JSON for FEATURE_SPEC, IMPL_PLAN, SPECS_DIR, BRANCH.

### 2. Load Context

Read:
- FEATURE_SPEC (the specification file)
- `.specify/memory/constitution.md` (project constitution)
- Load IMPL_PLAN template (already copied)

### 3. Execute Plan Workflow

Follow the structure in IMPL_PLAN template to:

1. Fill Technical Context (mark unknowns as "NEEDS CLARIFICATION")
2. Fill Constitution Check section from constitution
3. Evaluate gates (ERROR if violations unjustified)
4. Phase 0: Generate research.md (resolve all NEEDS CLARIFICATION)
5. Phase 1: Generate data-model.md, contracts/, quickstart.md
6. Phase 1: Update agent context by running the agent script
7. Re-evaluate Constitution Check post-design

### 4. Stop and Report

Command ends after Phase 2 planning. Report:
- Branch name
- IMPL_PLAN path
- Generated artifacts

## Phases

### Phase 0: Outline & Research

1. **Extract unknowns from Technical Context**:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:
   ```text
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all NEEDS CLARIFICATION resolved

### Phase 1: Design & Contracts

**Prerequisites:** `research.md` complete

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

3. **Agent context update**:
   Run the following command:
   ```powershell
   .specify/scripts/powershell/update-agent-context.ps1 -AgentType gemini
   ```
   - These scripts detect which AI agent is in use
   - Update the appropriate agent-specific context file
   - Add only new technology from current plan
   - Preserve manual additions between markers

**Output**: data-model.md, /contracts/*, quickstart.md, agent-specific file

## Key Rules

- Use absolute paths
- ERROR on gate failures or unresolved clarifications
