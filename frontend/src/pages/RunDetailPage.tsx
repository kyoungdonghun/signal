import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { StatusSummary } from '../components/StatusBadge';
import { CommitForm } from './CommitForm';
import type { RunDetail } from '../types';
import styles from './BriefingPage.module.css';
import detailStyles from './RunDetailPage.module.css';

const TICKER_NAMES: Record<string, string> = {
  '005930.KS': '삼성전자',
  '005380.KS': '현대차',
  'TSLA': '테슬라',
};

export function RunDetailPage() {
  const { ticker, runId } = useParams<{ ticker: string; runId: string }>();
  const navigate = useNavigate();
  const [run, setRun] = useState<RunDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!runId) return;
    api.getRunDetail(runId)
      .then(setRun)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [runId]);

  if (loading) return <div className={detailStyles.loading}>불러오는 중...</div>;
  if (!run) return <div className={detailStyles.loading}>분석 데이터를 찾을 수 없습니다.</div>;

  const ip = run.fullResult?.ip;
  const tr = run.fullResult?.tr;
  const detail = ip?.details?.[0];
  const relevantNews = (run.fullResult?.taggedNews ?? [])
    .filter(n => n.relevance === 'High' || n.relevance === 'Medium')
    .sort((a, _b) => (a.relevance === 'High' ? -1 : 1));

  const name = TICKER_NAMES[run.ticker] ?? run.ticker;

  return (
    <div className={detailStyles.page}>
      <div className={detailStyles.topBar}>
        <button
          className={detailStyles.backBtn}
          onClick={() => navigate(`/ticker/${encodeURIComponent(ticker ?? run.ticker)}`)}
        >
          ← {name}
        </button>
        <span className={detailStyles.runDate}>
          {new Date(run.executedAt).toLocaleString('ko-KR')}
        </span>
      </div>

      <div className={styles.card} style={{ marginTop: 0 }}>
        <div className={styles.cardHeader}>
          <span className={styles.ticker}>{name}</span>
          <StatusSummary
            stability={run.stability || 'unknown'}
            newsDirection={run.newsDirection || ''}
            crossResult={run.crossResult || 'UNCERTAIN'}
          />
        </div>

        <div className={styles.metaRow}>
          <div className={styles.metaItem}>
            <span className={styles.metaLabel}>현재가</span>
            <span className={styles.metaValue}>{run.price?.toLocaleString()}</span>
          </div>
          <div className={styles.metaItem}>
            <span className={styles.metaLabel}>RSI(14)</span>
            <span className={styles.metaValue}>{run.rsi?.toFixed(2)}</span>
          </div>
          <div className={styles.metaItem}>
            <span className={styles.metaLabel}>기술적 신뢰도</span>
            <span className={styles.metaValue}>{tr?.confidence ?? '-'}</span>
          </div>
          {run.priceChangePct != null && (
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>1일 후 변화</span>
              <span className={styles.metaValue} style={{
                color: run.priceChangePct >= 0 ? 'var(--color-bullish)' : 'var(--color-bearish)'
              }}>
                {run.priceChangePct >= 0 ? '+' : ''}{run.priceChangePct.toFixed(2)}%
              </span>
            </div>
          )}
        </div>

        {ip?.topAttention?.map((a, i) => (
          <div key={i} className={styles.topAttentionBox}>
            <div className={styles.topAttentionTitle}>즉시 확인 필요</div>
            <div className={styles.topAttentionText}>{a.oneLine}</div>
          </div>
        ))}

        {ip?.marketOverview && (
          <div className={styles.section}>
            <div className={styles.sectionTitle}>시장 개요</div>
            <p className={styles.bodyText}>{ip.marketOverview}</p>
          </div>
        )}

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
            {relevantNews.length > 0 && (
              <div className={styles.section}>
                <div className={styles.sectionTitle}>분석에 사용된 뉴스</div>
                <div className={styles.sourceList}>
                  {relevantNews.map((n, i) => (
                    <a key={i} href={n.url} target="_blank" rel="noreferrer" className={styles.sourceLink}>
                      <span className={styles.relevanceBadge}>{n.relevance}</span>
                      {n.title}
                    </a>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {ip?.closingStatement && (
          <div className={styles.closing}>{ip.closingStatement}</div>
        )}

        <CommitForm runId={run.runId} ticker={run.ticker} aiCrossResult={run.crossResult} />
      </div>
    </div>
  );
}
