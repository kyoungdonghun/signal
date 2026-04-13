# Phase 1.5 Data Model Proposal

## Purpose

This document proposes the minimum backend and database changes needed to support the Phase 1.5 meta layer.

It is intentionally conservative.

The goal is not to complete the whole meta layer at once.
The goal is to create a clean foundation for:

- track record
- level-based verdicts
- drift
- calibration
- reasoning quality review

This proposal must be read under the charter boundary:

- before the 100-run threshold, these structures are bootstrap instrumentation
- they are not the final scoring or calibration model

## Current Model

Current core persistence objects:

- [PipelineRunEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/PipelineRunEntity.java)
- [OutcomeRecordEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/OutcomeRecordEntity.java)
- [UserCommitEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/UserCommitEntity.java)

Current strengths:

- run-level storage already exists
- outcome tracking already exists
- user-side commit capture already exists
- `level_commits` already exist as raw JSON

Current weakness:

- the most important post-hoc scoring unit is not normalized or evaluated

## Recommended Policy Decisions

### 1. User Commit Policy

Recommended:

- one user commit per run

Reason:

- simpler calibration
- simpler UI
- clearer symmetry with AI commit at one run boundary

Implication:

- save behavior should become upsert by `run_id`
- hard DB uniqueness on `run_id` can be added after confirming existing data cleanup needs

### 2. Verdict Granularity

Recommended:

- evaluate `level_commit` separately from broad `cross_result`

Reason:

- the charter explicitly treats level observation as the fairer scoring unit
- directional labels are too coarse

## Proposed Additions

### A. New Table: `level_commit_verdicts`

Recommended fields:

| Field | Type | Purpose |
|------|------|------|
| `id` | bigint | primary key |
| `run_id` | varchar | source run |
| `ticker` | varchar | ticker |
| `level_value` | double | committed level |
| `level_type` | varchar | `support` or `resistance` |
| `basis` | varchar | `MA20`, `MA60`, `price_action`, etc. |
| `description` | text | copied/verbatim observation context |
| `horizon` | varchar | `1d`, `1w` |
| `verdict` | varchar | `held`, `broken`, `untested`, `ambiguous` |
| `observed_price` | double | observed reference price at evaluation |
| `checked_at` | datetime | when verdict was computed |
| `notes` | text | optional explanation |

Why separate table:

- one run can produce multiple level commits
- one level commit may be checked at multiple horizons

### B. Optional Extension: `outcome_records`

Keep current fields, but treat this table as:

- coarse market outcome storage

Do not overload it with:

- multiple verdict rows
- horizon-by-level judgment logic

That logic fits better in `level_commit_verdicts`.

### C. Adjust `user_commits`

Recommended changes:

- add unique constraint on `run_id`
- optionally add `updated_at`

If the user later wants revision history, create a separate history table instead of making the primary table ambiguous.

## Proposed Backend Structure

### New Entities

- `LevelCommitVerdictEntity`

### New Repository

- `LevelCommitVerdictRepository`

### New Services

- `MetaLayerService`
  - track record summary
  - drift summary
  - calibration summary
  - reasoning review summary

- `LevelCommitVerdictService`
  - evaluate raw `level_commit` records into verdict rows

### New Controller

- `MetaLayerController`

Recommended endpoints:

- `GET /api/meta/track-record`
- `GET /api/meta/drift`
- `GET /api/meta/calibration`
- `GET /api/meta/reasoning-quality`
- `GET /api/meta/stats`

## API Direction

### Keep Existing APIs For Raw Data

Keep:

- `/api/runs`
- `/api/runs/{runId}`
- `/api/user-commits`

These should remain raw operational endpoints.

### Add Separate Meta APIs

Do not overload `/api/runs` with all meta-layer logic.

Reason:

- raw runs and meta judgments are different concerns
- it keeps frontend migration safer
- it reduces accidental contract breakage

## Frontend Impact

The current [TrackRecordPage.tsx](/C:/Users/jm991/Desktop/project/Signal/frontend/src/pages/TrackRecordPage.tsx) should eventually consume meta APIs, not only raw run lists.

Recommended future view sections:

1. Track record overview
2. Level commit verdict list
3. Drift comparison
4. AI vs user calibration
5. Reasoning quality flags

## Implementation Sequence

Recommended order:

1. Decide `user_commit` policy.
2. Add `level_commit_verdicts`.
3. Add verdict evaluation service.
4. Add meta aggregation service.
5. Add meta controller.
6. Update frontend track-record page.

## What Not To Model Yet

Avoid adding these too early:

- scoreboards
- user ranking or badges
- overly complex confidence math
- speculative drift formulas before real run volume exists

## Approval Checklist

Please confirm:

1. `user_commits` should be one-per-run.
2. `level_commit_verdicts` should be a separate table.
3. meta-layer APIs should be separate from `/api/runs`.
