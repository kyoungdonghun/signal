---
name: create-wi-evidence-pack
description: This skill should be used when standardizing Subagent outputs into Evidence Packs (Agent-facing deliverables). It enforces pointer-based documentation with reproducibility, test results, and rollback information.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write
model: sonnet
version: 3.0
last_updated: 2026-02-05
---

# Create WI Evidence Pack

## Purpose

Standardize Subagent work outputs into Evidence Packs that enable detailed traceability from summaries alone.

## When to Use

- When a Subagent reports task completion and needs to document results
- When creating Agent-facing deliverables for audit, reuse, or regression analysis
- When ensuring work can be traced, reproduced, and verified

## How to Use

### Project Tags

**Source**: `.claude/config/workspace.json`

WI ID must include project tag from workspace.json:
- `meta_framework.tag` → System tag (e.g., `SYS`)
- `domain_projects[].tag` → Domain project tags (e.g., `DDS`, `USI`, `PMV2`)

**Naming Convention**: `WI-YYYYMMDD-<PRJ>-###-evidence-pack.md`

### Inputs

- WI ID (must include project tag, e.g., `WI-20260201-DDS-001`)
- Work summary (one-line description)
- Changed files/paths (if applicable)
- Commands executed / logs / test results (if applicable)
- **Injected documents** (from handoff packet's INPUT POINTERS)

### Process

1. **Document results** as an **Agent-facing Evidence Pack**
2. **Follow principles**:
   - **Pointers over prose**: File paths, line numbers, commands, logs
   - **Reproducibility**: Include minimal reproduction steps
   - **Test results**: If tests were run, include command and outcome
   - **Reference traceability**: Document which Tier 0-2 docs were injected
3. **Generate output** following the OUTPUT format below
4. **Save immediately**: Write to `deliverables/agent/<WI-ID>-evidence-pack.md`

### Output Format

Generate an Evidence Pack following this structure:

```text
# Evidence Pack: <WI-ID>

## Summary (one-liner)
- Brief description of what was accomplished

## Scope / DoD Check
- DoD items:
  - [x] Item 1
  - [x] Item 2

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution (all agents) |
| 0 | docs/standards/development-standards.md | Assignee: se |
| 1 | docs/policies/security-policy.md | Task type: security |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: <agent>
- Task type: <type>
- agent_required_tiers: [0] or [0, 1]

## Evidence Pointers (required)
- Files changed:
  - path/to/file.ext (what changed / why)
- Key locations (recommended):
  - path/to/file.ext:10-25 (explanation)

## Commands & Outputs (if any)
- Commands executed:
  - `command here`
- Outputs:
  - Brief result or pointer to log file

## Tests (if any)
- `test command` → pass/fail + notes

## Risks / Rollback
- Risks:
  - Identified risks from this change
- Rollback:
  - Steps to revert if needed

## Follow-ups (optional)
- Next WI candidates:
  - Suggested follow-up work items
```

## Best Practices

- **Prefer pointers** (file:line) over long explanations
- **Make it reproducible**: Someone else should be able to verify
- **Include test evidence**: Commands and results, not just "tested"
- **Document rollback**: How to undo if needed
- **List injected docs**: For traceability, document which context was provided
- **Save to**: `deliverables/agent/<WI-ID>-evidence-pack.md`

## Execution

⚠️ **This skill MUST generate the Evidence Pack directly, not just provide guidance.**

### Precondition Gate (MANDATORY)

**Before ANY processing, verify WI handoff packet exists:**

```
1. Extract WI ID from invocation (e.g., WI-20260205-SYS-001)
2. Check if deliverables/agent/<WI-ID>-handoff.md exists
3. If NOT exists:
   - STOP execution
   - Output: "⛔ BLOCKED: WI handoff packet이 없습니다. 먼저 /create-wi-handoff-packet으로 WI를 생성하세요."
   - Do NOT generate evidence pack
```

### Main Execution Steps

Upon invocation (after precondition passes):

1. **Read** WI handoff packet from `deliverables/agent/<WI-ID>-handoff.md`
2. **Extract** INPUT POINTERS section from handoff packet
3. **Parse** Tier 0, Tier 1, Tier 2 documents from INPUT POINTERS
4. **Collect** work results:
   - Changed files/paths
   - Commands executed
   - Test results
   - Risks identified
5. **Generate** Evidence Pack following Output Format above
6. **Auto-populate** Reference Documents section from handoff's INPUT POINTERS
7. **Write** evidence pack to `deliverables/agent/<WI-ID>-evidence-pack.md`
8. **Report** the file path to user

**DO NOT** just show the format and wait - **CREATE** the evidence pack immediately.
