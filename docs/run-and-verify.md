# Run And Verify

## Purpose

This document explains how to run SIGNAL locally and how to verify it without confusing:

- the normal scheduled server path
- manual API triggering
- automated test execution

## Runtime Modes

### Normal Server Mode

Use the normal Spring profile when you want the application to behave like the local working server.

- Config source: [src/main/resources/application.properties](/C:/Users/jm991/Desktop/project/Signal/src/main/resources/application.properties)
- Includes:
  - local MySQL settings
  - real watchlist entries
  - weekday `09:00` scheduler
  - real Claude API settings via environment variables

The scheduler is implemented in [PipelineScheduler.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/service/PipelineScheduler.java:15).

### Test Mode

Use the test profile only for automated verification.

- Config source: [src/test/resources/application-test.properties](/C:/Users/jm991/Desktop/project/Signal/src/test/resources/application-test.properties)
- Includes:
  - H2 in-memory database
  - dummy Claude settings
  - test-only watchlist

Running tests must not be treated as a reason to restart the normal app server.

## Common Commands

### Backend

Run automated backend tests:

```powershell
./gradlew test
```

Start the backend server in normal mode:

```powershell
./gradlew bootRun
```

### Frontend

Start the frontend dev server:

```powershell
npm run dev
```

Build the frontend:

```powershell
npm run build
```

Frontend scripts live in [frontend/package.json](/C:/Users/jm991/Desktop/project/Signal/frontend/package.json).

## Manual Verification Routes

### Trigger The Full Watchlist Scheduler Path

Endpoint:

- `POST /api/pipeline/scheduler/trigger`

Controller:

- [PipelineController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/PipelineController.java:31)

### Trigger A Single Manual Pipeline Run

Endpoint:

- `POST /api/pipeline/run`

Body fields:

- `ticker`
- `rssFeedUrl`
- `rssFeedSource`

### Inspect Stored Results

- Latest runs: `GET /api/runs/latest`
- Run list: `GET /api/runs`
- Run detail: `GET /api/runs/{runId}`
- Run stats: `GET /api/runs/stats`

Controller:

- [RunController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/RunController.java:14)

### Inspect Scheduler Logs

- `GET /api/scheduler-logs`

Controller:

- [SchedulerLogController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/SchedulerLogController.java:12)

### Inspect Or Save User Commit Data

- Save commit: `POST /api/user-commits`
- Get commit history for a run: `GET /api/user-commits/{runId}`
- Get all commits: `GET /api/user-commits`

Controller:

- [UserCommitController.java](/C:/Users/jm991/Desktop/project/Signal/src/main/java/com/kyoung/signal/controller/UserCommitController.java:11)

## Verification Guidance

- If `./gradlew test` passes, it means the app context and persistence layer work in isolated test mode.
- It does not prove that the normal MySQL-backed server is currently running.
- If the normal server is already up, do not restart it just because tests were executed.
- If you need to verify scheduled or manual pipeline behavior, use the API routes above against the running backend.
