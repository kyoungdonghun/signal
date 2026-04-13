import styles from './StatusBadge.module.css';

interface Props {
  value: string;
  label?: string;
}

const LABELS: Record<string, string> = {
  ALIGNED_BULLISH: '긍정 일치',
  ALIGNED_BEARISH: '부정 일치',
  CONFLICT: '충돌',
  UNCERTAIN: '불확실',
  stable: '일관',
  unstable: '모순',
  unknown: '불확실',
};

export function StatusBadge({ value, label }: Props) {
  const cls = styles[value] ?? styles['UNCERTAIN'];
  return (
    <span className={`${styles.badge} ${cls}`}>
      {label ?? LABELS[value] ?? value}
    </span>
  );
}

// 3줄 상태 표시 컴포넌트
interface StatusSummaryProps {
  stability: string;
  newsDirection: string;
  crossResult: string;
}

const CROSS_ICON: Record<string, string> = {};

const NEWS_LABEL: Record<string, string> = {
  positive: '긍정',
  negative: '부정',
  unclear: '불확실',
};

const NEWS_COLOR: Record<string, string> = {
  positive: 'var(--color-bullish)',
  negative: 'var(--color-bearish)',
  unclear: 'var(--color-uncertain)',
};

const CROSS_COLOR: Record<string, string> = {
  ALIGNED_BULLISH: 'var(--color-bullish)',
  ALIGNED_BEARISH: 'var(--color-bearish)',
  CONFLICT: 'var(--color-conflict)',
  UNCERTAIN: 'var(--color-uncertain)',
};

const STABILITY_COLOR: Record<string, string> = {
  stable: 'var(--color-bullish)',
  unstable: 'var(--color-bearish)',
  unknown: 'var(--color-uncertain)',
};

export function StatusSummary({ stability, newsDirection, crossResult }: StatusSummaryProps) {
  const icon = CROSS_ICON[crossResult] ?? '';
  const crossLabel = LABELS[crossResult] ?? crossResult;
  const newsLabel = NEWS_LABEL[newsDirection] || (newsDirection || '—');

  return (
    <div className={styles.summary}>
      <div className={styles.summaryRow}>
        <span className={styles.summaryLabel}>기술 분석</span>
        <span className={styles.summaryValue} style={{ color: STABILITY_COLOR[stability] ?? 'var(--color-uncertain)' }}>
          {LABELS[stability] ?? (stability || '—')}
        </span>
      </div>
      <div className={styles.summaryRow}>
        <span className={styles.summaryLabel}>뉴스 분석</span>
        <span className={styles.summaryValue} style={{ color: NEWS_COLOR[newsDirection] ?? 'var(--color-uncertain)' }}>
          {newsLabel}
        </span>
      </div>
      <div className={`${styles.summaryRow} ${styles.summaryResult}`}>
        <span className={styles.summaryLabel}>결과</span>
        <span className={styles.summaryValue} style={{ color: CROSS_COLOR[crossResult] ?? 'var(--color-uncertain)' }}>
          {icon ? `${icon} ${crossLabel}` : crossLabel}
        </span>
      </div>
    </div>
  );
}
