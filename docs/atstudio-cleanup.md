# ATStudio Cleanup Report

## Decision

The repository no longer treats the ATStudio-style delivery process as active SIGNAL infrastructure.
However, general app-building worker roles are still valid and remain part of SIGNAL.

Current rule:

- Keep SIGNAL runtime pipeline assets.
- Remove ATStudio-specific build agents and WI/REQ workflow assets.
- Keep only skills that still help the current SIGNAL codebase directly.

## Classification

### Keep

- `CLAUDE.md`
- `.claude/config/PIPELINE.md`
- `.claude/agents/runtime/*`
- Domain skills such as `yahoo-finance-fetch`, `rss-feed-fetch`, `technical-indicator-calc`, `claude-api-call`
- Utility skills still usable in the current repo: `lint`, `test`, `test-coverage`, `build-check`, `create-agent`, `react-best-practices`, `skill-creator`, `manage-hooks`, `validate-docs`

### Delete

- `.claude/skills/create-req`
- `.claude/skills/create-wi-handoff-packet`
- `.claude/skills/create-wi-evidence-pack`
- `.claude/skills/ce`
- `.claude/skills/pe`

### Restore As SIGNAL Build Workers

- `.claude/agents/build/*`

These files are kept only as lightweight app-building roles, not as an imported ATStudio process contract.

### Defer

- `manage-hooks`
  - Keep, but convert scripts to real project commands instead of Claude-only commands.
- `.claude/settings.local.json`
  - User-local configuration; not part of the shared migration baseline.

## Why

- The deleted assets describe a parallel delivery framework, not the active SIGNAL runtime pipeline.
- They depend on REQ/WI/handoff/evidence workflows that are not part of current SIGNAL operation.
- Build workers were restored because generic planning, implementation, review, QA, and documentation roles are still useful for building the SIGNAL app itself.

## Result Of This Pass

- SIGNAL documentation and config now center on runtime agents and executable app behavior.
- Shared project context is simpler and less coupled to ATStudio assumptions.
