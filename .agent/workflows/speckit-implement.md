---
description: Execute the implementation plan by processing and executing all tasks defined in tasks.md
---

## Goal

Execute the implementation plan by processing all tasks from `tasks.md` in the correct order, respecting dependencies and parallelization markers.

## Execution Steps

### 1. Setup

Run the following command from repo root:

```powershell
.specify/scripts/powershell/check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks
```

Parse FEATURE_DIR and AVAILABLE_DOCS list. All paths must be absolute.

### 2. Check Checklists Status

If `FEATURE_DIR/checklists/` exists:

1. Scan all checklist files in the checklists/ directory
2. For each checklist, count:
   - Total items: All lines matching `- [ ]` or `- [X]` or `- [x]`
   - Completed items: Lines matching `- [X]` or `- [x]`
   - Incomplete items: Lines matching `- [ ]`

3. Create a status table:
   ```text
   | Checklist | Total | Completed | Incomplete | Status |
   |-----------|-------|-----------|------------|--------|
   | ux.md     | 12    | 12        | 0          | ✓ PASS |
   | test.md   | 8     | 5         | 3          | ✗ FAIL |
   ```

4. **If any checklist is incomplete**:
   - Display the table with incomplete item counts
   - **STOP** and ask: "Some checklists are incomplete. Do you want to proceed with implementation anyway? (yes/no)"
   - Wait for user response before continuing

5. **If all checklists are complete**:
   - Display the table showing all checklists passed
   - Automatically proceed to step 3

### 3. Load and Analyze Implementation Context

- **REQUIRED**: Read tasks.md for the complete task list and execution plan
- **REQUIRED**: Read plan.md for tech stack, architecture, and file structure
- **IF EXISTS**: Read data-model.md for entities and relationships
- **IF EXISTS**: Read contracts/ for API specifications and test requirements
- **IF EXISTS**: Read research.md for technical decisions and constraints
- **IF EXISTS**: Read quickstart.md for integration scenarios

### 4. Project Setup Verification

Create/verify ignore files based on actual project setup:

**Detection & Creation Logic:**
- Check if git repo (create/verify .gitignore if so):
  ```powershell
  git rev-parse --git-dir 2>$null
  ```
- Check if Dockerfile* exists or Docker in plan.md → create/verify .dockerignore
- Check if .eslintrc* exists → create/verify .eslintignore
- Check if .prettierrc* exists → create/verify .prettierignore
- Check if package.json exists → create/verify .npmignore (if publishing)

**Common Patterns by Technology:**
- **Node.js/TypeScript**: `node_modules/`, `dist/`, `build/`, `*.log`, `.env*`
- **Python**: `__pycache__/`, `*.pyc`, `.venv/`, `venv/`, `dist/`
- **Java**: `target/`, `*.class`, `*.jar`, `.gradle/`, `build/`
- **C#/.NET**: `bin/`, `obj/`, `*.user`, `*.suo`, `packages/`
- **Universal**: `.DS_Store`, `Thumbs.db`, `*.tmp`, `.vscode/`, `.idea/`

### 5. Parse tasks.md Structure

Extract:
- **Task phases**: Setup, Tests, Core, Integration, Polish
- **Task dependencies**: Sequential vs parallel execution rules
- **Task details**: ID, description, file paths, parallel markers [P]
- **Execution flow**: Order and dependency requirements

### 6. Execute Implementation

- **Phase-by-phase execution**: Complete each phase before moving to the next
- **Respect dependencies**: Run sequential tasks in order, parallel tasks [P] can run together
- **Follow TDD approach**: Execute test tasks before their corresponding implementation tasks
- **File-based coordination**: Tasks affecting the same files must run sequentially
- **Validation checkpoints**: Verify each phase completion before proceeding

### 7. Implementation Execution Rules

- **Setup first**: Initialize project structure, dependencies, configuration
- **Tests before code**: If you need to write tests for contracts, entities, and integration scenarios
- **Core development**: Implement models, services, CLI commands, endpoints
- **Integration work**: Database connections, middleware, logging, external services
- **Polish and validation**: Unit tests, performance optimization, documentation

### 8. Progress Tracking and Error Handling

- Report progress after each completed task
- Halt execution if any non-parallel task fails
- For parallel tasks [P], continue with successful tasks, report failed ones
- Provide clear error messages with context for debugging
- **IMPORTANT**: For completed tasks, mark the task off as [X] in the tasks file

### 9. Completion Validation

- Verify all required tasks are completed
- Check that implemented features match the original specification
- Validate that tests pass and coverage meets requirements
- Confirm the implementation follows the technical plan
- Report final status with summary of completed work

**Note**: This workflow assumes a complete task breakdown exists in tasks.md. If tasks are incomplete or missing, suggest running `/speckit-tasks` first.
