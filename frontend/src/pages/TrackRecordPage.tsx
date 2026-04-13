import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { CalibrationRow, DriftRow, TrackRecordRow } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import styles from './TrackRecordPage.module.css';

const LABEL: Record<string, string> = {
  ALIGNED_BULLISH: '강세',
  ALIGNED_BEARISH: '약세',
  CONFLICT: '충돌',
  UNCERTAIN: '불확실',
};

const CALIBRATION_LABEL: Record<string, string> = {
  both_correct: '둘 다 적중',
  ai_leads: 'AI 우위',
  user_leads: '사용자 우위',
  both_incorrect: '둘 다 빗나감',
  ai_only: 'AI만 판정 가능',
  unresolved: '판정 보류',
};

const MARKET_LABEL: Record<string, string> = {
  bullish: '상승',
  bearish: '하락',
  flat: '보합',
  unknown: '미집계',
};

function driftClassName(driftState: string) {
  if (driftState === 'stable') return styles.driftStable;
  if (driftState === 'medium') return styles.driftMedium;
  if (driftState === 'high') return styles.driftHigh;
  return styles.driftInsufficient;
}

function calibrationClassName(comparison: string) {
  if (comparison === 'both_correct') return styles.driftStable;
  if (comparison === 'user_leads') return styles.driftMedium;
  if (comparison === 'ai_leads') return styles.driftHigh;
  return styles.driftInsufficient;
}

