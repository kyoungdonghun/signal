# afterSourceForConfigSkills — Config & Skills 고도화 후보 목록

이 파일은 config/skills 설계 과정에서 의도적으로 제외되었으나,
추후 고도화 또는 컨펌을 통해 적용 가치가 있는 항목들을 보존한다.

각 항목은 **어느 파일에 적용되는지**, **왜 지금은 아닌지**, **어느 시점에 적합한지**를 명시한다.

---

## workspace.json 관련

### skill-creator 활용한 신규 스킬 자동 생성
- **적용 대상**: workspace.json `skills` 목록 + skill-creator 스킬
- **내용**: Phase 2~4 진행 시 신규 스킬이 필요할 때, skill-creator 스킬을 활용해 표준화된 형식으로 자동 생성. 현재 수동으로 작성한 4개 스킬(yahoo-finance-fetch 등)과 동일한 품질 보장.
- **왜 지금은 아닌가**: Phase 1 스킬 4개가 먼저 실제 동작 검증되어야 템플릿으로 쓸 수 있음.
- **적합 시점**: Phase 2 진입 시

### multi-project 구조 확장
- **적용 대상**: workspace.json `domain_projects` 배열
- **내용**: 현재 PRJ-SIGNAL-001 단일 프로젝트. 향후 "미국 전용", "한국 전용", "포트폴리오" 등으로 도메인 분리 시 멀티 프로젝트 구조로 확장.
- **왜 지금은 아닌가**: 단일 파이프라인 검증 전 분리는 복잡도만 높임.
- **적합 시점**: Phase 4 (포트폴리오 기능 도입 시)

### phase_gate 조건부 스킬 활성화
- **적용 대상**: workspace.json `tech_stack_documents`
- **내용**: 스킬을 Phase 조건에 따라 자동 활성화/비활성화. 예: React 관련 스킬은 대시보드 Phase에서만 활성화.
- **왜 지금은 아닌가**: Phase 1은 백엔드 중심. 프론트엔드 스킬 조건부 제어가 아직 불필요.
- **적합 시점**: Phase 2 (React 대시보드 구현 시작 시)

---

## context-triggers.json 관련

### 트리거 우선순위 (priority) 필드 추가
- **적용 대상**: context-triggers.json 각 트리거 항목
- **내용**: 여러 트리거가 동시에 매칭될 때 어떤 문서를 우선 로드할지 결정하는 priority 필드. 현재는 매칭된 모든 트리거의 문서를 전부 로드하는 구조.
- **왜 지금은 아닌가**: Phase 1에서는 트리거 수가 적어 충돌 가능성 낮음. 트리거가 20개 이상으로 늘어나면 필요.
- **적합 시점**: Phase 3 이후 트리거 수 증가 시

### 동적 트리거 (DB 기반)
- **적용 대상**: context-triggers.json 전체
- **내용**: 현재 JSON 파일로 고정된 트리거를 DB 또는 관리 UI에서 동적으로 추가/수정 가능하도록 전환. 새로운 소스(증권사 리서치 등)가 추가될 때마다 파일 수정 없이 트리거 추가 가능.
- **왜 지금은 아닌가**: 오버엔지니어링. Phase 1은 JSON 파일로 충분.
- **적합 시점**: Phase 3~4 (소스 다양화 시)

### 언어 감지 트리거 분기
- **적용 대상**: context-triggers.json
- **내용**: 한국어 키워드와 영어 키워드를 별도 트리거로 분리하거나, 언어 감지 후 적합한 소스 문서를 다르게 로드. 예: "주가" 입력 시 한국 시장 문서, "stock price" 입력 시 미국 시장 문서 우선 로드.
- **왜 지금은 아닌가**: 현재 트리거는 한/영 혼합 regex로 처리 중. 소스별 분리가 필요한 시점에 도입.
- **적합 시점**: Phase 2 (한국/미국 시장 분리 대응 시)

---

## context-injection-rules.json 관련

### Runtime 에이전트 전용 Tier 0 헌법 문서 (signal-principles.md)
- **적용 대상**: context-injection-rules.json `tier_defaults` + Runtime 에이전트 전체
- **내용**: 현재 Runtime 에이전트의 "매수/매도 금지" 원칙은 `AGENTS.md`, `CLAUDE.md`, `runtime_agent_constraints`에 분산되어 있음. 이것을 별도의 vendor-neutral 원칙 문서로 분리해 Tier 0으로 등록하면, 모든 에이전트에 자동 주입되어 원칙 위반 가능성을 구조적으로 차단 가능.
- **왜 지금은 아닌가**: 공통 문서 계층이 아직 얇고, 현재는 `AGENTS.md` + 에이전트 파일 조합으로 충분하다.
- **적합 시점**: Phase 2 (docs/ 문서 체계 구축 시)

