---
name: validate-docs
description: This skill should be used when validating documentation consistency and integrity. It checks internal links and current core project document presence.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash
model: sonnet
---

# Validate Docs

## Purpose

Validate documentation consistency and integrity by checking links and required core project documents.

## When to Use

- After modifying documentation files
- Before commits or pull requests that include doc changes
- When checking for broken internal links
- When ensuring current core project documents are properly referenced

## How to Use

### Quick Start

Run the bundled validation script:

```bash
python3 .claude/skills/validate-docs/scripts/validate_docs.py
```

### What Gets Checked

1. **Internal Links**
   - Validate file path references in markdown files
   - Check relative and absolute path accuracy
   - Detect broken links to non-existent files

2. **Core Project Documents**
   - Verify existence of current baseline documents:
     - `AGENTS.md`
     - `CLAUDE.md`
     - `.claude/config/PIPELINE.md`
     - `docs/meta-layer-charter.md`

### Handling Results

**Error Types:**
- **Error**: Broken links, missing core documents
- **Warning**: Read failures or optional-document issues

**Example Output:**

```
[VALIDATION RESULTS]

✓ Core Documents: All required files exist

✗ Internal Links: 2 broken links found
  - docs/guides/workflow.md:15 → docs/missing.md (file not found)
  - README.md:23 → docs/old-path.md (file not found)

[SUMMARY]
Status: FAILED (2 errors)
Action Required: Fix broken links
```

## Exit Codes

- `0`: All validations passed
- `1`: Errors found (broken links, missing docs)
- `2`: Warnings only
