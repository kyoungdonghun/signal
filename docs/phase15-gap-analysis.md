# Phase 1.5 Gap Analysis

## Purpose

This document compares the current SIGNAL repository against the Phase 1.5 philosophy defined in:

- [docs/meta-layer-charter.md](/C:/Users/jm991/Desktop/project/Signal/docs/meta-layer-charter.md)
- [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md)
- [.codex/project.md](/C:/Users/jm991/Desktop/project/Signal/.codex/project.md)

The goal is to clarify:

- what already exists
- what is missing
- what is implemented only partially
- what should be fixed next

## Executive Summary

SIGNAL already has the early building blocks of Phase 1.5:

- run storage
- scheduled outcome filling
- user commit input
- track-record UI entrypoint
- `level_commit` production in TR

However, the actual Phase 1.5 meta layer is not complete yet.

What exists today is closer to:

- run history
- simple outcome tracking
- user judgment capture

What does **not** exist yet is the real Phase 1.5 core:

- charter-level verdict tracking
- charter-level drift analysis
- charter-level AI vs user calibration analysis
- charter-level reasoning quality verification

Current interpretation:

- anything already implemented before the 100-run threshold should be treated as bootstrap instrumentation
- current verdict, drift, calibration, and reasoning checks are temporary observational rules, not final scoring doctrine
- current calibration UI/API should be read as a 1-day bootstrap proxy, not a final 1-week scoring model

## Reference Questions From The Charter

The charter defines four questions:

1. Track Record
2. Drift
3. Calibration
4. Quality

Current implementation status:

| Area | Status | Notes |
|------|--------|------|
| Track Record | Partial | Meta track-record aggregation now exists, but remains a bootstrap dashboard |
| Drift | Partial | A minimal previous-run comparison exists, but not charter-level drift analysis |
| Calibration | Partial | Coarse AI vs user comparison exists, but only as bootstrap instrumentation |
| Reasoning Quality | Partial | A heuristic checker exists, but not a final validation or review workflow |

## What Already Exists

### 1. Run Persistence

Current run storage already captures useful baseline data.

Relevant files:

- [PipelineService.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/PipelineService.java)
- [PipelineRunEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/PipelineRunEntity.java)

Stored fields already include:

- `run_id`
- `ticker`
- `executed_at`
- `stability`
- `tr_confidence`
- `cross_result`
- `news_direction`
- market indicator values
- `level_commits`
- `full_result`

This is a strong base for Phase 1.5.

### 2. Outcome Tracking

Current outcome tracking exists and is scheduled.

Relevant files:

- [OutcomeService.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/OutcomeService.java)
- [OutcomeRecordEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/OutcomeRecordEntity.java)
- [PipelineScheduler.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/PipelineScheduler.java)

Current tracked values:

- `price_1d_after`
- `price_1w_after`
- `price_change_pct`

This supports simple post-hoc market observation and coarse calibration, but not final charter-level verdicts by itself.

### 3. User Commit Capture

User-side commit capture already exists.

Relevant files:

- [UserCommitEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/UserCommitEntity.java)
- [UserCommitController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/UserCommitController.java)
- [CommitForm.tsx](/C:/Users/jm991/Desktop/project/Signal/frontend/src/pages/CommitForm.tsx)

Current stored values:

- `user_cross_result`
- `user_note`
- `agreed_with_ai`
- `user_level_view`
- `committed_at`

This is enough to start user-side meta-layer work, but not enough for clean calibration logic yet.

### 4. Track Record UI Entry Point

A track-record page already exists.

Relevant file:

- [TrackRecordPage.tsx](/C:/Users/jm991/Desktop/project/Signal/frontend/src/pages/TrackRecordPage.tsx)

This now functions as a bootstrap meta-layer dashboard, but it is not yet a full Q1-Q4 charter implementation.

## Main Gaps

### 1. `level_commit` Verdict Model Is Still Too Thin

This is the largest gap.

The charter explicitly says that Phase 1.5 scoring should be built around verifiable level observations, not only around coarse directional labels.

Relevant references:

- [docs/meta-layer-charter.md](/C:/Users/jm991/Desktop/project/Signal/docs/meta-layer-charter.md)
- [TrService.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/TrService.java)

Current limitation:

- `TrService` produces `level_commit`
- `PipelineRunEntity` stores `level_commits`
- a later pass now evaluates whether that level held, broke, or remained ambiguous
- but the current logic is still only a bootstrap rule using simple 1d/1w observed prices

