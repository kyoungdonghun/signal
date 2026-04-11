---
name: create-req
description: This skill should be used when normalizing user requests into REQ (Requirement Definition) drafts. It structures goal, scope, success criteria, constraints, risks, and approval points for user sign-off.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write
model: sonnet
version: 3.0
last_updated: 2026-02-05
---

# Create REQ

## Purpose

Normalize user requests into structured REQ (Requirement Definition) drafts that can be approved before implementation begins.

## When to Use

- When a user expresses a requirement that needs approval before implementation
- When enforcing the "no speculative execution" principle
- When converting informal requests into approvable requirement documents

## How to Use

### Inputs

- User requirement (verbatim if possible)
- Optional constraints: schedule, technology, security, budget, preferences

### Process

1. **Draft the REQ** using the standard output format below
2. **Keep it concise**: Focus on "what/why/success criteria/constraints"
3. **Include approval points** and **minimal open questions** so users can approve or request changes immediately
4. **Defer implementation details**: Leave planning, detailed design, and WI breakdown until after REQ approval

### Project Tags

**Source**: `.claude/config/workspace.json`

1. Read `workspace.json` to get available project tags:
   - `meta_framework.tag` → System tag (e.g., `SYS`)
   - `domain_projects[].tag` → Domain project tags (e.g., `DDS`, `USI`, `PMV2`)

2. Match current context to appropriate tag:
   - If working in meta framework → use `meta_framework.tag`
   - If working in domain project → use matching `domain_projects[].tag`

**Naming Convention**: `REQ-YYYYMMDD-<PRJ>-###.md`
- Example: `REQ-20260201-SYS-001.md` (meta framework)
- Example: `REQ-20260201-DDS-001.md` (domain project)

### Language Policy

⚠️ **CRITICAL: REQ content MUST be written in the user's language (e.g., Korean)**

This is an **EXCEPTION** to the English documentation rule. REQ is user-facing and requires user approval.

| Element | Language | Example |
|---------|----------|---------|
| Section headers | English | `[GOAL / WHY]`, `[SCOPE]` |
| **ALL content** | **User's language** | 한국어, 日本語, etc. |
| Technical terms | Original | API, WI, REQ |

**Violation**: Writing REQ content in English when user speaks Korean = **POLICY VIOLATION**

Reference: `CLAUDE.md` → Language Policy (Three-Track) → REQ (Exception)

### Output Format

Generate a REQ draft following this structure:

```text
[REQ]
REQ ID: <REQ-YYYYMMDD-<PRJ>-### | TBD>
Project: <PRJ tag from workspace.json>
Owner: MA
Status: draft (approval pending)

[GOAL / WHY]
- Clear statement of what is needed and why

[SCOPE]
- In:
  - What is included in this requirement
- Out:
  - What is explicitly excluded

[SUCCESS CRITERIA (DoD)]
- Measurable criteria to determine when this requirement is satisfied

[CONSTRAINTS / POLICIES]
- Work tracking unit: WI
- No speculative execution before approval
- Security/data/irreversible changes require pre-approval
- Other:
  - Project-specific constraints

[EXECUTION STRATEGY]
| Task | Subagent | Skill | Context Injection |
|------|----------|-------|-------------------|
| Task description | Agent name | Skill name or - | Tier docs + files |

(Subagent and Model are defined in CLAUDE.md Routing Matrix - single source of truth)
(Context Injection: List required Tier 0/1/2 docs per context-injection-rules.json)

[PARALLEL WORK PLAN]

> **Goal**: Achieve maximum quality in minimum time through optimal work decomposition.
> Fine-grained WI splitting enables parallel agent execution, reducing total elapsed time
> while maintaining quality through independent verification at each unit.

**WI Granularity Principles**:
1. **Atomic Unit**: 1 WI = 1 agent instance = 1 deliverable scope
2. **Independence**: If tasks have no data dependency, split into separate WIs
3. **Parallelization**: More WIs = more parallel agents = faster completion
4. **Size Guideline**: 5-15 files per WI (too large = bottleneck, too small = overhead)

**Quality/Review Phase Split Rules**:
- **Quality checks**: ALWAYS split (typecheck ∥ eslint ∥ test are independent)
- **Code review**: Split by functional area (each cr instance reviews different scope)
- **Testing**: Split by test suite if independent (unit ∥ integration ∥ e2e)

| Phase | WI (parallel within phase) | Depends On |
|-------|---------------------------|------------|
| 1 | WI-001: task ∥ WI-002: task ∥ WI-003: task | - |
| 2 | WI-004: task | Phase 1 |
| N-1 | WI-X: typecheck ∥ WI-Y: eslint ∥ WI-Z: test | Phase N-2 |
| N | WI-A: review-area1 ∥ WI-B: review-area2 ∥ WI-C: review-area3 | Phase N-1 |

**Example (Auth Feature)**:
| Phase | WI | Agent | Depends On |
|-------|-----|-------|------------|
| 1 | WI-001: login ∥ WI-002: signup ∥ WI-003: password-reset | se ∥ se ∥ se | - |
| 2 | WI-004: typecheck ∥ WI-005: eslint ∥ WI-006: test | qa ∥ qa ∥ qa | Phase 1 |
| 3 | WI-007: review-login ∥ WI-008: review-signup ∥ WI-009: review-password | cr ∥ cr ∥ cr | Phase 2 |

**Anti-patterns (AVOID)**:
- ❌ `WI-X: typecheck/lint/test` → Split into 3 WIs
- ❌ `WI-X: full code review` → Split by scope/area
- ❌ `WI-X: entire section` (30+ files) → Split by feature/component

Note: Upon REQ approval, generate each WI immediately using /create-wi-handoff-packet.
Each WI packet contains its own agent assignment, skill chain, and quality gates.

[QUALITY GATES]
| Gate | Condition | Verification |
|------|-----------|--------------|
| G1 | Description | Verification method |

(Define checkpoints to ensure quality)

[RISKS / OPEN QUESTIONS]
- Risks:
  - Identified risks or concerns
- Questions (minimal):
  - Questions that must be resolved before approval

[APPROVAL POINTS]
- Approve as-is / Needs changes:
  - Clear decision points for the user
```

