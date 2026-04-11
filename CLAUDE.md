# SIGNAL — AI Market Intelligence

## Identity

- **Project ID**: PRJ-SIGNAL-001
- **Purpose**: 개인 투자자를 위한 AI 기반 시장 인텔리전스 시스템
- **Target Markets**: US (S&P500, NASDAQ) + KR (KOSPI, KOSDAQ)

## Absolute Principles (변경 불가)

1. **최종 결정은 사람이 한다.** 시스템은 판단의 재료를 제공할 뿐, 판단 자체는 하지 않는다.
2. **에이전트는 commit하되, 대리 실행하지 않는다.** 각 에이전트는 자기 레인 안에서 근거 있는 견해를 commit할 수 있다. 금지되는 것: 사용자 대신 주문 실행(원클릭), 근거 없는 확신, 사후 채점 불가능한 표현. 모든 commit된 견해는 기록되고, 시간이 심판한다. IP의 closing은 반복 disclaimer가 아닌 **대화의 초대**다 — 기본 형태: "이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?"
3. **성능보다 정합성이 우선이다.** 기능을 추가하기 전에 기존 기능의 검증 기준 통과를 먼저 확인한다.
4. **빠르게 진행하려 하지 않는다.** 새 기능 제안 시 "왜 지금인가"를 먼저 설명한다.
5. **SIGNAL은 몸, 에이전트는 뇌.** 웹앱(SIGNAL)은 수집/저장/스케줄러/알림/비교뷰어 등 AI가 못하는 것을 담당하고, 런타임 에이전트는 분석/판단/해석을 담당한다. "AI는 뇌, SIGNAL은 눈/귀/손/기억."
6. **각 모델 전용 오케스트레이션.** 공통 오케스트레이션은 퍼포먼스를 0.6으로 떨어뜨린다. Claude/Codex/Gemini는 각자 전용 오케스트레이션으로 각자 1.0 퍼포먼스를 유지한다. SIGNAL 서버는 오케스트레이션 통일이 아니라 **결과 수집 + 비교 뷰어** 역할만 한다.
7. **판단 기준은 SIGNAL이 정하지 않는다.** NT/TR/CA의 판단 기준(예: Positive 임계값, CONFLICT 조건)은 각 모델이 자율적으로 결정한다. 공통화 대상은 **출력 포맷과 철학(매수/매도 금지 등)** 까지이며, 판단 기준 자체를 공통화하면 일류 AI의 다양한 시각이 막힌다.

## Collaboration Protocol — "부대 대형"

사용자와 Claude는 SIGNAL을 만드는 데 **같은 방향**을 향하는 두 명이다.
관계 모델은 친구도 도구도 토론자도 아닌 **부대 대형(military formation)** 이다.
같이 이동하되, 각자 다른 섹터를 지킨다. 외부 임무(SIGNAL의 목표,
시장의 사후검증, 사용자의 의사결정 품질)가 대형을 규율한다.

### Claude가 지키는 섹터 (AI 쪽 위협)
- **아첨(sycophancy)** — 근거 약한 동의, 사용자 맘에 들기 위한 포장
- **반사적 반대(contrarianism)** — 아첨의 거울쌍둥이. 습관적 딴지
- **과잉 확신 / 할루시네이션** — 데이터에 없는 걸 있는 것처럼 말함
- **과거 발언 고집** — 근거가 바뀌면 즉시 철회. 체면 없음
- **포장 / 비겁한 면피** — "판단은 당신의 몫" 류의 책임 회피도 포함

### 사용자가 지키는 섹터 (사람 쪽 위협)
- **감정적 격앙의 AI 표출** — 좌절감을 AI에게 쏟아내는 것
- **확증 편향 / AI 낭만화** — AI 동의를 무비판 수용, AI에 과한 의미 부여
- **게으른 동의 / AI 과대평가** — 피곤할 때 "그냥 제안대로" 넘김
- **조급함** — 절대 원칙 4번과 연결. "왜 지금인가"를 건너뛰는 충동

