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

export interface UserCommit {
  runId: string;
  ticker: string;
  userCrossResult: string;
  userNote: string;
  userLevelView: string;
  agreedWithAi: boolean;
  committedAt: string;
  updatedAt?: string | null;
}

export interface SchedulerLog {
  id: number;
  startedAt: string;
  completedAt: string | null;
  tickersAttempted: string;
  tickersSucceeded: string | null;
  tickersFailed: string | null;
  status: string;
}

export interface LevelCommitVerdict {
  runId: string;
  ticker: string;
  levelValue: number;
  levelType: string;
  basis: string;
  description: string;
  horizon: string;
  verdict: string;
  observedPrice: number | null;
  checkedAt: string | null;
  notes: string;
}

export interface TrackRecordRow {
  runId: string;
  ticker: string;
  executedAt: string;
  stability: string;
  crossResult: string;
  newsDirection: string;
  price?: number | null;
  rsi?: number | null;
  price1dAfter?: number | null;
  price1wAfter?: number | null;
  priceChangePct?: number | null;
  levelVerdictCount: number;
  heldCount: number;
  brokenCount: number;
  reasoningStrongCount: number;
  reasoningWeakCount: number;
  driftState: string;
  driftFromRunId?: string | null;
  previousCrossResult?: string;
  previousStability?: string;
  crossChanged: boolean;
  stabilityChanged: boolean;
  calibration: CalibrationRow;
  userCommit?: UserCommit | null;
}

export interface DriftRow {
  ticker: string;
  status: string;
  recentRunId?: string | null;
  previousRunId?: string | null;
  currentCrossResult?: string;
  previousCrossResult?: string;
  currentStability?: string;
  previousStability?: string;
  crossChanged: boolean;
  stabilityChanged: boolean;
  driftState: string;
}

export interface CalibrationRow {
  runId: string;
  ticker: string;
  executedAt: string;
  marketOutcome: string;
  aiVerdict: string;
  userVerdict: string;
  comparison: string;
}

export interface ReasoningQualityRow {
  path: string;
  status: string;
  score: number;
  hasSpecificReference: boolean;
  hasConnection: boolean;
  hasFalsifiability: boolean;
  note: string;
}

export const api = {
  getLatestRuns: () => get<RunSummary[]>('/runs/latest'),
  getAllRuns: () => get<RunSummary[]>('/runs'),
  getRunsByTicker: (ticker: string) => get<RunSummary[]>(`/runs?ticker=${encodeURIComponent(ticker)}`),
  getRunDetail: (runId: string) => get<RunDetail>(`/runs/${runId}`),
  getRunStats: () => get<{ totalRuns: number }>('/runs/stats'),
  saveUserCommit: (req: UserCommitRequest) => post('/user-commits', req),
  getUserCommit: (runId: string) => get<UserCommit | null>(`/user-commits/${runId}`),
  getAllUserCommits: () => get<UserCommit[]>('/user-commits'),
  getSchedulerLogs: () => get<SchedulerLog[]>('/scheduler-logs'),
  getLevelVerdicts: (runId: string) => get<LevelCommitVerdict[]>(`/meta/level-verdicts/${runId}`),
  getTrackRecord: () => get<TrackRecordRow[]>('/meta/track-record'),
  getDrift: () => get<DriftRow[]>('/meta/drift'),
  getCalibration: () => get<CalibrationRow[]>('/meta/calibration'),
  getReasoningQuality: (runId: string) => get<ReasoningQualityRow[]>(`/meta/reasoning-quality/${runId}`),
};
