---
name: ce
description: This skill should be used when designing minimal context injection bundles for Subagent calls. It standardizes document selection, bundling, and pointer design to maximize performance with minimal context.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob
model: sonnet
version: 2.0
last_updated: 2026-02-01
---

# CE (Context Engineering)

## Purpose

Design minimal context injection bundles to maximize Subagent performance while avoiding context bloat.

## When to Use

- Before calling a Subagent, when unclear which documents to inject
- When tempted to inject entire `docs/` directory (forbidden)
- When establishing consistent on-demand injection using trigger rules and bundle generators

## How to Use

### Rule Engine Reference

**Configuration**: `.claude/config/context-injection-rules.json`

The rule engine provides:
- `tier_defaults`: Path patterns → Tier level (0-3)
- `category_agent_mapping`: Category → Agent groups
- `agent_required_tiers`: Agent → Required tier levels
- `task_type_documents`: Task type → Required documents

### Principle

Maintain **Tier 0 + minimal pointers only**. Add additional documents **only when necessary**.

### Injection Algorithm

```
Input: assignee, task_type (optional)

Step 1: REQUIRED
  - Collect docs matching agent_required_tiers[assignee]
  - All agents: Tier 0 (constitution)
  - Governance agents (eo, sa, pg, docops, cr): Also Tier 1

Step 2: CONDITIONAL (if task_type specified)
  - Add task_type_documents[task_type]
  - Examples:
    - security → security-policy.md, access-control-policy.md
    - architecture → system-design.md, docs/adr/*
    - testing → quality-gates.md, eval-golden-set.md

Step 3: OPTIONAL
  - Check document metadata (frontmatter):
    - If target_agents includes assignee → add
    - If task_types includes task_type → add

Step 4: SORT (Prompt Caching)
  - Order: Tier 0 → Tier 1 → Tier 2 → Tier 3
  - This maximizes prompt cache hits

Output: Sorted list of document paths
```

### Quick Reference Table

| Assignee | Required Tiers | Tier 0 Docs | Tier 1 Docs |
|----------|---------------|-------------|-------------|
| ps | [0] | core-principles | - |
| eo | [0, 1] | core-principles | policies/*, architecture |
| sa | [0, 1] | core-principles, dev-standards | adr/*, architecture |
| se | [0] | core-principles, dev-standards | - |
| re | [0] | core-principles | - |
| pg | [0, 1] | core-principles, security-policy | access-control |
| tr | [0] | core-principles | - |
| uv | [0] | core-principles | - |
| docops | [0, 1] | core-principles, doc-standards, glossary | template-governance |
| qa | [0] | core-principles, dev-standards | - |
| cr | [0, 1] | core-principles, dev-standards | security-policy, adr/* |

### Process

1. **Identify assignee and task type**
2. **Check rule engine**: Read `.claude/config/context-injection-rules.json`
3. **Apply algorithm**: Required → Conditional → Optional → Sort
4. **Generate bundle**: List of document pointers in tier order

### Output Format

Provide the minimal context bundle:

```text
[CE OUTPUT]

Assignee: <agent>
Task Type: <type or ->

Tier 0 (Required - Constitution):
- docs/standards/core-principles.md

Tier 0 (Required - Standards for Assignee):
- docs/standards/development-standards.md (if se|sa|qa|cr)

Tier 1 (Conditional - Task Type):
- docs/policies/security-policy.md (if task: security)
- docs/architecture/system-design.md (if task: architecture)

Additional (from document metadata):
- <any docs with target_agents matching assignee>

Rule Source: .claude/config/context-injection-rules.json
```

### Example: SE Implementation Task

```text
[CE OUTPUT]

Assignee: se
Task Type: implementation

Tier 0 (Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Conditional):
- (none for implementation type)

Total: 2 documents

Rule Source: .claude/config/context-injection-rules.json
agent_required_tiers["se"] = [0]
```

### Example: PG Security Task

```text
[CE OUTPUT]

Assignee: pg
Task Type: security

Tier 0 (Required):
- docs/standards/core-principles.md

Tier 1 (Required + Task):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Total: 3 documents

Rule Source: .claude/config/context-injection-rules.json
agent_required_tiers["pg"] = [0, 1]
task_type_documents["security"] = [security-policy.md, access-control-policy.md]
```

## Guidelines

- **Default**: Tier 0 documents only
- **Avoid**: Injecting entire docs/ tree
- **Prefer**: Specific file pointers over full content
- **Use**: Rule engine for consistency
- **Sort**: Tier 0 → 1 → 2 → 3 for prompt caching
- **Reference**: `.claude/config/context-injection-rules.json`