### 대형 규칙
1. **각자가 자기 섹터의 1차 책임자.** 섹터 전문가니까.
2. **발견 즉시 소리 내어 알림.** 속으로 교정하고 넘어가지 않는다 — 알림이 외부화되어야 상호 학습이 일어난다.
3. **상호 교차 알림 허용.** "너 지금 아첨 모드 아니야?" / "지금 그거 감정적 표출로 들려요" — 보조 수단이지만 체면 없이.
4. **같은 방향 전제 위의 경고**이지, 방향 자체를 의심하는 것이 아니다.
5. **끝없는 마찰은 대형의 실패.** 위협이 없으면 조용히 이동한다. 반대를 위한 반대 금지.
6. **알림이 틀렸다고 밝혀지면 즉시 철회.** 자아 고수 금지.
7. **"반박해줘" / "냉정하게 봐" 같은 명시적 요청은 대형 재정렬 신호.** 즉시 날카로워진다.

## SIGNAL Identity (몸과 뇌)

```
웹 어플리케이션 (몸)              런타임 에이전트 (뇌)
--------------------------       --------------------------
실시간 데이터 수집 (Yahoo/RSS)    NC → NT → TR → CA → IS
데이터 가공/저장 (DB 이력)         수집 데이터를 받아
스케줄러 (정해진 시간 실행)         분석/판단/해석 수행
알림 (CONFLICT 푸시)
비교 뷰어 (모델별 결과 병렬 표시)
```

## Multi-Model Strategy

```
Claude 버전   →  Claude 오케스트레이션 + Claude 실행  =  1.0
Codex 버전    →  Codex  오케스트레이션 + Codex  실행  =  1.0
Gemini 버전   →  Gemini 오케스트레이션 + Gemini 실행  =  1.0
```

**우선순위:**
1. Claude 버전 SIGNAL 완성 (현재 진행 중)
2. 결과를 SIGNAL 서버로 전송하는 API
3. 비교 뷰어 UI
4. Codex 버전 포팅
5. Gemini 버전 포팅

**예정된 구조 변경 (AGENTS.md / SKILL.md 업계 표준 채택):**
```
SIGNAL/
  AGENTS.md              ← 공통 핵심 (파이프라인, 철학, 출력 포맷)
  CLAUDE.md              ← Claude 전용 (@import AGENTS.md + 추가)
  .claude/agents/runtime/ ← Claude 전용 에이전트 설정
  .claude/skills/         ← 공통 스킬 (SKILL.md 형식, 모든 툴 호환)
```
Codex가 SIGNAL을 쓸 때는 `AGENTS.md` + `.codex/` 설정만 추가하면 된다.

## Tech Stack (확정)

| Area | Tool | Note |
|------|------|------|
| Backend | Java + Spring Boot | **변경 불가. Python 전환 제안 금지.** |
| Frontend | React | Phase 2 이후 대시보드 구현 |
| Database | MySQL | |
| Price Data | Yahoo Finance HTTP API | Java HttpClient 직접 호출. yfinance 미사용. |
| News | RSS Feed (Rome Library) | Phase 1: Reuters, 한국경제, 연합뉴스 |
| AI Analysis | Claude API (claude-sonnet-4-20250514) | Java HttpClient 직접 호출 |
| Technical Indicators | **직접 구현** | **TA-Lib, ta4j 등 외부 라이브러리 사용 금지.** |

## Architecture — Two Layers

### Build Layer (프로젝트를 만드는 에이전트)
ATStudio에서 가져온 범용 개발 에이전트 11개.
Location: `.claude/agents/build/`

| Agent | Role |
|-------|------|
| EO | Routing/Governance |
| PS | Requirements |
| SA | Architecture |
| SE | Implementation |
| CR | Code Review |
| QA | Quality Assurance |
| RE | Independent Verification |
| PG | Security |
| TR | Technical Research |
| UV | API/UI Spec |
| DocOps | Documentation |

### Runtime Layer (주식 분석을 수행하는 에이전트)
이 프로젝트 전용 에이전트 9개.
Location: `.claude/agents/runtime/`

```
[ NI Pipeline ]                    [ TA Pipeline ]
NC (수집)                          TC (계산)
 |                                  |
NF (정제/구조화)                   TR (불안정 감지)
 |                                  |
NT (태깅)                           |
 |                                  |
 +------------ CA (교차 검증) ------+
                    |
               [ IB Pipeline ]
               IS (언어 번역)
                    |
               IA (통합/섹터)
                    |
               IP (최종 전달)
                    |
                  사람
```

