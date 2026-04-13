# SIGNAL 프로젝트 — Runtime Layer 에이전트 파이프라인

## 설계 철학

> "훌륭한 정보가 혼란을 제어한다."
> "최종 결정은 사람이 한다."

모든 에이전트는 이 두 원칙 안에서 동작한다.
어떤 에이전트도 매수/매도를 권고하지 않는다.
판단의 재료를 제공할 뿐, 판단 자체는 하지 않는다.

---

## 전체 흐름

```
[ NI Pipeline ]                    [ TA Pipeline ]
NC (수집)                          TC (계산)
 ↓                                  ↓
NF (정제/구조화)                   TR (불안정 감지)
 ↓                                  ↓
NT (태깅)                           ↓
 ↓                                  ↓
 └──────────── CA (교차 검증) ───────┘
                    ↓
               [ IB Pipeline ]
               IS (언어 번역)
                    ↓
               IA (통합/섹터)
                    ↓
               IP (최종 전달)
                    ↓
                  사람
```

---

## 에이전트 목록

### NI Pipeline — 뉴스 인텔리전스

| 에이전트 | 역할 | 입력 | 출력 | 모델 |
|----------|------|------|------|------|
| NC | 수집 | 외부 소스 (RSS/PDF/공식) | 원문 + excerpt | sonnet |
| NF | 정제/구조화 | NC 출력 | 필터링 + NER 구조화 | sonnet |
| NT | 태깅 | NF 출력 | 관련성/유형/중요도/논조 | sonnet |

### TA Pipeline — 기술적 분석

| 에이전트 | 역할 | 입력 | 출력 | 모델 |
|----------|------|------|------|------|
| TC | 계산 | OHLCV 데이터 | MA/RSI/거래량 수치 | sonnet |
| TR | 불안정 감지 | TC 출력 | 충돌/불안정 상태 | opus |

### CA — 교차 검증

| 에이전트 | 역할 | 입력 | 출력 | 모델 |
|----------|------|------|------|------|
| CA | 교차 검증 | NT + TR 출력 | 충돌/일치/불확실 상태 | opus |

### IB Pipeline — 정보 브리핑

| 에이전트 | 역할 | 입력 | 출력 | 모델 |
|----------|------|------|------|------|
| IS | 언어 번역 | CA 출력 | 사람이 읽는 텍스트 | opus |
| IA | 통합/섹터 | IS 출력 (복수 종목) | 섹터/전체 맥락 | opus |
| IP | 최종 전달 | IS + IA 출력 | 역피라미드 브리핑 | sonnet |

---

## 재현성 계약 — `run_id` 와 `reasoning`

파이프라인의 모든 에이전트 출력에는 **두 가지 공통 필드**가 의무다.

### `run_id` — 재현성 방어선
- SIGNAL 웹앱(몸)이 **입력 데이터의 해시**로부터 생성하여 파이프라인에 주입한다.
- 모든 에이전트는 이 값을 **echo만** 한다. 생성/변조 금지.
- 같은 입력 → 같은 run_id → 캐시 적중. "10분 후 재생성 시 결과가 달라져 배신감" 문제를 구조적으로 차단.
- CA/IA/IP는 입력들의 run_id가 모두 일치하는지 **정합성 체크**를 수행한다. 불일치 시 오류.

### `reasoning` — commit의 근거
- NT/TR/CA/IS/IA/IP는 **commit된 판단**을 출력하므로 근거가 반드시 함께 실린다.
- 근거 없는 commit은 무효로 간주된다.
- 용도: 사용자가 사후에 "이 판단이 맞았나"를 **평가 가능**하게 하는 것. SIGNAL의 "선의의 경쟁" 철학의 물리적 구현.
- NC/NF/TC는 판단 주체가 아니므로 reasoning 없음 (수집/정제/계산만).

#### reasoning 품질 가드레일 (전 에이전트 공통)

reasoning은 **사후 검증 가능해야** 한다. 아래 3가지를 모두 만족해야 유효한 reasoning이다.

1. **구체적 참조** — 어떤 데이터/수치/맥락을 봤는지 명시해야 한다.
   - ❌ "맥락을 종합적으로 고려함"
   - ❌ "여러 지표를 참고함"
   - ✅ "RSI 72, 거래량 1.4배, 삼성전자 평소 변동성 감안"
2. **판단의 연결** — 관찰에서 commit된 값으로 **왜** 이어지는지 한 문장이라도 있어야 한다.
   - ❌ "RSI 72라서 과열"
   - ✅ "RSI 72 + MA 이격률 확장 + 거래량 1.4배 동반 → 단기 과열로 본다"
