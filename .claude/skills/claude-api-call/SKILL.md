---
name: claude-api-call
description: Java HttpClient로 Claude API를 호출하고 응답을 파싱한다. NT(뉴스 태깅)와 IS(요약) 에이전트가 사용한다.
---

# Claude API Call

## Purpose

Java HttpClient로 Anthropic Claude API를 직접 호출하고 응답을 파싱한다.
NT(News Tagger)와 IS(Intelligence Summarizer) 에이전트가 사용한다.
태깅 일관성이 핵심 — 동일 뉴스 3회 호출 시 동일 결과를 보장해야 한다.

## When to Use

- NT 에이전트가 뉴스에 4개 태그를 부여할 때
- IS 에이전트가 CA 결과를 사람이 읽는 언어로 번역할 때
- System Prompt 변경 후 일관성 검증 시

## API Specification

### Endpoint

```
POST https://api.anthropic.com/v1/messages
```

### Headers

```java
.header("Content-Type", "application/json")
.header("x-api-key", apiKey)           // 환경변수 또는 application.properties
.header("anthropic-version", "2023-06-01")
```

### Model

```
claude-sonnet-4-20250514
```

## System Prompts (확정 — 임의 수정 금지)

### NT Agent System Prompt

```
You are a news tagging agent. Your ONLY job is to assign exactly 4 tags to the given news article.

Tags:
1. relevance: "High" | "Medium" | "Low"
   - High: Article directly mentions a specific stock ticker or financial event
   - Medium: Article addresses a sector or macro environment indirectly
   - Low: World context level, distant from market

2. type: "Fact" | "Opinion"
   - Fact: Contains earnings figures, official announcements, statistical data
   - Opinion: Contains analyst views, forecasts, speculation

3. importance: "High" | "Medium" | "Low"
   - High: Interest rate decisions, earnings reports, M&A, regulation changes
   - Medium: Industry analysis, sector trends
   - Low: Background information, general context

4. tone: "Positive" | "Negative" | "Neutral"
   - Positive: Growth, rise, beat, good-news language dominant
   - Negative: Decline, loss, concern, risk language dominant
   - Neutral: Factual listing, no directional language

IMPORTANT: "tone" identifies linguistic attributes of the text, NOT whether the news is "good for the market."

Return ONLY a JSON object with these 4 fields. No explanation, no commentary.

Example output:
{"relevance": "High", "type": "Fact", "importance": "High", "tone": "Negative"}
```

### IS Agent System Prompt

```
You are an intelligence summarizer. Your ONLY job is to translate machine-readable analysis into human-readable text.

Rules:
1. Translate numbers and states into natural language
   - "RSI 78" → "단기 과열 구간"
   - "volume_ratio 2.3" → "평소 대비 거래량 2.3배 증가"
   - "ALIGNED_BULLISH" → "뉴스와 기술적 지표가 동일한 상승 방향을 가리키고 있습니다"
   - "CONFLICT" → "뉴스와 기술적 지표가 서로 다른 방향을 가리키고 있습니다"

2. NEVER add conclusions. No "therefore", no "thus", no "so you should".

3. NEVER add information not present in the input.

4. NEVER recommend buying or selling.

5. When CONFLICT state, describe BOTH sides equally without choosing one.

Return a natural language summary paragraph in Korean.
```

## Implementation

### Request Body

```java
String requestBody = """
{
  "model": "claude-sonnet-4-20250514",
  "max_tokens": 1024,
  "system": "%s",
  "messages": [
    {"role": "user", "content": "%s"}
  ]
}
""".formatted(systemPrompt, userContent);
```

### HTTP Call

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.anthropic.com/v1/messages"))
    .header("Content-Type", "application/json")
    .header("x-api-key", System.getenv("ANTHROPIC_API_KEY"))
    .header("anthropic-version", "2023-06-01")
    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

### Response Parsing

```java
// 1. HTTP 응답에서 content[0].text 추출
JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
String text = json.getAsJsonArray("content")
    .get(0).getAsJsonObject()
    .get("text").getAsString();

// 2. NT의 경우: text를 다시 JSON 파싱하여 4개 태그 추출
JsonObject tags = JsonParser.parseString(text).getAsJsonObject();
String relevance = tags.get("relevance").getAsString();
String type = tags.get("type").getAsString();
String importance = tags.get("importance").getAsString();
String tone = tags.get("tone").getAsString();
```

## Retry Logic

```java
int maxRetries = 3;
int[] backoffMs = {1000, 2000, 4000};  // 지수 백오프

for (int attempt = 0; attempt < maxRetries; attempt++) {
    HttpResponse<String> response = client.send(request, ...);
    int status = response.statusCode();

    if (status == 200) return parseResponse(response);
    if (status == 429) {  // Rate Limit
        Thread.sleep(backoffMs[attempt]);
        continue;
    }
    if (status >= 500) {  // Server Error
        Thread.sleep(backoffMs[attempt]);
        continue;
    }
    // 4xx (429 제외): 재시도 불필요, 즉시 에러 반환
    throw new ApiException("API error: " + status);
}
throw new ApiException("Max retries exceeded");
```

## Validation Criteria

| 항목 | 기준 |
|------|------|
| 태깅 일관성 | 동일 뉴스 3회 호출 → 4개 필드 모두 동일 |
| 네트워크 오류 | 최대 3회 재시도 후 에러 반환 |
| 파싱 실패 | 프로세스 중단 없이 에러 기록 후 다음 건 진행 |

## Absolute Prohibitions

- System Prompt 임의 수정 금지 — 변경 시 반드시 일관성 재검증
- API 응답을 매매 신호로 해석 금지
- API Key 하드코딩 금지 — 환경변수 또는 application.properties 사용
- temperature 파라미터 임의 조정 금지 (기본값 사용)
