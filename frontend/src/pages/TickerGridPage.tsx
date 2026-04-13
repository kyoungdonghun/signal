import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { SchedulerLog } from '../api/client';
import { StatusSummary } from '../components/StatusBadge';
import type { RunSummary } from '../types';
import styles from './TickerGridPage.module.css';

const TICKER_NAMES: Record<string, string> = {
  '005930.KS': '삼성전자',
  '005380.KS': '현대차',
  'TSLA': '테슬라',
};

const CROSS_BG: Record<string, string> = {
  CONFLICT: 'var(--color-conflict-bg)',
  ALIGNED_BULLISH: 'var(--color-bullish-bg)',
  ALIGNED_BEARISH: 'var(--color-bearish-bg)',
  UNCERTAIN: 'var(--color-uncertain-bg)',
};

const CROSS_BORDER: Record<string, string> = {
  CONFLICT: 'var(--color-conflict)',
  ALIGNED_BULLISH: 'var(--color-bullish)',
  ALIGNED_BEARISH: 'var(--color-bearish)',
  UNCERTAIN: 'var(--color-border)',
};

function timeSince(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins}분 전`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}시간 전`;
  const days = Math.floor(hours / 24);
  if (days === 1) return '어제';
  return `${days}일 전`;
}

function isToday(iso: string): boolean {
  return new Date(iso).toLocaleDateString('ko-KR') === new Date().toLocaleDateString('ko-KR');
}

const PHASE_TARGET = 100;

// 마지막 scheduler 실행이 평일 기준 1일 이상 지났는지 확인
function detectGap(logs: SchedulerLog[]): string | null {
  const successful = logs.filter(l => l.status === 'SUCCESS' || l.status === 'PARTIAL');
  if (successful.length === 0) return null;
  const lastRun = new Date(successful[0].startedAt);
  const now = new Date();
  const diffMs = now.getTime() - lastRun.getTime();
  const diffDays = diffMs / (1000 * 60 * 60 * 24);
  if (diffDays >= 2) {
    return `마지막 실행: ${lastRun.toLocaleDateString('ko-KR')} — ${Math.floor(diffDays)}일 경과`;
  }
  return null;
}

export function TickerGridPage() {
  const navigate = useNavigate();
  const [runs, setRuns] = useState<RunSummary[]>([]);
  const [committed, setCommitted] = useState<Record<string, boolean>>({});
  const [totalRuns, setTotalRuns] = useState<number | null>(null);
  const [gapWarning, setGapWarning] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.getLatestRuns(),
      api.getRunStats(),
      api.getSchedulerLogs(),
    ]).then(async ([data, stats, logs]) => {
        setRuns(data);
        setTotalRuns(stats.totalRuns);
        setGapWarning(detectGap(logs));
        const entries = await Promise.all(
          data.map(async r => {
            try {
              const commit = await api.getUserCommit(r.runId);
              return [r.runId, commit != null] as [string, boolean];
            } catch {
              return [r.runId, false] as [string, boolean];
            }
          })
        );
        setCommitted(Object.fromEntries(entries));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;
  if (runs.length === 0) return (
    <div className={styles.empty}>
      아직 분석 데이터가 없습니다.<br />
      서버에서 파이프라인을 실행해주세요.
    </div>
  );

  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long',
  });

  const pct = totalRuns != null ? Math.min((totalRuns / PHASE_TARGET) * 100, 100) : null;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.heading}>시장 현황</h1>
        <p className={styles.subheading}>{today}</p>
      </div>

      {gapWarning && (
        <div className={styles.gapWarning}>
          ⚠ 서버 가동 간격 감지 — {gapWarning}
        </div>
      )}

      {pct != null && (
        <div className={styles.progressWrap}>
          <div className={styles.progressLabel}>
            Phase 1.5 진입 조건: <strong>{totalRuns}</strong> / {PHASE_TARGET}건
          </div>
          <div className={styles.progressBar}>
            <div className={styles.progressFill} style={{ width: `${pct}%` }} />
          </div>
        </div>
      )}

      <div className={styles.grid}>
        {runs.map(run => {
          const cr = run.crossResult || 'UNCERTAIN';
          const hasToday = isToday(run.executedAt);
          const didCommit = committed[run.runId];

          return (
            <div
              key={run.runId}
              className={styles.card}
              style={{
                borderTop: `4px solid ${CROSS_BORDER[cr] ?? 'var(--color-border)'}`,
                background: hasToday
                  ? CROSS_BG[cr] ?? 'var(--color-surface)'
                  : 'var(--color-surface)',
              }}
              onClick={() => navigate(`/ticker/${encodeURIComponent(run.ticker)}`)}
            >
              <div className={styles.cardTop}>
                <div>
                  <p className={styles.tickerName}>{TICKER_NAMES[run.ticker] ?? run.ticker}</p>
                  <p className={styles.tickerCode}>{run.ticker}</p>
                </div>
                {!hasToday && <span className={styles.staleTag}>오늘 데이터 없음</span>}
              </div>

              <StatusSummary
                stability={run.stability || 'unknown'}
                newsDirection={run.newsDirection || ''}
                crossResult={cr}
              />

              <div className={styles.metrics}>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>현재가</span>
                  <span className={styles.metricValue}>{run.price?.toLocaleString()}</span>
                </div>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>RSI(14)</span>
                  <span className={styles.metricValue}>{run.rsi?.toFixed(1)}</span>
                </div>
              </div>

              <div className={styles.cardFooter}>
                <span className={styles.timeAgo}>{timeSince(run.executedAt)}</span>
                {hasToday && (
                  <span className={`${styles.commitDot} ${didCommit ? styles.commitDone : styles.commitPending}`}>
                    {didCommit ? '✓ commit' : '○ 미commit'}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
