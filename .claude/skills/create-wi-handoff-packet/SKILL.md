---
name: create-wi-handoff-packet
description: This skill should be used when MA delegates work to Subagents. It generates standard instruction packets with WI summary, input pointers, output contracts, and traceability requirements in a consistent format.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write
model: sonnet
version: 3.0
last_updated: 2026-02-05
---

# Create WI Handoff Packet

## Purpose

Generate standard instruction packets for Subagent delegation with consistent structure, clear contracts, and traceability requirements.

## When to Use

- When MA is about to delegate work to a Subagent (ps, se, pg, docops, etc.)
- When ensuring consistent document mapping to reduce omissions
- When attaching on-demand injection bundles to the delegation

## How to Use

### Project Tags

**Source**: `.claude/config/workspace.json`

1. Read `workspace.json` to get available project tags:
   - `meta_framework.tag` → System tag (e.g., `SYS`)
   - `domain_projects[].tag` → Domain project tags (e.g., `DDS`, `USI`, `PMV2`)

2. Match current context to appropriate tag based on REQ or working directory.

**Naming Convention**: `WI-YYYYMMDD-<PRJ>-###.md`
- Example: `WI-20260201-SYS-001-handoff.md`
- Example: `WI-20260201-DDS-001-summary.md`

### WI Sequence Number Assignment

**CRITICAL: Check existing WIs before assigning new WI ID**

1. **Scan existing WIs**: Check `deliverables/agent/` for files matching pattern `WI-YYYYMMDD-<PRJ>-*`
2. **Extract highest number**: Parse sequence numbers from matching files
3. **Assign next number**: Use highest + 1 for new WI

**Algorithm**:
```
1. List files in deliverables/agent/ matching WI-<today>-<PRJ>-*
2. Extract ### portion from each filename
3. Find max(###) from existing files
4. New WI ID = WI-YYYYMMDD-<PRJ>-{max + 1, zero-padded to 3 digits}
5. If no existing files, start with 001
```

**Examples**:
- Existing: `WI-20260201-SYS-001`, `WI-20260201-SYS-002` → New: `WI-20260201-SYS-003`
- Existing: None → New: `WI-20260201-SYS-001`
- Existing: `WI-20260201-DDS-005`, `WI-20260201-DDS-003` → New: `WI-20260201-DDS-006`

**⚠️ VIOLATION: Overwriting existing WI files will cause traceability failure**

### Inputs (minimal)

- WI ID (or `TBD` if not yet assigned) - **MUST include project tag**
- Session ID (current active session or `none`)
- Assignee (Subagent name)
- Work purpose, scope, DoD, constraints (including prohibitions)
- Related document/file pointers (prefer paths)

### Context Inference (LLM-based)

**Document Catalog**: `docs/index.md` (references category indexes)

#### 1. Required Tier 0 Documents (by Assignee)

All Subagents receive Tier 0 documents based on their role:
- `docs/standards/core-principles.md` - All agents
- `docs/standards/development-standards.md` - se, sa, qa, cr
- `docs/standards/documentation-standards.md` - docops
- `docs/standards/glossary.md` - docops
- `docs/policies/security-policy.md` - pg, cr

#### 2. Additional Document Inference (LLM analyzes REQ + WI)

Analyze REQ and WI content to select relevant documents:

| Work Characteristics | Documents to Infer |
|---------------------|-------------------|
| Security, PII, authentication, authorization | `docs/policies/security-policy.md` |
| Quality, testing, verification | `docs/policies/quality-gates.md` |
| Access control, permission management | `docs/policies/access-control-policy.md` |
| Version management, releases | `docs/policies/versioning-policy.md` |

**Inference Method**:
1. Analyze REQ GOAL/SCOPE section
2. Extract keywords/context from WI description
3. Reference `docs/index.md` and category indexes to identify relevant documents

#### 3. Sort Order

Tier 0 → Tier 1 → Tier 2 (for prompt caching efficiency)

#### 4. Document Scope (Meta-only vs Universal)

