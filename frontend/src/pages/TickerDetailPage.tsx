import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type { RunSummary } from '../types';
import styles from './TickerDetailPage.module.css';

const TICKER_NAMES: Record<string, string> = {
  '005930.KS': '삼성전자',
  '005380.KS': '현대차',
  'TSLA': '테슬라',
};

export function TickerDetailPage() {
  const { ticker } = useParams<{ ticker: string }>();
  const navigate = useNavigate();
  const [runs, setRuns] = useState<RunSummary[]>([]);
  const [committed, setCommitted] = useState<Record<string, boolean>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!ticker) return;
    api.getRunsByTicker(ticker)
      .then(async data => {
        setRuns(data);
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
  }, [ticker]);

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;

  const name = TICKER_NAMES[ticker ?? ''] ?? ticker;
  const latest = runs[0];

  return (
    <div className={styles.page}>
      <div className={styles.topBar}>
        <button className={styles.backBtn} onClick={() => navigate('/')}>← 목록</button>
      </div>

      <div className={styles.header}>
        <div>
          <h1 className={styles.heading}>{name}</h1>
          <p className={styles.tickerCode}>{ticker}</p>
        </div>
        {latest && (
          <div className={styles.latestBadges}>
            <StatusBadge value={latest.crossResult || 'UNCERTAIN'} />
            <StatusBadge value={latest.stability || 'unknown'} />
          </div>
        )}
      </div>

      {runs.length === 0 ? (
        <div className={styles.empty}>분석 기록이 없습니다.</div>
      ) : (
        <div className={styles.list}>
          {runs.map(run => {
            const change = run.priceChangePct;
            const didCommit = committed[run.runId];

            return (
              <div
                key={run.runId}
                className={styles.row}
                onClick={() => navigate(`/ticker/${encodeURIComponent(ticker ?? '')}/${run.runId}`)}
              >
                <div className={styles.rowDate}>
                  <span className={styles.date}>
                    {new Date(run.executedAt).toLocaleDateString('ko-KR', {
                      month: 'short', day: 'numeric',
                    })}
                  </span>
                  <span className={styles.time}>
                    {new Date(run.executedAt).toLocaleTimeString('ko-KR', {
                      hour: '2-digit', minute: '2-digit',
                    })}
                  </span>
                </div>

                <div className={styles.rowBadges}>
                  <StatusBadge value={run.crossResult || 'UNCERTAIN'} />
                  <StatusBadge value={run.stability || 'unknown'} />
                </div>

                <div className={styles.rowMetrics}>
                  <span className={styles.price}>{run.price?.toLocaleString()}</span>
                  <span className={styles.rsi}>RSI {run.rsi?.toFixed(1)}</span>
                </div>

                <div className={styles.rowRight}>
                  {change != null ? (
                    <span className={change >= 0 ? styles.changePos : styles.changeNeg}>
                      {change >= 0 ? '+' : ''}{change.toFixed(2)}%
                    </span>
                  ) : (
                    <span className={styles.changeDash}>—</span>
                  )}
                  <span className={`${styles.commitBadge} ${didCommit ? styles.commitDone : styles.commitNone}`}>
                    {didCommit ? '✓' : '○'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