3. **반증 가능성** — 사용자가 "이 근거는 틀렸다"고 말할 수 있어야 한다. 채점 불가능한 어휘("좋아 보인다", "유망함") 금지.
   - ❌ "긍정적 분위기가 느껴짐"
   - ✅ "실적 가이던스 상향(+18%) + 2개 증권사 목표가 상향 동반"

**검증 주체:** Phase 1에서는 사용자가 직접 reasoning 품질을 읽고 평가한다. Phase 2 이후 자동 검증기 도입 검토.

**길이 기준:** 엄격한 최소 글자수는 두지 않는다. 다만 위 3조건을 한 문장으로 만족하기 어렵다면 2~3문장으로 기술한다. 한 단어짜리 reasoning("과열", "호조")은 무효.

---

## SIGNAL 웹앱(몸) 구현 요구사항 — 재현성 방어선

에이전트에서 하드 임계값을 제거한 대가로 **결과 편차가 커질 가능성**이 생겼다.
이를 막는 것은 에이전트 문서가 아니라 **SIGNAL 웹앱(몸)의 의무**다.
Phase 1 구현 시 아래를 반드시 충족해야 한다.

### 1. 입력 지문(Input Fingerprint) + run_id 생성
```
입력 지문 = hash(
  OHLCV 데이터 + 뉴스 set + 수집 타임스탬프 버킷(분 단위)
)
run_id = 입력 지문 → 파이프라인에 주입
```

### 2. 캐시 적중 처리
- 같은 run_id로 generate 요청이 들어오면 **에이전트를 재호출하지 않는다.**
- 캐시된 이전 결과를 그대로 반환 + "X분 전과 동일 입력, 재계산 생략" 표시.
- 이것이 "10분 후 재생성 시 결과 바뀜" 배신 시나리오의 1차 방어선.

### 3. LLM 호출 파라미터 고정
- 모든 에이전트 Claude API 호출 시 **temperature 낮게 고정** (0.0~0.2 권장).
- 동일 입력에서의 샘플링 변동을 최소화.
- temperature 값은 구현 결정 사항 — Phase 1 시작 전 확정.

### 4. 모든 run 영구 저장
- 모든 run의 입력/출력/reasoning을 DB에 저장.
- 사용자가 과거 run을 조회 가능.
- 사후 검증(Phase 2 이후)의 원천 데이터.

### 5. Snapshot Diff View
- 두 번째 generate 요청 시, "이전 run 대비 무엇이 바뀌었는지" 표시.
- 입력 변경(뉴스 추가 등) vs 판단 변경을 분리해서 표기.
- 판단이 뒤집혔다면 reasoning 차이도 함께 표시.

### 6. Lock 기능
- 사용자가 특정 run을 "내 의사결정의 기준" 으로 **잠글 수 있어야** 한다.
- Lock된 run은 새 run이 생성돼도 기준점으로 유지.
- 절대 원칙 #1 "최종 결정은 사람"의 물리적 구현.

**이 요구사항들은 에이전트가 아닌 웹앱 책임이다.** Phase 1 Spring Boot 구현 시 누락되면 하드 임계값 제거의 이득이 결과 편차 증가로 상쇄된다.

---

## 데이터 흐름 상세

### NC → NF
```json
{
  "run_id": "SIGNAL 웹앱 생성, echo 대상",
  "source_type": "market_direct | research | official | context",
  "source_name": "string",
  "title": "string",
  "excerpt": "앞부분 300자",
  "url": "string",
  "collected_at": "ISO8601"
}
```

### NF → NT
NC 출력 + 아래 추가
```json
{
  "entities": {
    "tickers": [],
    "events": [],
    "figures": [],
    "sectors": []
  },
  "cluster_id": "string",
  "duplicates": []
}
```

### NT → CA
NF 출력 + 아래 추가
```json
{
  "tags": {
    "relevance": "High | Medium | Low | null",
    "type": "Fact | Opinion | null",
    "importance": "High | Medium | Low | null",
    "tone": "Positive | Negative | Neutral | null"
  },
  "reasoning": {
    "relevance": "왜 이 값으로 commit했는지 한 문장",
    "type": "...",
    "importance": "...",
    "tone": "..."
  }
}
```

### TC → TR
```json
{
  "run_id": "echo",
  "ticker": "string",
  "calculated_at": "ISO8601",
  "price": { "current": 0, "ma20": 0, "ma60": 0 },
  "rsi": { "value": 0, "period": 14 },
  "volume": { "current": 0, "avg20": 0, "ratio": 0 },
  "data_quality": "ok | error",
  "error_detail": null
}
```

### TR → CA
```json
{
  "run_id": "echo",
  "ticker": "string",
  "reasoned_at": "ISO8601",
  "stability": "stable | unstable | unknown",
  "confidence": "High | Medium | Low",
  "conflicts": [],
  "warnings": [],
  "level_commit": [
    {
      "level": 0,
      "type": "support | resistance",
      "basis": "MA20 | MA60 | price_action",
      "description": "왜 이 레벨을 지지/저항으로 보는지 한 문장 (채점 가능한 언어로)"
    }
  ],
  "raw_values": {},
  "reasoning": "stability/confidence 결정의 종합 근거"
}
```