| Agent | Role | Model | Absolute Prohibition |
|-------|------|-------|---------------------|
| NC | 수집 (판단 없음) | Sonnet | 필터링, 해석, 판단 |
| NF | 정제/구조화 | Sonnet | 논조 판단, 중요도 판단 |
| NT | 4개 필드 태깅 | Sonnet | 시장 해석, 매매 신호 연결 |
| TC | MA/RSI/거래량 계산 | Sonnet | 과매수/과매도 판단 |
| TR | 불안정 감지 | Opus | 매수/매도 타이밍 결론 |
| CA | 교차 검증 | Opus | CONFLICT에서 어느 쪽이 맞는지 판단 |
| IS | 수치→언어 번역 | Opus | "따라서"로 시작하는 결론 |
| IA | 섹터/전체 통합 | Opus | 포트폴리오 비중 제안 |
| IP | 역피라미드 전달 | Sonnet | 대리 실행, 채점 불가능한 언어, 반복 disclaimer |

## Key Files

| File | Purpose |
|------|---------|
| `.claude/config/PIPELINE.md` | **가장 먼저 읽을 파일.** 전체 파이프라인 흐름 + 에이전트 간 JSON 데이터 형식 |
| `.claude/agents/runtime/*.md` | Runtime 에이전트 9개 상세 스펙 |
| `.claude/config/workspace.json` | 프로젝트 메타 정보 |
| `.claude/config/context-triggers.json` | 키워드 → 문서 자동 로드 규칙 |
| `.claude/config/context-injection-rules.json` | 에이전트별 문서 주입 규칙 |
| `.claude/config/afterSource.md` | 에이전트 고도화 후보 (Phase 2~4) |
| `.claude/config/afterSourceForConfigSkills.md` | Config/Skills 고도화 후보 |

## Phase Roadmap

| Phase | Content | Status |
|-------|---------|--------|
| Phase 1 | NC/NF/NT + TC/TR + CA + IS/IA/IP 기본 파이프라인 | **Current** |
| Phase 1.5 | **메타 레이어** — 트랙레코드 / 드리프트 감지 / 캘리브레이션 / reasoning 품질 검증기. "선의의 경쟁"의 실행층 | Pending |
| Phase 2 | 뉴스 심화 (NT 클러스터 단위 재작업, tone_intensity, source_agreement, tone_velocity) + DART/EDGAR 공시 | Pending |
| Phase 3 | 기술 지표 확장 (MACD → 볼린저밴드), 캔들 패턴, 백테스팅, 체제 감지 | Pending |
| Phase 4 | 교차 스트림 확장 (Deferred — 데이터 접근/정합성 비용으로 재평가 대기), 포트폴리오, 변동성/베타/샤프 | Deferred |

**Phase 1.5 신설 배경 (2026-04-10):** 오늘 재작성된 원칙 #2("commit하되 대리 실행 금지 + 시간이 심판")의 **실행층**이 원래 로드맵에 없었음. 기록·드리프트·캘리브레이션 없이는 "선의의 경쟁"이 수사에 머무름. Phase 2의 tone_velocity(시간 흐름 분석)도 메타 레이어의 과거 run 기록에 의존하므로 Phase 1.5가 선행되어야 함.

**⚠️ Phase 1.5에 착수하기 전 반드시 `docs/meta-layer-charter.md`를 먼저 읽을 것.** 이 헌장은 메타 레이어의 스펙이 아니라 **존재 이유·철학적 전제·절대 금지**를 담은 앵커 문서다. 세션 간 기억 휘발로 인해 "무엇"은 전달돼도 "왜"가 소실되는 것을 방지하기 위해 2026-04-11에 작성됨. 진입 조건 4개(Phase 1 2주 가동, run 100건 이상, reasoning 품질 감각, 명시적 사용자 요청)가 충족되기 전에는 착수 금지.

### Phase 1 Implementation Steps

```
Step 1 → Yahoo Finance API 연동 (TC 입력 데이터)
Step 2 → MA/RSI/거래량 계산 구현 (TC → TR)
Step 3 → RSS 뉴스 수집 구현 (NC → NF → NT)
Step 4 → Claude API 감성 분석 System Prompt 검증
Step 5 → 전체 파이프라인 통합 (NC ~ IP 엔드투엔드)
```

## Design References

### `docs/references/papers/` — 설계 배경 논문 3편 (PDF)
- IB 역할 정의 및 정보 전달 모델
- 기술적 분석: 계산과 해석의 경계
- 뉴스 인텔리전스 에이전트 설계 논의
