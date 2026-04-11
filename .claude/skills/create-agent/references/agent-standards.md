# Agent Standards Reference

## YAML Frontmatter Specification

Every agent file must begin with YAML frontmatter containing exactly these fields:

```yaml
---
name: <string>           # Required: 2-3 character agent identifier
description: <string>    # Required: Role - Responsibilities. Usage hint.
tools: <string>          # Required: Comma-separated tool list
model: <string>          # Required: "sonnet" or "opus"
---
```

## Model Selection Criteria

| Model | When to Use | Examples |
|-------|-------------|----------|
| `sonnet` | Speed-critical, straightforward tasks | `ps`, `re`, `qa`, `tr`, `uv`, `docops` |
| `opus` | Complex decisions, security, architecture | `se`, `sa`, `pg`, `cr`, `eo` |

## Tool Availability Matrix

| Tool | Description | Typical Agents |
|------|-------------|----------------|
| `Read` | Read file contents | All agents |
| `Grep` | Search file contents | Investigation, review |
| `Glob` | Find files by pattern | Investigation, review |
| `Bash` | Execute shell commands | Build, test, automation |
| `Write` | Create new files | Implementation |
| `Edit` | Modify existing files | Implementation |
| `Task` | Delegate to subagents | Coordination, complex workflows |
| `AskUserQuestion` | Query user interactively | Entry point agents only |

## Required Sections Checklist

1. **YAML Frontmatter** - All 4 fields present
2. **Role Statement** - Single sentence with quoted objective
3. **Required Initialization** - Tier 0 document loading
4. **Mandatory Rules** - Constitution reference + two-set rule
5. **Output Specification** - Minimum deliverable definition

## Optional Sections

- **Core Responsibilities** - Numbered list of duties
- **Workflow** - Step-by-step process
- **Classification Tables** - Severity/priority definitions
- **Delegation Rules** - When to hand off to other agents

## Two-Set Deliverable Standard

Every agent output must include both sets:

### User-facing Set
- Brief, actionable summary
- Pass/fail status when applicable
- Risk assessment if relevant
- Clear next steps or approval points

### Agent-facing Set
- File paths with line numbers
- Commands executed with outputs
- Log snippets or error messages
- Reproduction steps
- Follow-up WI recommendations

## Agent Categories

### Entry Point Agents
- Role: First contact with user requests
- Tool: `AskUserQuestion` available
- Example: `ps` (Product Strategist)

### Investigation Agents
- Role: Search, analyze, verify
- Tools: `Read`, `Grep`, `Glob`
- Examples: `cr`, `pg`, `tr`

### Implementation Agents
- Role: Create, modify, build
- Tools: `Write`, `Edit`, `Bash`
- Examples: `se`, `uv`

### Verification Agents
- Role: Test, validate, ensure quality
- Tools: `Bash`, `Read`, `Grep`
- Examples: `re`, `qa`

### Coordination Agents
- Role: Orchestrate multi-agent workflows
- Tools: `Task` for delegation
- Examples: `eo`, `docops`