### 에이전트 간 데이터 계약 검증 (schema validation)
- **적용 대상**: context-injection-rules.json + 신규 skill
- **내용**: NC→NF→NT→CA 등 에이전트 간 데이터 형식(JSON Schema)을 등록하고, 실제 전달 데이터가 스펙과 일치하는지 자동 검증. 현재는 PIPELINE.md에 형식이 문서로만 정의되어 있음.
- **왜 지금은 아닌가**: 파이프라인이 실제로 구현되기 전에는 스키마 검증 도구가 의미 없음.
- **적합 시점**: Phase 1 구현 완료 후 (Step 5 이후)

### LLM inference 단계 고도화 (Phase 2 llm_inference_config)
- **적용 대상**: context-injection-rules.json `task_type_documents.llm_inference_config`
- **내용**: 현재 Phase 2 LLM 추론은 기본 instruction만 정의. 향후 WI 내용을 분석해 "이 작업이 어느 Runtime 에이전트와 관련 있는가"를 자동 판단하는 로직으로 고도화. 특히 CA/IB 파이프라인 관련 작업을 정확하게 분류하는 데 유용.
- **왜 지금은 아닌가**: LLM 추론 품질은 실제 WI가 쌓인 후 평가할 수 있음.
- **적합 시점**: Phase 2 (WI 10개 이상 축적 후)

---

## 신규 스킬 관련

### yahoo-finance-fetch: 장애 대응 (fallback 소스)
- **적용 대상**: yahoo-finance-fetch/SKILL.md
- **내용**: Yahoo Finance API가 응답하지 않을 때 대체 소스(Alpha Vantage, 네이버 금융 등)로 자동 전환하는 fallback 로직. 현재는 실패 시 오류 반환만 함.
- **왜 지금은 아닌가**: Phase 1은 Yahoo Finance 단일 소스로 먼저 안정화.
- **적합 시점**: Phase 2

### rss-feed-fetch: 소스 확장 (Bloomberg, WSJ, 연합인포맥스)
- **적용 대상**: rss-feed-fetch/SKILL.md `수집 소스 목록`
- **내용**: Phase 1에서는 Reuters, 한국경제, 연합뉴스 3개로 시작. Phase 2에서 Bloomberg, WSJ, FT, 연합인포맥스, 증권사 리서치 추가.
- **왜 지금은 아닌가**: 소스가 늘어나면 NF의 중복 제거 로직 부담이 증가. 기본 파이프라인 안정화 후 확장.
- **적합 시점**: Phase 2

### rss-feed-fetch: DART/SEC EDGAR 공시 수집
- **적용 대상**: rss-feed-fetch/SKILL.md 또는 신규 스킬 분리
- **내용**: RSS가 아닌 공식 API로 한국 DART, 미국 SEC EDGAR 공시 직접 수집. 각 소스의 인증 방식과 형식이 다르므로 별도 스킬로 분리 권장.
- **왜 지금은 아닌가**: API 인증, 데이터 형식 처리 복잡도가 높음. RSS 파이프라인 먼저 검증.
- **적합 시점**: Phase 2

### technical-indicator-calc: 지표 확장 (볼린저밴드, MACD, 스토캐스틱)
- **적용 대상**: technical-indicator-calc/SKILL.md
- **내용**: Phase 1의 MA/RSI/거래량 외에 볼린저밴드, MACD, 스토캐스틱 추가. TR 에이전트의 불안정 감지 로직도 함께 확장 필요.
- **왜 지금은 아닌가**: 지표 증가 시 TR의 충돌 감지 로직 복잡도 증가. 기본 3종 검증 후 확장.
- **적합 시점**: Phase 2

### claude-api-call: 배치 처리 (Batch API)
- **적용 대상**: claude-api-call/SKILL.md
- **내용**: 종목이 많아질 경우 NT 태깅을 건별 호출이 아닌 Anthropic Batch API로 처리. 비용 절감 + 처리량 증가.
- **왜 지금은 아닌가**: Phase 1은 소수 종목으로 시작. 건별 호출로 충분.
- **적합 시점**: Phase 3 (종목 수 30개 이상 시)

### claude-api-call: 태깅 일관성 자동 검증 스킬
- **적용 대상**: 신규 스킬 `validate-tagging-consistency`
- **내용**: 동일 뉴스를 3회 호출해 태깅 결과가 일치하는지 자동 검증하는 스킬. 현재는 검증 기준만 문서로 정의되어 있고 자동화되어 있지 않음. System Prompt 변경 시 회귀 테스트로 활용 가능.
- **왜 지금은 아닌가**: System Prompt가 먼저 확정되어야 기준이 생김.
- **적합 시점**: Phase 1 Step 4 완료 후