## Best Practices

- **Be specific** about scope boundaries (in/out)
- **Make success criteria measurable**
- **Ask minimal questions** - only blockers
- **Defer "how"** until after approval
- **Plan execution strategy**: Assign tasks to appropriate subagents based on routing matrix in CLAUDE.md
- **Maximize parallelization**:
  - Split independent features into separate WIs (more WIs = more parallel agents)
  - ALWAYS split quality checks: typecheck ∥ eslint ∥ test (3 WIs, not 1)
  - ALWAYS split code review by scope: review-area1 ∥ review-area2 (N WIs, not 1)
  - Target 5-15 files per WI for optimal balance
- **Define quality gates**: Set checkpoints to verify quality at key milestones
- **Generate WI immediately**: Upon REQ approval, use `/create-wi-handoff-packet` for each planned WI
- **Save to**: `deliverables/user/<REQ-ID>.md` after approval

## Execution

⚠️ **This skill MUST generate the REQ file directly, not just provide guidance.**

Upon invocation with user requirement:

1. **Read** `.claude/config/workspace.json` to get project tags
2. **Read** `.claude/config/context-injection-rules.json` to get injection rules
3. **Determine** appropriate project tag based on context
4. **Generate** REQ content following Output Format above
5. **Auto-generate Context Injection column** in EXECUTION STRATEGY table:
   - For each task row, look up the Subagent column value
   - Query `agent_specific_requirements[subagent].required` from context-injection-rules.json
   - Insert the required documents into Context Injection column
6. **Write** REQ file to `deliverables/user/<REQ-ID>.md` using Write tool
7. **Report** the file path to user for review

### Context Injection Column Auto-Generation

```python
def get_context_injection(subagent: str, rules: dict) -> str:
    """
    Auto-generate Context Injection column value from context-injection-rules.json
    """
    agent_reqs = rules["agent_specific_requirements"].get(subagent, {})
    required_docs = agent_reqs.get("required", [])

    # Format as comma-separated short names
    short_names = [doc.split("/")[-1] for doc in required_docs]
    return ", ".join(short_names)
```

**Example**:
| Task | Subagent | Skill | Context Injection |
|------|----------|-------|-------------------|
| 구현 | se | - | core-principles.md, development-standards.md |
| 보안 검토 | pg | - | core-principles.md, security-policy.md |
| 문서화 | docops | - | core-principles.md, documentation-standards.md, glossary.md |

**DO NOT** just show the format and wait - **CREATE** the REQ file immediately.
