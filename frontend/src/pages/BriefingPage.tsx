import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type { RunDetail } from '../types';
import styles from './BriefingPage.module.css';
import { CommitForm } from './CommitForm';

export function BriefingPage() {
  const [runs, setRuns] = useState<RunDetail[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getLatestRuns()
      .then(summaries =>
        Promise.all(summaries.map(s => api.getRunDetail(s.runId)))
      )
      .then(setRuns)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.loading}>분석 데이터 불러오는 중...</div>;
  if (runs.length === 0) return (
    <div className={styles.empty}>
      아직 실행된 분석이 없습니다.<br />
      서버에서 <code>POST /api/pipeline/scheduler/trigger</code>를 호출해주세요.
    </div>
  );

  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  });

  return (
    <div className={styles.page}>
      <div>
        <h1 className={styles.heading}>오늘의 브리핑</h1>
        <p className={styles.subheading}>{today}</p>
      </div>

      {runs.map(run => {
        const ip = run.fullResult?.ip;
        const tr = run.fullResult?.tr;
        const detail = ip?.details?.[0];

        return (
          <div key={run.runId} className={styles.card}>
            <div className={styles.cardHeader}>
              <span className={styles.ticker}>{run.ticker}</span>
              <div className={styles.badges}>
                <StatusBadge value={run.stability || 'unknown'} />
                <StatusBadge value={run.crossResult || 'UNCERTAIN'} />
              </div>
            </div>

            {/* 기술 지표 요약 */}
            <div className={styles.metaRow}>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>현재가</span>
                <span className={styles.metaValue}>
                  {run.price?.toLocaleString()}
                </span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>RSI(14)</span>
                <span className={styles.metaValue}>{run.rsi?.toFixed(2)}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>기술적 신뢰도</span>
                <span className={styles.metaValue}>{tr?.confidence ?? '-'}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>실행 시각</span>
                <span className={styles.metaValue}>
                  {new Date(run.executedAt).toLocaleString('ko-KR')}
                </span>
              </div>
            </div>

            {/* Top Attention */}
            {ip?.topAttention?.map((a, i) => (
              <div key={i} className={styles.topAttentionBox}>
                <div className={styles.topAttentionTitle}>즉시 확인 필요</div>
                <div className={styles.topAttentionText}>{a.oneLine}</div>
              </div>
            ))}

            {/* 시장 개요 */}
            {ip?.marketOverview && (
              <div className={styles.section}>
                <div className={styles.sectionTitle}>시장 개요</div>
                <p className={styles.bodyText}>{ip.marketOverview}</p>
              </div>
            )}

            {/* 종목 상세 */}
            {detail && (
              <>
                <div className={styles.section}>
                  <div className={styles.sectionTitle}>뉴스 요약</div>
                  <p className={styles.bodyText}>{detail.newsSummary}</p>
                </div>
                <div className={styles.section}>
                  <div className={styles.sectionTitle}>기술적 상태</div>
                  <p className={styles.bodyText}>{detail.technicalSummary}</p>
                </div>
                <div className={styles.section}>
                  <div className={styles.sectionTitle}>종합 분석</div>
                  <p className={styles.bodyText}>{detail.summaryText}</p>
                </div>
                {detail.sourceUrls?.length > 0 && (
                  <div className={styles.section}>
                    <div className={styles.sectionTitle}>뉴스 출처</div>
                    <div className={styles.sourceList}>
                      {detail.sourceUrls.map((url, i) => (
                        <a key={i} href={url} target="_blank" rel="noreferrer" className={styles.sourceLink}>
                          {url}
                        </a>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}

            {/* Closing */}
            {ip?.closingStatement && (
              <div className={styles.closing}>{ip.closingStatement}</div>
            )}

            {/* 사용자 commit */}
            <CommitForm runId={run.runId} ticker={run.ticker} />
          </div>
        );
      })}
    </div>
  );
}
