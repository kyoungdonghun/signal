---
name: rss-feed-fetch
description: RSS 피드에서 뉴스를 수집하고 앞부분 300자를 추출한다. NC 에이전트의 수집 도구.
---

# RSS Feed Fetch

## Purpose

신뢰할 수 있는 RSS 소스에서 뉴스를 수집하고, 본문 앞부분 300자를 추출한다.
NC(News Collector) 에이전트가 이 스킬로 수집한 데이터를 NF에 전달한다.
수집만 한다. 필터링, 해석, 판단은 하지 않는다.

## When to Use

- NC 에이전트가 뉴스를 수집할 때
- 새로운 RSS 소스를 추가/검증할 때
- 수집 파이프라인 장애 진단 시

## Source List (Phase 1)

| Type | Source | URL | Language |
|------|--------|-----|----------|
| market_direct | Reuters | RSS Feed URL | EN |
| market_direct | 한국경제 | RSS Feed URL | KR |
| context | 연합뉴스 | RSS Feed URL | KR |

Phase 2 확장 예정: Bloomberg, WSJ, FT, 연합인포맥스

## Implementation

### Recommended: Rome Library

```java
// Rome 라이브러리 (v2.1.0) 사용
SyndFeedInput input = new SyndFeedInput();
SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));

for (SyndEntry entry : feed.getEntries()) {
    String title = entry.getTitle();
    String link = entry.getLink();
    Date published = entry.getPublishedDate();
    String body = entry.getDescription() != null
        ? entry.getDescription().getValue()
        : "";
}
```

### Alternative: Direct XML Parsing

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
DocumentBuilder builder = factory.newDocumentBuilder();
Document doc = builder.parse(new URL(rssUrl).openStream());
NodeList items = doc.getElementsByTagName("item");
```

## 300-Character Extraction Rule

소스마다 본문 형식이 다르다. 300자 추출로 형식 차이를 흡수한다.

```java
// 1. HTML 태그 제거
String clean = rawBody.replaceAll("<[^>]+>", "").trim();
// 2. 300자 추출
String excerpt = clean.length() > 300 ? clean.substring(0, 300) : clean;
```

## Output Format

NF 에이전트에 전달하는 JSON 형식:

```json
{
  "source_type": "market_direct",
  "source_name": "Reuters",
  "title": "Fed Holds Interest Rates Steady",
  "excerpt": "앞부분 300자...",
  "url": "https://...",
  "collected_at": "2026-03-29T09:00:00Z",
  "fetch_status": "ok",
  "error_detail": null
}
```

## Error Handling

- 소스 응답 없음 → `fetch_status: "error"`, `error_detail` 에 사유 기록, 다음 소스로 계속 진행
- 파싱 실패 → 동일하게 error 기록 후 계속 진행
- 전체 소스 실패 시 → NF에 빈 배열 + 경고 전달

## Validation Criteria

| 항목 | 기준 |
|------|------|
| 대상 | 각 소스 최신 기사 10개 |
| title | 누락 없음 |
| url | 누락 없음 |
| collected_at | 유효한 ISO8601 timestamp |
| excerpt | 300자 이하 |
| 정렬 | 시간 역순 (최신 → 과거) |

## Absolute Prohibitions

- 감성 판단 금지 (긍정/부정 분류는 NT의 역할)
- 중복 탐지 금지 (NF의 역할)
- 중요도 판단 금지 (NT의 역할)
- 수집 범위 임의 확장 금지
- 원문 내용 수정 금지 — 있는 그대로 전달