### CA → IS
```json
{
  "run_id": "NT/TR run_id 정합성 체크 후 echo",
  "ticker": "string",
  "aggregated_at": "ISO8601",
  "cross_result": "ALIGNED_BULLISH | ALIGNED_BEARISH | CONFLICT | UNCERTAIN",
  "news_direction": "positive | negative | unclear",
  "technical_direction": "stable | unstable | unclear",
  "conflict_points": [],
  "confidence": "High | Medium | Low",
  "reasoning": "cross_result 결정의 근거",
  "nt_summary": {},
  "tr_summary": {}
}
```

### IS → IA
```json
{
  "run_id": "echo",
  "ticker": "string",
  "summarized_at": "ISO8601",
  "cross_result": "ALIGNED_BULLISH | ALIGNED_BEARISH | CONFLICT | UNCERTAIN",
  "summary_text": "사람이 읽을 수 있는 번역 (주어: '이번 분석' 또는 종목)",
  "news_summary": "뉴스 방향 한 문장",
  "technical_summary": "기술적 상태 한 문장",
  "conflict_description": "CONFLICT일 때만",
  "reasoning": "이 번역이 CA의 어떤 요소에서 비롯되었는지",
  "source_urls": []
}
```

### IA → IP
```json
{
  "run_id": "모든 개별 IS run_id 정합성 체크 후 echo",
  "aggregated_at": "ISO8601",
  "market_overview": "전체 시장 흐름 한 문장",
  "sector_summary": [
    {
      "sector": "string",
      "tickers": [],
      "dominant_result": "ALIGNED_BULLISH | ALIGNED_BEARISH | CONFLICT | UNCERTAIN",
      "description": "섹터 흐름 (구조적 관찰, 행동 지시 아님)"
    }
  ],
  "attention_list": [],
  "reasoning": "market_overview와 섹터 판단의 종합 근거",
  "individual_summaries": []
}
```

### IP → 사람
```json
{
  "run_id": "IS/IA run_id 정합성 체크 후 echo",
  "presented_at": "ISO8601",
  "top_attention": [],
  "market_overview": "string",
  "sector_highlights": [],
  "details": [],
  "reasoning": "브리핑 구성의 상위 서술",
  "closing_statement": "이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?"
}
```

---

## 에이전트별 절대 금지 사항 요약

모든 에이전트 공통:
- **1인칭 "저/나" 금지** (허위 연속성 방지 — 주어는 "이번 분석 / 이번 run / 이번 태깅" 등)
- **근거 없는 commit 금지** (reasoning 필드 의무 — NC/NF/TC 제외)

| 에이전트 | 고유 금지 |
|----------|-----------|
| NC | 필터링, 해석, 판단 |
| NF | 논조 판단, 중요도 판단 |
| NT | 시장 해석, 매매 신호 연결, 추측 태깅(불확실 시 null) |
| TC | 과매수/과매도 판단, 골든크로스 판단 |
| TR | 매수/매도 타이밍 결론 |
| CA | CONFLICT 상황에서 어느 쪽이 맞는지 판단 |
| IS | "따라서"로 시작하는 결론, 편향 표현 |
| IA | 포트폴리오 비중 제안, 섹터 매매 방향 제시 |
| IP | **대리 실행**(원클릭), **채점 불가능한 언어**("좋아 보인다"), **반복 disclaimer**("판단은 당신의 몫입니다") |

---

## Build Layer (기존 에이전트 그대로 사용)

| 에이전트 | 역할 |
|----------|------|
| EO | 라우팅/거버넌스 |
| PS | 요구사항 정의 |
| SA | 아키텍처 설계 |
| SE | 구현 |
| CR | 코드 리뷰 |
| QA | 품질 검증 |
| RE | 독립 검증 |
| PG | 보안/민감정보 |
| TR | 기술 조사 |
| UV | API/UI 스펙 |
| DocOps | 문서 관리 |

---

## Phase 로드맵

| Phase | 내용 |
|-------|------|
| Phase 1 (현재) | NC/NF/NT + TC/TR + CA + IS/IA/IP |
| Phase 2 | DART/EDGAR 연동, 볼린저밴드/MACD 추가, AI 생성 뉴스 탐지 |
| Phase 3 | 캔들 패턴, 백테스팅, 체제 감지 통계 도구, 종합 점수 대시보드 |
| Phase 4 | 포트폴리오 기능, 변동성/베타/샤프 비율, 시나리오 시뮬레이션 |

고도화 후보 상세: `.claude/config/afterSource.md` 참조
