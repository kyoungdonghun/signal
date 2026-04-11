---
name: manage-hooks
description: This skill should be used when managing Git hooks for automation. Use when adding pre-commit/pre-push/commit-msg hooks, listing installed hooks, enabling/disabling hooks, or setting up project-wide hook templates. Triggers on requests like "add a pre-commit hook", "install documentation validation hook", "show current hooks", or "disable the test hook".
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Write, Bash, Glob
model: sonnet
---

# Manage Hooks

## Overview

Manage Git hooks for project automation. Install standardized hook templates, enable/disable hooks, and customize hook behavior for consistent pre-commit validation, testing, and documentation integrity checks.

## Core Capabilities

### 1. List Installed Hooks

Show currently installed Git hooks and their status.

**Usage:**
```
/manage-hooks list
```

**Procedure:**
1. Check `.git/hooks/` directory for executable hook files
2. For each standard hook type (pre-commit, pre-push, commit-msg, etc.):
   - Check if file exists and is executable
   - Identify hook source (built-in template, custom, disabled)
3. Display table:
   ```
   Hook Type      | Status   | Source
   ---------------|----------|------------------
   pre-commit     | Active   | docs-validation
   pre-push       | Disabled | (renamed to .sample)
   commit-msg     | None     | -
   ```

### 2. Install Hook from Template

Install a pre-defined hook template.

**Usage:**
```
/manage-hooks install <template-name>
```

**Available Templates:**
- `docs-validation` - Pre-commit hook for documentation integrity
- `test-runner` - Pre-commit or pre-push hook for running tests
- `commit-msg-format` - Commit-msg hook for commit message validation

**Procedure:**
1. Verify template exists in `assets/` directory
2. Check if hook file already exists in `.git/hooks/`
3. If exists, prompt user for overwrite confirmation
4. Copy template to `.git/hooks/<hook-type>` (e.g., `.git/hooks/pre-commit`)
5. Make executable: `chmod +x .git/hooks/<hook-type>`
6. Verify installation by checking file exists and is executable

**Example:**
```
/manage-hooks install docs-validation

→ Installing docs-validation hook (pre-commit)...
→ ✅ Hook installed: .git/hooks/pre-commit
→ ✅ Made executable
→
→ This hook will run on every commit when docs/ files change:
→   1. Index count verification (/sync-docs-index --check)
→   2. Document path validation
→   3. Link validation (/validate-docs)
```

### 3. Disable Hook

Temporarily disable a hook without deleting it.

**Usage:**
```
/manage-hooks disable <hook-type>
```

**Procedure:**
1. Check if `.git/hooks/<hook-type>` exists
2. Rename to `.git/hooks/<hook-type>.disabled`
3. Confirm hook is no longer executable

**Example:**
```
/manage-hooks disable pre-commit

→ Disabling pre-commit hook...
→ ✅ Renamed: .git/hooks/pre-commit → .git/hooks/pre-commit.disabled
→
→ To re-enable: /manage-hooks enable pre-commit
```

### 4. Enable Hook

Re-enable a previously disabled hook.

**Usage:**
```
/manage-hooks enable <hook-type>
```

**Procedure:**
1. Check if `.git/hooks/<hook-type>.disabled` exists
2. Rename to `.git/hooks/<hook-type>`
3. Ensure executable: `chmod +x .git/hooks/<hook-type>`

### 5. Remove Hook

Permanently remove a hook.

**Usage:**
```
/manage-hooks remove <hook-type>
```

**Procedure:**
1. Check if hook exists (active or disabled)
2. Prompt for confirmation (destructive operation)
3. Delete file
4. Confirm removal

**Safety:** Always confirm before deletion.

### 6. Install Custom Hook

Install a hook from a custom script path.

**Usage:**
```
/manage-hooks install-custom <hook-type> <script-path>
```

**Procedure:**
1. Verify script file exists at given path
2. Verify script has shebang line (#!/bin/bash or #!/usr/bin/env python3)
3. Copy to `.git/hooks/<hook-type>`
4. Make executable
5. Test syntax (for bash: `bash -n`, for python: `python3 -m py_compile`)

**Example:**
```
/manage-hooks install-custom pre-push ./scripts/run-tests.sh

→ Installing custom pre-push hook...
→ ✅ Copied: ./scripts/run-tests.sh → .git/hooks/pre-push
→ ✅ Made executable
→ ✅ Syntax validated
```

## Hook Templates

This skill provides standardized hook templates in the `assets/` directory:

### `docs-validation` (pre-commit)

**Purpose:** Validate documentation integrity before committing docs/ changes

**Checks:**
1. Index count sync (`/sync-docs-index --check`)
2. Document path validation (all referenced paths exist)
3. Link validation (`/validate-docs`)

**When it runs:** Only when `docs/` files are staged for commit

**Template file:** `assets/pre-commit-docs-validation.sh`

### `test-runner` (pre-commit or pre-push)

**Purpose:** Run project tests before committing or pushing

**Options:**
- Pre-commit: Fast unit tests only
- Pre-push: Full test suite including integration tests

**Template files:**
- `assets/pre-commit-test-runner.sh`
- `assets/pre-push-test-runner.sh`

### `commit-msg-format` (commit-msg)

**Purpose:** Enforce commit message conventions

**Validates:**
- Message format (e.g., "type(scope): subject")
- Subject line length (<72 characters)
- Body wrapping (optional)

**Template file:** `assets/commit-msg-format.sh`

## Bypassing Hooks

When hooks fail inappropriately or emergency commits are needed:

```bash
# Skip hooks for a single commit
git commit --no-verify -m "Emergency fix"

# Skip hooks for a single push
git push --no-verify
```

**Warning:** Use sparingly. Bypassing hooks may introduce inconsistencies.

## Hook Development Guidelines

When creating custom hooks:

1. **Exit codes:** Return 0 for success, non-zero to block commit/push
2. **Fast execution:** Keep hooks under 5 seconds when possible
3. **Clear messages:** Explain why the hook failed and how to fix it
4. **Selective execution:** Only run when relevant files changed (use `git diff --cached --name-only`)
5. **Provide fixes:** Suggest commands to resolve issues (e.g., `/sync-docs-index --fix`)

## Resources

### scripts/

**`scripts/hook_manager.py`** - Core hook management logic

Python utility for programmatic hook management. Can be used directly:

```bash
# List hooks
python3 .claude/skills/manage-hooks/scripts/hook_manager.py list

# Install hook
python3 .claude/skills/manage-hooks/scripts/hook_manager.py install docs-validation

# Disable hook
python3 .claude/skills/manage-hooks/scripts/hook_manager.py disable pre-commit
```

### references/

**`references/git-hooks-guide.md`** - Comprehensive Git hooks reference

Detailed documentation on:
- All Git hook types and when they trigger
- Common use cases and examples
- Hook execution order
- Environment variables available in hooks
- Best practices and anti-patterns

Load this reference when designing complex custom hooks or understanding hook behavior.

### assets/

**Hook templates** - Ready-to-use hook scripts

- `assets/pre-commit-docs-validation.sh` - Documentation validation
- `assets/pre-commit-test-runner.sh` - Fast unit tests
- `assets/pre-push-test-runner.sh` - Full test suite
- `assets/commit-msg-format.sh` - Commit message validation

These templates are copied to `.git/hooks/` when installed.
