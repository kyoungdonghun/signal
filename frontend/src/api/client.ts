import type { RunSummary, RunDetail, UserCommitRequest } from '../types';

const BASE = 'http://localhost:8080/api';

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`);
  if (!res.ok) throw new Error(`GET ${path} failed: ${res.status}`);
  return res.json();
}

async function post<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(`POST ${path} failed: ${res.status}`);
  return res.json();
}

export const api = {
  getLatestRuns: () => get<RunSummary[]>('/runs/latest'),
  getAllRuns: () => get<RunSummary[]>('/runs'),
  getRunsByTicker: (ticker: string) => get<RunSummary[]>(`/runs?ticker=${encodeURIComponent(ticker)}`),
  getRunDetail: (runId: string) => get<RunDetail>(`/runs/${runId}`),
  saveUserCommit: (req: UserCommitRequest) => post('/user-commits', req),
  getUserCommits: (runId: string) => get<{ userCrossResult: string; committedAt: string }[]>(`/user-commits/${runId}`),
};
