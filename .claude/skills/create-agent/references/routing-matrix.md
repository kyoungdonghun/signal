# Agent Routing Matrix Reference

## Current Agent Registry

| Agent | Role | Model | Primary Trigger |
|-------|------|-------|-----------------|
| `ps` | Product Strategist | sonnet | User intent normalization |
| `eo` | Ensemble Overseer | opus | Governance, routing decisions |
| `sa` | Software Architect | opus | Architecture, ADR creation |
| `se` | Software Engineer | opus | Implementation, refactoring |
| `re` | Reliability Engineer | sonnet | Testing, regression verification |
| `pg` | Privacy Guardian | opus | Security, sensitive data |
| `tr` | Technology Researcher | sonnet | Tech comparison, alternatives |
| `uv` | UX/UI Virtuoso | sonnet | Design system, UX patterns |
| `docops` | Documentation Ops | sonnet | Doc management, drift detection |
| `qa` | Quality Assurance | sonnet | Integrated quality verification |
| `cr` | Code Reviewer | opus | Code review, best practices |

## Routing Decision Tree

```
User Request
    │
    ├─ Intent unclear? → ps (clarify)
    │
    ├─ Policy/routing question? → eo (govern)
    │
    ├─ Architecture decision? → sa (design)
    │
    ├─ Implementation needed? → se (code)
    │
    ├─ Testing required? → re (verify)
    │
    ├─ Security concern? → pg (protect)
    │
    ├─ Tech comparison? → tr (research)
    │
    ├─ UI/UX design? → uv (design)
    │
    ├─ Documentation? → docops (maintain)
    │
    ├─ Quality check? → qa (verify)
    │
    └─ Code review? → cr (review)
```

## Multi-Agent Collaboration Patterns

### Pattern 1: New Feature
```
ps → sa → pg → se → re → docops
```

### Pattern 2: Security Response
```
pg → sa → se → re
```

### Pattern 3: Code Quality
```
qa → cr → se → re
```

### Pattern 4: Documentation Update
```
docops → sa (if arch) → se (if code examples)
```

## When to Add New Agent

Consider a new agent when:

1. **Distinct Expertise**: Task requires specialized knowledge not covered
2. **Different Model Needs**: Task requires different speed/complexity tradeoff
3. **Tool Combination**: Unique tool set needed for the role
4. **Delegation Pattern**: Frequent handoffs suggest specialized role

## Registration Requirements

After creating an agent:

1. **CLAUDE.md**: Add to routing matrix table
2. **workspace.json**: Add to `agents` array
3. **This document**: Update routing matrix
4. **docs/guides/**: Add workflow guide if complex
