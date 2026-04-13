# SIGNAL Migration Status

## Current Posture

SIGNAL is not in full Claude-to-Codex migration.
It is in a bridge state.

- Claude assets remain in place as active project context.
- Codex uses a thin wrapper layer instead of a full orchestration copy.
- The project now prefers real repository files over missing ATStudio-derived references.

## Completed In This Pass

- Added [AGENTS.md](/C:/Users/jm991/Desktop/project/Signal/AGENTS.md) as a shared project entrypoint.
- Added [`.codex/project.md`](/C:/Users/jm991/Desktop/project/Signal/.codex/project.md) as a Codex-facing wrapper.
- Added UTF-8 repository defaults through [`.gitattributes`](/C:/Users/jm991/Desktop/project/Signal/.gitattributes) and [`.editorconfig`](/C:/Users/jm991/Desktop/project/Signal/.editorconfig).
- Reduced stale `.claude/config/*` references to missing `docs/standards/*`, `docs/policies/*`, and `docs/architecture/*` files.
- Isolated backend tests from local MySQL and normal runtime scheduling through [`application-test.properties`](/C:/Users/jm991/Desktop/project/Signal/src/test/resources/application-test.properties).

## What Remains Intentionally Unchanged

- `.claude` is still the legacy-but-active source of runtime philosophy.
- `CLAUDE.md` and `.claude/config/PIPELINE.md` are still core reference files.
- The weekday 9:00 scheduler remains part of the normal server profile.
- Manual local server operation remains separate from automated test execution.

## Known Gaps

- Some `.claude` build-layer assets and skill references still reflect ATStudio-era wording.
- The repo still lacks a full vendor-neutral docs tree.
- Some hook templates and legacy config comments still contain garbled text.

## Recommended Next Steps

1. Review `.claude/skills/*` and `afterSource*` files for remaining ATStudio-only assumptions.
2. Clean up garbled hook/config text that still causes operator confusion.
3. Add broader automated tests beyond the current context-load coverage.
