import { useState } from 'react';
import { api } from '../api/client';
import type { CrossResult } from '../types';
import styles from './CommitForm.module.css';

interface Props {
  runId: string;
  ticker: string;
}

const OPTIONS: { value: CrossResult; label: string }[] = [
  { value: 'ALIGNED_BULLISH', label: '강세 동의' },
  { value: 'ALIGNED_BEARISH', label: '약세 동의' },
  { value: 'CONFLICT', label: '충돌 감지' },
  { value: 'UNCERTAIN', label: '불확실' },
];

export function CommitForm({ runId, ticker }: Props) {
  const [selected, setSelected] = useState<CrossResult | null>(null);
  const [note, setNote] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async () => {
    if (!selected) return;
    setSubmitting(true);
    try {
      await api.saveUserCommit({
        runId,
        ticker,
        userCrossResult: selected,
        userNote: note,
        agreedWithAi: selected === 'UNCERTAIN',
      });
      setSubmitted(true);
    } catch (e) {
      console.error(e);
    } finally {
      setSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className={styles.wrapper}>
        <p className={styles.success}>✓ commit이 기록됐습니다. 시간이 심판할 것입니다.</p>
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
          <label className={styles.label} htmlFor={`note-${runId}`}>근거 (선택)</label>
          <textarea
            id={`note-${runId}`}
            className={styles.textarea}
            placeholder="왜 이렇게 판단했는지 기록해두세요. 나중에 사후 검증에 쓰입니다."
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
      </div>
    </div>
  );
}
