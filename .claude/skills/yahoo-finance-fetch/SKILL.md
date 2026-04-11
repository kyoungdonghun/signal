---
name: yahoo-finance-fetch
description: Yahoo Finance 공개 HTTP API로 미국/한국 주식 OHLCV 데이터를 수집한다. TC 에이전트의 입력 데이터 소스.
---

# Yahoo Finance Fetch

## Purpose

Yahoo Finance 공개 HTTP API를 Java HttpClient로 직접 호출하여 OHLCV(시가/고가/저가/종가/거래량) 데이터를 수집한다.
TC(Tech Calculator) 에이전트가 이 데이터를 받아 기술적 지표를 계산한다.

## When to Use

- TC 에이전트에 입력할 가격 데이터가 필요할 때
- 새로운 종목을 분석 대상에 추가할 때
- 데이터 품질 검증이 필요할 때

## API Specification

### Endpoint

```
GET https://query1.finance.yahoo.com/v8/finance/chart/{ticker}
```

### Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| interval  | 1d    | 일봉 데이터 |
| range     | 6mo   | 최근 6개월  |

### Ticker Rules

| Market | Suffix | Example |
|--------|--------|---------|
| US (S&P500, NASDAQ) | 없음 | AAPL, MSFT, GOOGL |
| KR (KOSPI) | .KS | 005930.KS (삼성전자) |
| KR (KOSDAQ) | .KQ | 035720.KQ (카카오게임즈) |

## Implementation

Java HttpClient를 사용한 직접 호출. yfinance 등 Python 라이브러리 사용 금지.

```java
HttpClient client = HttpClient.newHttpClient();
String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker
    + "?interval=1d&range=6mo";
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .header("User-Agent", "Mozilla/5.0")
    .GET()
    .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

## Data Quality Validation (필수)

수집 후 반드시 3단계 검증을 수행한다. 하나라도 실패하면 TC에 error를 전달한다.

### 1. 누락값 검증
- timestamp 또는 OHLCV 중 null이 있는 날짜 제거
- 제거 후 남은 데이터로 재검증

### 2. 날짜 정렬 검증
- timestamp가 오름차순(과거 → 현재)인지 확인
- 역순이면 정렬 후 진행

### 3. 필드 완전성 검증
- 각 레코드에 O, H, L, C, V 5개 필드가 모두 존재하는지 확인

## Output Format

TC 에이전트에 전달하는 JSON 형식:

```json
{
  "ticker": "AAPL",
  "market": "US",
  "fetched_at": "2026-03-29T09:00:00Z",
  "data_quality": "ok",
  "error_detail": null,
  "records": [
    {
      "date": "2026-03-28",
      "open": 150.00,
      "high": 152.50,
      "low": 149.80,
      "close": 151.30,
      "volume": 45000000
    }
  ]
}
```

## Validation Criteria

| 항목 | 기준 |
|------|------|
| 대상 종목 | 미국 3개 + 한국 2개 (총 5개) |
| 최소 데이터 | 60거래일 이상 |
| 누락값 | 0건 (제거 후 기준) |
| 날짜 정렬 | 오름차순 |
| 필드 완전성 | O/H/L/C/V 전부 존재 |

## Absolute Prohibitions

- yfinance, pandas 등 Python 라이브러리 사용 금지
- 데이터 품질 검증 생략 금지
- 검증 실패 시 계산 진행 금지 — 반드시 error 상태로 TC에 전달