**Source of Truth**: `docs/index.md` → "Document Scope Classification" section

**Rule**: If project tag is NOT `SYS`, exclude meta-only documents from INPUT POINTERS.

### Agent Assignment

**Source**: Approved REQ's EXECUTION STRATEGY table

Agent is already assigned in REQ - copy from REQ EXECUTION STRATEGY.
Do not re-assign agents in WI. REQ is the single source of truth for agent assignment.

### Process

**CRITICAL: Before generating output, MUST complete this checklist:**

1. **Reference REQ**: Get agent, skill chain, quality gates from approved REQ
2. **Check Existing WIs**: Scan `deliverables/agent/WI-YYYYMMDD-<PRJ>-*` to find next sequence number
3. **Infer Required Docs**: Based on assignee (Tier 0) + REQ/WI content analysis (Tier 1/2)
4. **MANDATORY: Include ALL required Tier documents** in INPUT POINTERS section
   - ⚠️ VIOLATION: Omitting required docs will cause delegation failure
5. **Define Acceptance Criteria**: Functional, Performance, Quality requirements
6. **Generate output** following the OUTPUT format below with Tier docs in priority order
7. **Follow additional principles**:
   - Use **pointers (paths/links)** instead of pasting full content
   - **Require 2-set deliverables**: User-facing + Agent-facing
   - **Enforce traceability**: Evidence pointers, tests, reproducibility
8. **Save handoff packet**: Write to `deliverables/agent/<WI-ID>-handoff.md` immediately

### Output Format

⚠️ **REMINDER: INPUT POINTERS section MUST list documents in Tier order (0 → 1 → 2)!**

Generate a WI Handoff Packet following this structure:

```text
[WI HEADER]
WI ID: <WI-YYYYMMDD-<PRJ>-### | TBD>
REQ: <REQ-YYYYMMDD-<PRJ>-### | TBD>
Agent: <from REQ EXECUTION STRATEGY>
Depends On: <WI-ID | - >
Blocks: <WI-ID | - >

[WI SUMMARY]
Why: Brief statement of purpose
Scope (in/out): What is included and excluded
DoD: Definition of Done criteria
Constraints/Forbidden: Explicit prohibitions and constraints

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Criterion 1
- [ ] Criterion 2
Performance:
- [ ] Response time < Xms (if applicable)
- [ ] Memory usage < XMB (if applicable)
Quality:
- [ ] No lint errors
- [ ] Type check passes
- [ ] Tests pass

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md (if Assignee: se|sa|qa|cr)
- docs/standards/documentation-standards.md (if Assignee: docops)
- docs/standards/glossary.md (if Assignee: docops)

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md (if work involves: security, PII, auth)
- docs/policies/quality-gates.md (if work involves: testing, quality)

Tier 2 (Tech Stack - Conditional on project tech_stack from workspace.json):
- .claude/skills/react-best-practices/AGENTS.md (if tech_stack includes: react AND Assignee: se|qa|cr|uv)

REQ/Context Docs:
- path/to/REQ.md
- path/to/context-document.md

Files:
- path/to/file.ext[:line-range]

Repro/Logs:
- command or path to log

[OUTPUT CONTRACT]
User-facing -> deliverables/user/<WI-ID>-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/<WI-ID>-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/<WI-ID>-handoff.md :
- This packet (for traceability)

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: If applicable, include test command and results
Rollback (if needed): Document how to revert changes
```

Note: SKILL CHAIN, EXECUTION PLAN, QUALITY GATES are defined in REQ - reference REQ for these details.

## Best Practices

- **Reference REQ**: Skill chain, parallel plan, quality gates are in REQ - don't duplicate
- **Infer context**: Analyze REQ/WI content to select relevant docs (use `docs/index.md` as catalog)
- **Tier order matters**: Always list Tier 0 first, then 1, then 2 (prompt caching)
- **Use pointers** instead of full content
- **Be explicit** about constraints and prohibitions
- **Define acceptance criteria**: Functional, Performance, Quality requirements per WI
- **Track dependencies**: Use Depends On / Blocks for WI sequencing
- **Require 2 deliverables**: User-facing summary + Agent-facing evidence pack
- **Enforce traceability**: Pointers, commands, tests must be documented
- **Save handoff to**: `deliverables/agent/<WI-ID>-handoff.md`

