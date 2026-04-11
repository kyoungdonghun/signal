---
name: lint
description: This skill should be used when verifying code and document quality before commits or pull requests. It runs linters for Markdown, JSON, and Python files in the project.
disable-model-invocation: false
user-invocable: true
allowed-tools: Bash, Read, Glob
model: sonnet
---

# Lint

## Purpose

Verify code and document quality by running linters for Markdown, JSON, and Python files.

## When to Use

- Before creating commits or pull requests
- When checking document formatting consistency
- When verifying JSON configuration files
- When ensuring Python code style compliance

## How to Use

### Quick Start

Run the bundled lint script to check all files:

```bash
python3 .claude/skills/lint/scripts/lint_all.py
```

### What Gets Checked

1. **Markdown** - Formatting, heading structure, link format
   - Tool: `markdownlint-cli` (requires npm installation if not available)
   - Config: `.markdownlint.json` (if present)

2. **JSON** - Syntax errors, formatting
   - Tool: `jq` (usually pre-installed)
   - Checks: Valid syntax, proper formatting

3. **Python** - Code style, syntax errors
   - Tool: `ruff` (requires pip installation if not available)
   - Checks: PEP 8 compliance, syntax errors

### Handling Results

**When errors are found:**
- Display error list with file paths and line numbers
- Provide fix suggestions
- Indicate which errors can be auto-fixed

**When all checks pass:**
- Display success summary with file counts

### Example Output

```
[LINT RESULTS]
✓ Markdown: 23 files checked, 0 errors
✗ JSON: 2 files checked, 1 error
  - .claude/config/workspace.json:15 - trailing comma
✓ Python: 3 files checked, 0 errors

[SUMMARY]
Status: FAILED (1 error)
Fix: Remove trailing comma in workspace.json:15
```

## Installation Requirements

If linting tools are not installed, the script will provide installation commands:

```bash
# Markdown
npm install -g markdownlint-cli

# Python
pip install ruff
```
