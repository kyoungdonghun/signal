# SIGNAL Agent Guide

## Project Identity

- Project: SIGNAL
- Purpose: AI-assisted market intelligence pipeline for US and KR tickers
- Stack: Spring Boot, Java 17, MySQL, React, TypeScript, Vite
- Runtime shape: market data + news collection + technical reasoning + cross-validation + briefing

## Absolute Principles

1. Final judgment belongs to the human user.
2. The system must not issue buy or sell recommendations.
3. Agent outputs may commit an observation, but must include reasoning when the output is judgmental.
4. The project values consistency and traceability over aggressive feature expansion.
5. Claude-specific assets remain the current source material for runtime philosophy and pipeline contracts until replaced by vendor-neutral docs.

## Runtime Pipeline Overview

- NI pipeline: NC -> NF -> NT
- TA pipeline: TC -> TR
- Cross-validation: CA
- Briefing pipeline: IS -> IA -> IP

The live backend implementation is centered in:

- [PipelineService.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/PipelineService.java:59)
- [PipelineScheduler.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/PipelineScheduler.java:15)

## Source Of Truth

Until a vendor-neutral documentation layer is completed, use these files as primary reference:

1. [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md)
2. [.claude/config/PIPELINE.md](/C:/Users/jm991/Desktop/project/Signal/.claude/config/PIPELINE.md)
3. [.claude/agents/runtime](/C:/Users/jm991/Desktop/project/Signal/.claude/agents/runtime)
4. [docs/meta-layer-charter.md](/C:/Users/jm991/Desktop/project/Signal/docs/meta-layer-charter.md)

For actual behavior, prefer code over speculative framework documents:

- [src/main/java](/C:/Users/jm991/Desktop/project/Signal/src/main/java)
- [frontend/src](/C:/Users/jm991/Desktop/project/Signal/frontend/src)

## Current Gaps

- Some `.claude` assets still carry ATStudio-era wording or assumptions even after the main cleanup pass.
- A few hook templates and legacy config comments still contain garbled text and should be treated as maintenance targets, not source-of-truth guidance.
- There is no completed vendor-neutral documentation layer yet.

## Runtime And Test Separation

- Production-like local server behavior uses `src/main/resources/application.properties`.
- The weekday 9:00 scheduler remains part of the normal server profile and is not changed by test setup.
- Automated tests use the `test` profile with an isolated in-memory database and dummy external integration settings.
- Do not ask to restart the server just because `./gradlew test` ran. Test execution and the scheduled runtime path are intentionally separated.

## Working Rules For Claude And Codex

- Do not mechanically copy `.claude` into `.codex`.
- Keep `.claude` as-is for now and treat it as legacy-but-active project context.
- Use Codex as a thin operational layer that reads existing project sources and works against the real codebase.
- When `.claude` documents conflict with code, trust the code.
- When `.claude` documents conflict with missing files, treat the rule as incomplete rather than authoritative.

## Migration Posture

This repository is in a bridging state, not a full migration state.

- Short term: maintain Claude assets and add Codex entrypoints.
- Mid term: repair encoding, replace missing references with real docs, and extract vendor-neutral standards.
- Long term: reduce dependence on Claude-specific wrapper files after common project documentation is stable.