export function TrackRecordPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState<TrackRecordRow[]>([]);
  const [driftRows, setDriftRows] = useState<DriftRow[]>([]);
  const [calibrationRows, setCalibrationRows] = useState<CalibrationRow[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([api.getTrackRecord(), api.getDrift(), api.getCalibration()])
      .then(([trackRecord, drift, calibration]) => {
        setRows(trackRecord);
        setDriftRows(drift);
        setCalibrationRows(calibration);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;
  if (rows.length === 0) return <div className={styles.empty}>기록이 없습니다.</div>;

  return (
    <div className={styles.page}>
      <div>
        <h1 className={styles.heading}>트랙 레코드</h1>
        <p className={styles.subheading}>
          AI 판단, 사용자 commit, 사후 결과를 같은 run 기준으로 모아 보는 메타 레이어 관측 화면입니다.
        </p>
      </div>

      <div className={styles.summaryStrip}>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>전체 Run</span>
          <strong className={styles.summaryValue}>{rows.length}</strong>
        </div>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>사용자 Commit</span>
          <strong className={styles.summaryValue}>{rows.filter(r => r.userCommit).length}</strong>
        </div>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>레벨 판정</span>
          <strong className={styles.summaryValue}>{rows.reduce((sum, r) => sum + r.levelVerdictCount, 0)}</strong>
        </div>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>드리프트 경고</span>
          <strong className={styles.summaryValue}>
            {driftRows.filter(r => r.driftState === 'high' || r.driftState === 'medium').length}
          </strong>
        </div>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>약한 Reasoning</span>
          <strong className={styles.summaryValue}>{rows.reduce((sum, r) => sum + r.reasoningWeakCount, 0)}</strong>
        </div>
        <div className={styles.summaryCard}>
          <span className={styles.summaryLabel}>사용자 우위</span>
          <strong className={styles.summaryValue}>
            {calibrationRows.filter(r => r.comparison === 'user_leads').length}
          </strong>
        </div>
      </div>

      <div className={styles.driftPanel}>
        <div>
          <h2 className={styles.panelHeading}>최근 드리프트</h2>
          <p className={styles.panelSubheading}>같은 종목의 직전 run과 비교해 판단 변화가 있었는지 확인합니다.</p>
        </div>
        <div className={styles.driftList}>
          {driftRows.map(row => (
            <div key={row.ticker} className={styles.driftItem}>
              <div className={styles.driftTicker}>{row.ticker}</div>
              <div className={styles.driftStateWrap}>
                <span className={`${styles.driftState} ${driftClassName(row.driftState)}`}>
                  {row.driftState}
                </span>
              </div>
              <div className={styles.driftMeta}>
                {row.status === 'insufficient'
                  ? '비교할 이전 run이 없습니다.'
                  : `cross ${row.crossChanged ? 'changed' : 'same'} / stability ${row.stabilityChanged ? 'changed' : 'same'}`}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className={styles.driftPanel}>
        <div>
          <h2 className={styles.panelHeading}>최근 Calibration</h2>
          <p className={styles.panelSubheading}>1주 후 가격 변화 기준으로 AI와 사용자 판단을 거칠게 비교한 bootstrap 계측입니다.</p>
        </div>
        <div className={styles.driftList}>
          {calibrationRows.slice(0, 6).map(row => (
            <div key={row.runId} className={styles.driftItem}>
              <div className={styles.driftTicker}>{row.ticker}</div>
              <div className={styles.driftStateWrap}>
                <span className={`${styles.driftState} ${calibrationClassName(row.comparison)}`}>
                  {CALIBRATION_LABEL[row.comparison] ?? row.comparison}
                </span>
              </div>
              <div className={styles.driftMeta}>
                시장 결과 {MARKET_LABEL[row.marketOutcome] ?? row.marketOutcome} / AI {row.aiVerdict} / 사용자 {row.userVerdict}
              </div>
            </div>
          ))}
        </div>
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Ticker</th>
            <th>실행일</th>
            <th>기술 상태</th>
            <th>AI 판단</th>
            <th>내 판단</th>
            <th>정렬 여부</th>
            <th>드리프트</th>
            <th>Reasoning</th>
            <th>Calibration</th>
            <th>레벨 판정</th>
            <th className={styles.thRight}>현재가</th>
            <th className={styles.thRight}>RSI</th>
            <th className={styles.thRight}>1주 변화</th>
            <th className={styles.thRight}>1주 후</th>
          </tr>
        </thead>
        <tbody>
          {rows.map(row => {
            const change = row.priceChangePct;
            const changeClass = change == null
              ? styles.changeDash
              : change >= 0 ? styles.changePos : styles.changeNeg;
            const userCommit = row.userCommit;
            const alignment = userCommit == null
              ? '미입력'
              : userCommit.agreedWithAi ? '일치' : '불일치';
            const verdictSummary = row.levelVerdictCount === 0
              ? '없음'
              : `${row.heldCount} held / ${row.brokenCount} broken`;

            return (
              <tr key={row.runId} className={styles.clickableRow} onClick={() => navigate(`/ticker/${encodeURIComponent(row.ticker)}/${row.runId}`)}>
                <td className={styles.ticker}>{row.ticker}</td>
                <td className={styles.date}>{new Date(row.executedAt).toLocaleDateString('ko-KR')}</td>
                <td>
                  <StatusBadge value={row.stability || 'unknown'} />
                </td>
                <td>
                  <StatusBadge value={row.crossResult || 'UNCERTAIN'} />
                </td>
                <td className={styles.userCommit}>
                  {userCommit
                    ? <span className={styles.userCommitBadge}>{LABEL[userCommit.userCrossResult] ?? userCommit.userCrossResult}</span>
                    : <span className={styles.noCommit}>미입력</span>}
                </td>
                <td>
                  <span className={alignment === '일치' ? styles.aligned : alignment === '불일치' ? styles.misaligned : styles.noCommit}>
                    {alignment}
                  </span>
                </td>
                <td>
                  <span className={`${styles.driftState} ${driftClassName(row.driftState)}`}>
                    {row.driftState}
                  </span>
                </td>
                <td>
                  <span className={row.reasoningWeakCount > 0 ? styles.misaligned : styles.aligned}>
                    {row.reasoningWeakCount > 0 ? `weak ${row.reasoningWeakCount}` : `strong ${row.reasoningStrongCount}`}
                  </span>
                </td>
                <td className={styles.verdictCell}>
                  {CALIBRATION_LABEL[row.calibration.comparison] ?? row.calibration.comparison}
                </td>
                <td className={styles.verdictCell}>{verdictSummary}</td>
                <td style={{ textAlign: 'right' }}>{row.price?.toLocaleString() ?? '-'}</td>
                <td style={{ textAlign: 'right' }}>{row.rsi?.toFixed(1) ?? '-'}</td>
                <td className={changeClass} style={{ textAlign: 'right' }}>
                  {change != null ? `${change >= 0 ? '+' : ''}${change.toFixed(2)}%` : '-'}
                </td>
                <td style={{ textAlign: 'right' }}>{row.price1wAfter != null ? row.price1wAfter.toLocaleString() : '-'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
