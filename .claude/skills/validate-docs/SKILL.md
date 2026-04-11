---
name: validate-docs
description: This skill should be used when validating documentation consistency and integrity. It checks internal links, Tier 0 document references, and traceability ID validity.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash
model: sonnet
---

# Validate Docs

## Purpose

Validate documentation consistency and integrity by checking links, required documents, and traceability IDs.

## When to Use

- After modifying documentation files
- Before commits or pull requests that include doc changes
- When checking for broken internal links
- When verifying traceability ID compliance
- When ensuring Tier 0 documents are properly referenced

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

2. **Tier 0 Document References**
   - Verify existence of required core documents:
     - `docs/standards/core-principles.md`
     - `docs/standards/documentation-standards.md`
     - `docs/standards/development-standards.md`
     - `docs/standards/prompt-caching-strategy.md`

3. **Traceability IDs**
   - Validate ID format compliance:
     - `REQ-YYYYMMDD-<PRJ>-###` (Requirements, e.g., `REQ-20260211-ATS-001`)
     - `WI-YYYYMMDD-<PRJ>-###` (Work Items, e.g., `WI-20260211-ATS-001`)
     - `STD-###` (Standards)
   - Detect duplicate IDs across documents

4. **Document Index**
   - Verify `docs/index.md` link validity
   - Detect orphaned documents (not listed in index)

### Handling Results

**Error Types:**
- **Error**: Broken links, missing Tier 0 docs, duplicate IDs
- **Warning**: Orphaned documents, non-standard formatting

**Example Output:**

```
[VALIDATION RESULTS]

✓ Tier 0 Documents: All required files exist

✗ Internal Links: 2 broken links found
  - docs/guides/workflow.md:15 → docs/missing.md (file not found)
  - README.md:23 → docs/old-path.md (file not found)

✓ Traceability IDs: No duplicates, format valid

⚠ Document Index: 1 orphan document
  - docs/experimental/draft.md (not listed in docs/index.md)

[SUMMARY]
Status: FAILED (2 errors, 1 warning)
Action Required: Fix broken links
```

## Exit Codes

- `0`: All validations passed
- `1`: Errors found (broken links, missing docs, duplicate IDs)
- `2`: Warnings only (orphaned documents)
