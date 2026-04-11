import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type { RunSummary } from '../types';
import styles from './TrackRecordPage.module.css';

export function TrackRecordPage() {
  const [runs, setRuns] = useState<RunSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getAllRuns()
      .then(setRuns)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;
  if (runs.length === 0) return <div className={styles.empty}>기록이 없습니다.</div>;

  return (
    <div className={styles.page}>
      <div>
        <h1 className={styles.heading}>트랙레코드</h1>
        <p className={styles.subheading}>
          AI의 판단과 실제 결과를 시간 순으로 기록합니다. 기록이 있어야 경쟁이 가능합니다.
        </p>
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Ticker</th>
            <th>실행일</th>
            <th>기술적 상태</th>
            <th>교차 판단</th>
            <th className={styles.thRight}>현재가</th>
            <th className={styles.thRight}>RSI</th>
            <th className={styles.thRight}>1일 후 변화</th>
          </tr>
        </thead>
        <tbody>
          {runs.map(run => {
            const change = run.priceChangePct;
            const changeClass = change == null
              ? styles.changeDash
              : change >= 0 ? styles.changePos : styles.changeNeg;

            return (
              <tr key={run.runId}>
                <td className={styles.ticker}>{run.ticker}</td>
                <td className={styles.date}>
                  {new Date(run.executedAt).toLocaleDateString('ko-KR')}
                </td>
                <td>
                  <StatusBadge value={run.stability || 'unknown'} />
                </td>
                <td>
                  <StatusBadge value={run.crossResult || 'UNCERTAIN'} />
                </td>
                <td style={{ textAlign: 'right' }}>
                  {run.price?.toLocaleString()}
                </td>
                <td style={{ textAlign: 'right' }}>
                  {run.rsi?.toFixed(1)}
                </td>
                <td className={`${changeClass}`} style={{ textAlign: 'right' }}>
                  {change != null
                    ? `${change >= 0 ? '+' : ''}${change.toFixed(2)}%`
                    : '—'}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
