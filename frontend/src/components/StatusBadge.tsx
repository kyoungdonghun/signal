import styles from './StatusBadge.module.css';

interface Props {
  value: string;
  label?: string;
}

const LABELS: Record<string, string> = {
  ALIGNED_BULLISH: '강세 일치',
  ALIGNED_BEARISH: '약세 일치',
  CONFLICT: '충돌',
  UNCERTAIN: '불확실',
  stable: '안정',
  unstable: '불안정',
  unknown: '알 수 없음',
};

export function StatusBadge({ value, label }: Props) {
  const cls = styles[value] ?? styles['UNCERTAIN'];
  return (
    <span className={`${styles.badge} ${cls}`}>
      {label ?? LABELS[value] ?? value}
    </span>
  );
}
