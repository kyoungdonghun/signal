# SIGNAL Frontend

## Purpose

This frontend is the SIGNAL viewer layer for:

- latest ticker status
- per-ticker run history
- run detail inspection
- user commit and track-record flows

It is a React + TypeScript + Vite application and talks to the Spring backend at `http://localhost:8080/api` by default.

## Main Routes

- `/` : ticker grid
- `/ticker/:ticker` : ticker detail
- `/ticker/:ticker/:runId` : run detail
- `/track-record` : track-record view

Route wiring lives in [src/App.tsx](/C:/Users/jm991/Desktop/project/Signal/frontend/src/App.tsx).

## Development

Install dependencies:

```powershell
npm install
```

Start the frontend dev server:

```powershell
npm run dev
```

Build for production:

```powershell
npm run build
```

Run lint:

```powershell
npm run lint
```

## Backend Dependency

This frontend expects the backend API to be available locally.
Base API configuration lives in [src/api/client.ts](/C:/Users/jm991/Desktop/project/Signal/frontend/src/api/client.ts).

Default API base:

```text
http://localhost:8080/api
```

If the backend is not running, pages that fetch runs, scheduler logs, or user commits will fail to load data.

## Notes

- The frontend is a viewer and interaction layer. Core scheduling and pipeline execution remain on the backend.
- UI text includes Korean market-facing labels, so repository UTF-8 settings should be preserved.
