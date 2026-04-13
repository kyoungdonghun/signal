import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { CrossResult } from '../types';
import styles from './CommitForm.module.css';

interface Props {
  runId: string;
  ticker: string;
  aiCrossResult?: string;
}

interface ExistingCommit {
  userCrossResult: string;
  userNote: string;
  userLevelView: string;
  committedAt: string;
  agreedWithAi: boolean;
}

const OPTIONS: { value: CrossResult; label: string }[] = [
  { value: 'ALIGNED_BULLISH', label: '강세 예상' },
  { value: 'ALIGNED_BEARISH', label: '약세 예상' },
  { value: 'CONFLICT', label: '충돌 예상' },
  { value: 'UNCERTAIN', label: '불확실' },
];

const LABEL: Record<string, string> = {
  ALIGNED_BULLISH: '강세 예상',
  ALIGNED_BEARISH: '약세 예상',
  CONFLICT: '충돌 예상',
  UNCERTAIN: '불확실',
};

export function CommitForm({ runId, ticker, aiCrossResult }: Props) {
  const [existing, setExisting] = useState<ExistingCommit | null>(null);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<CrossResult | null>(null);
  const [note, setNote] = useState('');
  const [levelView, setLevelView] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    api.getUserCommit(runId)
      .then((data: ExistingCommit | null) => {
        if (data) setExisting(data);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [runId]);

  const handleSubmit = async () => {
    if (!selected) return;
    setSubmitting(true);
    setSaveError(null);
    try {
      await api.saveUserCommit({
        runId,
        ticker,
        userCrossResult: selected,
        userNote: note,
        agreedWithAi: aiCrossResult ? selected === aiCrossResult : false,
        userLevelView: levelView || undefined,
      });
      setExisting({
        userCrossResult: selected,
        userNote: note,
        userLevelView: levelView,
        committedAt: new Date().toISOString(),
        agreedWithAi: aiCrossResult ? selected === aiCrossResult : false,
      });
      setSubmitted(true);
    } catch (e) {
      console.error(e);
      setSaveError('저장에 실패했습니다. 서버 연결을 확인해주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return null;

  // 기존 commit이 있거나 방금 제출한 경우 — 내용 표시
  if (existing || submitted) {
    const data = existing;
    return (
      <div className={styles.wrapper}>
        <p className={styles.title}>나의 commit</p>
        <div className={styles.committed}>
          <div className={styles.committedRow}>
            <span className={styles.committedLabel}>판단</span>
            <span className={styles.committedValue}>
              {data ? LABEL[data.userCrossResult] ?? data.userCrossResult : LABEL[selected!] ?? selected}
            </span>
          </div>
          {data?.userLevelView && (
            <div className={styles.committedRow}>
              <span className={styles.committedLabel}>레벨 관찰</span>
              <span className={styles.committedValue}>{data.userLevelView}</span>
            </div>
          )}
          {data?.userNote && (
            <div className={styles.committedRow}>
              <span className={styles.committedLabel}>근거</span>
              <span className={styles.committedValue}>{data.userNote}</span>
            </div>
          )}
          <div className={styles.committedRow}>
            <span className={styles.committedLabel}>기록 시각</span>
            <span className={styles.committedValue}>
              {data ? new Date(data.committedAt).toLocaleString('ko-KR') : '방금'}
            </span>
          </div>
          <p className={styles.committedFooter}>시간이 심판할 것입니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.wrapper}>
      <p className={styles.title}>당신은 어떻게 보시나요?</p>
      <div className={styles.form}>
        <div>
          <span className={styles.label}>나의 판단</span>
          <div className={styles.radioGroup}>
            {OPTIONS.map(opt => (
              <button
                key={opt.value}
                className={`${styles.radioBtn} ${selected === opt.value ? styles.selected : ''}`}
                onClick={() => setSelected(opt.value)}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>
        <div>
          <label className={styles.label} htmlFor={`level-${runId}`}>레벨 관찰 (선택)</label>
          <textarea
            id={`level-${runId}`}
            className={styles.textarea}
            placeholder={`컨벤션: 가격 / 유형 / 기간 / 근거\n예) 185000 / 지지 / 1주 / MA60 근처, Q4 반등 구간\n형식이 안 맞으면 자유롭게 써도 됩니다.`}
            value={levelView}
            onChange={e => setLevelView(e.target.value)}
          />
        </div>
        <div>
          <label className={styles.label} htmlFor={`note-${runId}`}>추가 근거 (선택)</label>
          <textarea
            id={`note-${runId}`}
            className={styles.textarea}
            placeholder="방향 판단의 근거를 기록해두세요. 나중에 사후 검증에 쓰입니다."
            value={note}
            onChange={e => setNote(e.target.value)}
          />
        </div>
        <button
          className={styles.submitBtn}
          onClick={handleSubmit}
          disabled={!selected || submitting}
        >
          {submitting ? '저장 중...' : 'Commit'}
        </button>
        {saveError && (
          <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-bearish)', margin: 0 }}>
            {saveError}
          </p>
        )}
      </div>
    </div>
  );
}
