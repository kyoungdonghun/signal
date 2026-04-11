---
name: sync-docs-index
description: This skill should be used when synchronizing documentation index counts with actual file counts. It verifies and optionally fixes the Document Overview table in docs/index.md to match the actual number of .md files in each category directory.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Bash, Edit
model: sonnet
---

# Sync Docs Index

## Purpose

Synchronize the Document Overview table in `docs/index.md` with the actual count of markdown files in each category directory. Ensure the index remains accurate when documents are added, removed, or reorganized.

## When to Use

- After adding or removing documentation files
- Before running `/validate-docs` to ensure index accuracy
- When document counts appear inconsistent
- As part of documentation maintenance workflows
- Before commits or pull requests that include documentation changes

## How to Use

### Check Mode (Default, Read-only)

Run the skill without flags or with explicit `--check` flag.

```
/sync-docs-index
/sync-docs-index --check
```

**Procedure:**

1. Define target category directories for counting
2. For each category, count markdown files using Bash:
   ```bash
   find <category-dir> -name "*.md" ! -name "index.md" | wc -l
   ```
3. Read `docs/index.md` using Read tool
4. Parse the Document Overview table to extract current counts
5. Compare actual counts with index counts
6. Report mismatches in a structured table format

**Output Example:**

```
Documentation Index Sync Check

| Category | Index Count | Actual Count | Status |
|----------|-------------|--------------|--------|
| Architecture | 1 | 1 | Match |
| Design | 2 | 2 | Match |
| Guides | 11 | 12 | Mismatch |
| Policies | 7 | 7 | Match |
| Standards | 9 | 9 | Match |
| Templates | 18 | 18 | Match |
| Registry | 4 | 4 | Match |
| ADR | 1 | 1 | Match |
| Analysis | 2 | 2 | Match |
| Eval | 0 | 0 | Match |

Result: 1 mismatch found
Action Required: Run with --fix flag to update docs/index.md
```

### Fix Mode (Write)

Run the skill with `--fix` flag to update the index.

```
/sync-docs-index --fix
```

**Procedure:**

1. Execute Check Mode first to display current state
2. If mismatches are found:
   - For each mismatch, use Edit tool to replace the old count with the actual count in the Document Overview table
   - Update only the "Document Count" column values
   - Preserve all other table content and formatting
3. Display before/after summary showing which counts were updated
4. Verify changes by showing the updated table section

**Safety Measures:**
- Always display current state before making changes
- Show explicit before/after values for each update
- Only modify numeric values in the Document Count column
- Never modify category names, index links, or descriptions

**Output Example:**

```
Documentation Index Sync Check
[... current state table ...]

Applying fixes...

Updated counts:
- Guides: 11 → 12

Changes applied to docs/index.md
Verification: All counts now match actual file counts
```

## Target Categories

The skill counts markdown files in the following directories:

| Category | Directory Path | Count Rules |
|----------|---------------|-------------|
| Architecture | `docs/architecture/` | Exclude `index.md` |
| Design | `docs/design/` | Recursive (includes subdirectories like `protocols/`) |
| Guides | `docs/guides/` | Exclude `index.md` |
| Policies | `docs/policies/` | Exclude `index.md` |
| Standards | `docs/standards/` | Exclude `index.md` |
| Templates | `docs/templates/` | Exclude `index.md` |
| Registry | `docs/registry/` | Exclude `index.md` |
| ADR | `docs/adr/` | Exclude `index.md` |
| Analysis | `docs/analysis/` | Exclude `index.md` |
| Eval | `docs/eval/` | Exclude `index.md` |

## Counting Rules

1. **File Type**: Count only `.md` files (markdown)
2. **Exclusions**: Exclude `index.md` files from count
3. **Recursion**: For Design category, include subdirectories recursively
4. **Command Format**:
   - Standard: `find docs/<category>/ -maxdepth 1 -name "*.md" ! -name "index.md" | wc -l`
   - Design (recursive): `find docs/design/ -name "*.md" ! -name "index.md" | wc -l`

## Implementation Notes

- Use Bash tool with `find` and `wc` for accurate file counting
- Use Read tool to load `docs/index.md` content
- Parse the Document Overview table by locating the table section between the header and the next markdown section
- Use Edit tool for targeted updates in --fix mode
- Maintain exact table formatting (alignment, spacing, markdown syntax)
- Validate that the table structure remains intact after updates

## Example Workflow

1. Developer adds a new guide: `docs/guides/new-workflow.md`
2. Run `/sync-docs-index` to check status
3. See mismatch: Guides shows 11 in index but 12 actual files
4. Run `/sync-docs-index --fix` to update
5. Verify: Index now shows 12 for Guides category
6. Proceed with `/validate-docs` for full documentation validation
