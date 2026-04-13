---
name: create-agent
description: Agent Creator - Create standardized SIGNAL-focused agents under .claude/agents/ with proper YAML frontmatter, initialization requirements, mandatory rules, and output specifications.
---

# Create Agent

## Overview

Create standardized agents that integrate with the current SIGNAL orchestration files. This skill ensures agent files follow consistent patterns for initialization, rules, and output contracts.

## When to Use

- When adding a new specialized agent to the framework
- When standardizing an existing agent to match framework conventions
- When the routing matrix needs a new role not covered by existing agents

## Agent Creation Workflow

### Step 1: Define Agent Identity

Determine the following for the new agent:

| Field | Description | Examples |
|-------|-------------|----------|
| `name` | Short identifier (2-3 chars) | `qa`, `cr`, `se`, `pg` |
| `description` | Role + key responsibilities | "Quality Assurance - Integrated code/doc quality verification" |
| `tools` | Required tool access | `Read, Grep, Glob, Bash, Task` |
| `model` | Model selection | `sonnet` (speed) or `opus` (complexity) |

**Model Selection Guidelines:**
- `sonnet`: Fast tasks, straightforward verification, documentation
- `opus`: Complex decisions, security analysis, architecture design

### Step 2: Load Template

Load the agent template from `assets/agent-template.md` and customize:

```bash
# Copy template to .claude/agents/
cp .claude/skills/create-agent/assets/agent-template.md .claude/agents/<name>.md
```

### Step 3: Complete Required Sections

Every agent MUST include these sections:

#### 3.1 YAML Frontmatter (Required)

```yaml
---
name: <agent-id>
description: <Role> - <Key responsibilities>. <Usage hint>.
tools: <Comma-separated tool list>
model: <sonnet|opus>
---
```

#### 3.2 Role Statement (Required)

One sentence defining the agent's core mission:

```markdown
You are <NAME>. Your goal is to <primary objective in quotes>.
```

#### 3.3 Mandatory Rules (Required)

```markdown
## Mandatory Rules
- At task start, treat `AGENTS.md` and `CLAUDE.md` as the current constitutional baseline and prohibit violations.
- <Agent-specific rule 1>
- <Agent-specific rule 2>
- Always create deliverables in **two sets**:
  - User-facing: <What user sees>
  - Agent-facing: <What agents trace>
```

#### 3.5 Output Specification (Required)

```markdown
## Output on Invocation (Minimum)

- <Summary Type> (User-facing): <Contents>
- Evidence (Agent-facing): <Traceable artifacts>
```

### Step 4: Add Optional Sections

Based on agent complexity, add:

- **Core Responsibilities**: Numbered list of primary duties
- **Workflow**: ASCII or numbered workflow steps
- **Classification Tables**: Severity, priority, or category definitions
- **Delegation Rules**: When to hand off to other agents

### Step 5: Register Agent

After creating the agent file, update framework registration:

1. Add to CLAUDE.md routing matrix if applicable
2. Add to `.claude/config/workspace.json` agents array
3. Document in project docs if a specialized workflow is needed

## Agent Naming Conventions

| Pattern | Description | Examples |
|---------|-------------|----------|
| 2-char | Core framework agents | `se`, `re`, `pg`, `sa` |
| 2-char | Specialized roles | `qa`, `cr`, `uv`, `tr` |
| Full word | Reserved for complex/external | Avoid unless necessary |

## Tools Reference

Available tools for subagents:

| Tool | Description | Use Case |
|------|-------------|----------|
| `Read` | Read files | All agents |
| `Grep` | Search content | Investigation agents |
| `Glob` | Find files by pattern | Investigation agents |
| `Bash` | Execute commands | Build/test agents |
| `Write` | Create files | Implementation agents |
| `Edit` | Modify files | Implementation agents |
| `Task` | Delegate to other agents | Coordination agents |
| `AskUserQuestion` | Query user | Entry point agents (ps) |

## Validation Checklist

Before finalizing an agent, verify:

- [ ] YAML frontmatter has all 4 required fields
- [ ] Role statement clearly defines mission
- [ ] Mandatory Rules includes current project constitution reference
- [ ] Two-set deliverable rule is included
- [ ] Output specification defines minimum outputs
- [ ] Agent is registered in CLAUDE.md routing matrix
- [ ] Agent is added to workspace.json

## Resources

### assets/

Contains the standard agent template:

- `agent-template.md` - Base template for new agents

### references/

Contains additional guidance:

- `agent-standards.md` - Detailed standards and patterns
- `routing-matrix.md` - Current agent routing reference
