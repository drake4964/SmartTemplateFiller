---
description: Convert existing tasks into actionable, dependency-ordered GitHub issues for the feature based on available design artifacts.
---

## Goal

Convert tasks from `tasks.md` into GitHub issues that can be tracked and assigned in the repository.

## Execution Steps

### 1. Setup

Run the following command from repo root:

```powershell
.specify/scripts/powershell/check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks
```

Parse FEATURE_DIR and AVAILABLE_DOCS list. All paths must be absolute.

### 2. Extract Tasks Path

From the executed script output, extract the path to **tasks.md**.

### 3. Verify GitHub Remote

Get the Git remote by running:

```powershell
git config --get remote.origin.url
```

> [!CAUTION]
> ONLY PROCEED TO NEXT STEPS IF THE REMOTE IS A GITHUB URL

### 4. Create GitHub Issues

For each task in the tasks list, create a new GitHub issue in the repository that matches the Git remote.

> [!CAUTION]  
> UNDER NO CIRCUMSTANCES EVER CREATE ISSUES IN REPOSITORIES THAT DO NOT MATCH THE REMOTE URL

## Issue Creation Guidelines

- Use task ID as part of the issue title
- Include task description in the issue body
- Add appropriate labels based on task type (e.g., "enhancement", "setup", "documentation")
- Reference dependencies between issues when applicable
- Include file paths mentioned in the task
