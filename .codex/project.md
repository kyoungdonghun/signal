# Codex Project Wrapper

## Purpose

This file is a thin Codex-facing wrapper for SIGNAL.
It does not replace `.claude`.
It tells Codex what to read, what to trust, and what to treat carefully.

## Read Order

1. [AGENTS.md](/C:/Users/jm991/Desktop/project/Signal/AGENTS.md)
2. [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md)
3. [.claude/config/PIPELINE.md](/C:/Users/jm991/Desktop/project/Signal/.claude/config/PIPELINE.md)
4. Relevant files in [.claude/agents/runtime](/C:/Users/jm991/Desktop/project/Signal/.claude/agents/runtime)
5. Real implementation under [src/main/java](/C:/Users/jm991/Desktop/project/Signal/src/main/java) and [frontend/src](/C:/Users/jm991/Desktop/project/Signal/frontend/src)

## What To Trust

- The actual application code is authoritative for runtime behavior.
- `CLAUDE.md` and `PIPELINE.md` are authoritative for project philosophy and intended contracts, unless contradicted by code.
- Runtime agent docs are useful as role references, not as proof that orchestration is fully implemented.

## What Not To Assume

- Do not assume `.claude/config/context-injection-rules.json` is fully valid for the current repo.
- Do not assume all referenced `docs/standards/*`, `docs/policies/*`, or `docs/architecture/*` files exist.
- Do not assume text with broken Korean encoding is semantically safe without cross-checking.
- Do not assume running `./gradlew test` means the app server must be restarted. Tests use an isolated `test` profile.

## Claude Asset References

- Core project constitution: [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md)
- Pipeline contract: [.claude/config/PIPELINE.md](/C:/Users/jm991/Desktop/project/Signal/.claude/config/PIPELINE.md)
- Runtime roles: [.claude/agents/runtime](/C:/Users/jm991/Desktop/project/Signal/.claude/agents/runtime)
- Meta-layer rationale: [docs/meta-layer-charter.md](/C:/Users/jm991/Desktop/project/Signal/docs/meta-layer-charter.md)

## Migration Posture

Current strategy:

- Keep Claude data in place.
- Do not merge or duplicate the full orchestration framework into Codex.
- Let Codex operate as a lightweight adapter over the existing project sources.
- Fix weak spots incrementally: encoding, missing docs, test isolation, and stale framework references.
