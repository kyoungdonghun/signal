---
name: pe
description: This skill should be used when strengthening Subagent instruction packets to improve output quality. It wraps WI handoff packets with format enforcement, prohibitions, and evidence requirements.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob
model: sonnet
---

# PE (Prompt Engineering)

## Purpose

Strengthen Subagent instruction packets to prevent misunderstanding, excessive output, or format violations.

## When to Use

- When a WI exists but the Subagent repeatedly misunderstands instructions
- When output quality is inconsistent (wrong format, excessive detail, scope violations)
- When needing to enforce output contracts more strictly

## How to Use

### Inputs

- WI summary (Why/Scope/DoD/Constraints)
- Assignee (Subagent name)
- Input pointers (document/file paths)
- Output contract (User-facing/Agent-facing)

### Process

**Do not modify** the existing WI handoff packet. Instead, **prepend reinforcement instructions** that:

1. **Enforce output format**: Specify required headings, checklists, file paths
2. **State prohibitions explicitly**: Out-of-scope actions, speculative execution, full-text pasting
3. **Require evidence**: File paths, line numbers, commands, test results

### Output Format

Generate a single text block that can be **prepended to the WI handoff packet**:

```
[PE WRAP — prepend to WI handoff]
- Obey the WI packet strictly. Do not deviate from stated scope.
- Output must follow the Output Contract paths exactly:
  - User-facing: deliverables/user/<WI-ID>-summary.md
  - Agent-facing: deliverables/agent/<WI-ID>-evidence-pack.md
- Use pointers (file:line) instead of pasting large excerpts.
- Evidence requirements:
  - Files changed: List all with brief "what/why"
  - Commands run: Include full command and result summary
  - Tests: Specify test command and pass/fail status
- If anything is ambiguous: Document as Assumptions/Questions. Do not proceed beyond allowed scope.
- Prohibited:
  - Speculative execution without approval
  - Full document pasting (use pointers)
  - Out-of-scope modifications
```

## Best Practices

- Keep the WI packet intact; only add reinforcement
- Be specific about format requirements
- Explicitly state what is prohibited
- Require traceable evidence (pointers, not prose)