Result:

- the core scoring unit now exists, but not yet at the depth required by the charter

### 2. Drift Analysis Is Still Minimal

The charter asks whether repeated judgments for the same ticker drift over time.

Current limitation:

- backend now compares recent runs for the same ticker
- meta APIs expose drift
- frontend shows a recent drift panel
- but drift is still only a one-step comparison, not a broader reasoning-aware pattern analysis

### 3. Calibration Is Still Coarse

The charter requires symmetric comparison between AI and user commitments.

Current limitation:

- user commits are stored
- AI commitments are stored
- a bootstrap service now compares which side was more accurate under a coarse 1-day outcome proxy
- but calibration is not yet condition-aware or statistically meaningful

Deferred follow-up:

- add explicit 1-week calibration fields instead of reusing the current 1-day proxy

### 4. Reasoning Quality Verification Is Still Heuristic

The pipeline contract already defines reasoning quality rules.

Relevant file:

- [.claude/config/PIPELINE.md](/C:/Users/jm991/Desktop/project/Signal/.claude/config/PIPELINE.md)

Current limitation:

- a heuristic checker now evaluates the three reasoning conditions
- the UI exposes weak reasoning counts
- but there is still no formal review workflow or mature scoring rubric

### 5. Ambiguous User Commit Policy

Current backend design originally allowed multiple commits for the same run.

Relevant files:

- [UserCommitEntity.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/domain/UserCommitEntity.java)
- [UserCommitRepository.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/repository/UserCommitRepository.java)
- [CommitForm.tsx](/C:/Users/jm991/Desktop/project/Signal/frontend/src/pages/CommitForm.tsx)

Current direction:

- Phase 1.5 now adopts `one commit per run` as the working policy
- backend should upsert by `run_id`
- hard DB uniqueness can be added after confirming existing data cleanup needs

Deferred follow-up:

- inspect existing duplicate `user_commits`
- keep the latest row per `run_id`
- then add a DB-level unique constraint on `run_id`

### 6. Run Summary API Is Too Thin For Phase 1.5

Relevant file:

- [RunController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/RunController.java)

Current summaries omit:

- `level_commits`
- reasoning details
- 1w result in list views
- meta verdict fields
- AI vs user comparison data

This blocks frontend meta-layer development.

### 7. Documentation State Is Slightly Misaligned

Current state mismatch:

- [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md) still marks Phase 1.5 as pending
- but some Phase 1.5 bootstrap pieces already exist in code

This should be clarified as either:

- Phase 1.5 bootstrap
- or Phase 1 support tooling

## What Should Be Improved

### Documentation

Improve:

- Phase 1.5 status wording
- missing memory references
- explicit distinction between philosophy and implemented scope

Recommended actions:

1. Update [CLAUDE.md](/C:/Users/jm991/Desktop/project/Signal/CLAUDE.md) to describe current status more precisely.
2. Replace or remove references to missing `memory/*` files from [docs/meta-layer-charter.md](/C:/Users/jm991/Desktop/project/Signal/docs/meta-layer-charter.md).
3. Keep this gap analysis as the working baseline for the next implementation step.

### Backend And Data Model

Improve:

- verdict storage for `level_commit`
- run comparison support
- calibration support
- reasoning review support

Recommended actions:

1. Add a dedicated verdict model for `level_commit`.
2. Clarify `user_commit` uniqueness policy.
3. Add meta-layer service and API endpoints instead of overloading `/api/runs`.

### Frontend

Improve:

- track-record page should become a meta-layer dashboard
- add sections for:
  - AI vs user comparison
  - level verdicts
  - drift
  - reasoning review

## Recommended Priority

1. Define `user_commit` policy.
2. Define `level_commit` verdict data model.
3. Add backend meta-layer aggregation APIs.
4. Redesign the track-record page around Q1-Q4.
5. Align status documentation.

## Things To Avoid

Do not do these yet:

- gamified rankings or scoreboards
- marketing-style accuracy percentages
- speculative calibration logic without enough real runs
- full Phase 1.5 rollout in one refactor

## Decision Request

Before implementation starts, confirm these two decisions:

1. `user_commit` policy:
   - one commit per run
   - or revision history

2. Phase 1.5 status wording:
   - keep as pending
   - or mark as bootstrap / partial