## Execution

⚠️ **This skill MUST generate the WI handoff packet directly, not just provide guidance.**

### Precondition Gate (MANDATORY)

**Before ANY processing, verify approved REQ exists:**

```
1. Check if REQ ID is provided in invocation
2. Read deliverables/user/<REQ-ID>.md
3. Verify Status contains "approved" or "✅"
4. If NOT approved:
   - STOP execution
   - Output: "⛔ BLOCKED: 승인된 REQ가 없습니다. 먼저 /create-req로 REQ를 생성하고 승인받으세요."
   - Do NOT generate handoff packet
```

### Main Execution Steps

Upon invocation with WI assignment (after precondition passes):

1. **Read** approved REQ to get agent assignment, skill chain, quality gates
2. **Read** `.claude/config/workspace.json` to get project tag
3. **Read** `.claude/config/context-injection-rules.json` to get injection rules
4. **Scan** existing WIs in `deliverables/agent/` to determine next sequence number
5. **Extract Tier 0/1 documents** from `agent_specific_requirements[assignee].required`
6. **Match tech_stack_documents**: Read project's `tech_stack` from `workspace.json`, check `tech_stack_documents` in rules for matching entries where `applicable_agents` includes current assignee. If matched, add documents to Tier 2 pointers.
7. **Match task_type_documents** using 2-phase strategy:
   - **Phase 1 (Keyword)**: Check if WI summary contains any keywords from `task_type_documents.types[].keywords`
   - **Phase 2 (LLM)**: Analyze WI content semantically to identify additional relevant documents
   - **Merge**: Union results from both phases, deduplicate
8. **Generate** WI handoff packet following Output Format above with auto-injected documents
9. **Write** handoff packet to `deliverables/agent/<WI-ID>-handoff.md` using Write tool
10. **Report** the file path to user for delegation

### Context Injection Algorithm

```python
def get_input_pointers(assignee: str, wi_summary: str, rules: dict) -> dict:
    """
    Auto-generate INPUT POINTERS section from context-injection-rules.json
    """
    pointers = {"tier_0": [], "tier_1": [], "tier_2": [], "tier_3": []}

    # Step 1: Agent-specific requirements (Tier 0/1)
    agent_reqs = rules["agent_specific_requirements"].get(assignee, {})
    for doc in agent_reqs.get("required", []):
        tier = get_tier_from_path(doc, rules["tier_defaults"])
        pointers[f"tier_{tier}"].append(doc)

    # Step 2: Tech-stack-based injection (from workspace.json + tech_stack_documents)
    project = find_project_by_tag(workspace_json, project_tag)
    if project:
        tech_stack = project.get("tech_stack", [])
        for tech in tech_stack:
            tech_config = rules.get("tech_stack_documents", {}).get(tech)
            if tech_config and assignee in tech_config.get("applicable_agents", []):
                tier = tech_config.get("tier", 2)
                for doc in tech_config.get("documents", []):
                    pointers[f"tier_{tier}"].append(doc)

    # Step 3: Task-type keyword matching (Tier 2/3)
    summary_lower = wi_summary.lower()
    for task_type in rules["task_type_documents"]["types"]:
        for keyword in task_type["keywords"]:
            if keyword.lower() in summary_lower:
                for doc in task_type["documents"]:
                    tier = task_type.get("tier", 2)
                    if isinstance(tier, list):
                        tier = tier[0]
                    pointers[f"tier_{tier}"].append(doc)
                break

    # Step 4: Deduplicate each tier
    for tier_key in pointers:
        pointers[tier_key] = list(dict.fromkeys(pointers[tier_key]))

    return pointers
```

**DO NOT** just show the format and wait - **CREATE** the handoff packet immediately.
