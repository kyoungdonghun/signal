package com.kyoung.signal.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.NewsItem;
import com.kyoung.signal.domain.PipelineRunEntity;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import com.kyoung.signal.repository.PipelineRunRepository;
import com.kyoung.signal.service.NtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NT 모델 비교 — Haiku vs Sonnet (기존 저장된 run의 뉴스로 오프라인 재태깅 비교)
 * GET /api/nt-compare/{runId}
 */
@RestController
@RequestMapping("/api/nt-compare")
public class NtCompareController {

    private final PipelineRunRepository runRepository;
    private final NtService ntService;
    private final ClaudeApiClient haikuClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NtCompareController(PipelineRunRepository runRepository,
                               NtService ntService,
                               @Qualifier("haikuClient") ClaudeApiClient haikuClient) {
        this.runRepository = runRepository;
        this.ntService = ntService;
        this.haikuClient = haikuClient;
    }

    @GetMapping("/{runId}")
    public ResponseEntity<?> compare(@PathVariable String runId) {
        PipelineRunEntity entity = runRepository.findByRunId(runId)
                .orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        String ticker = entity.getTicker();

        // fullResult JSON에서 taggedNews(Sonnet 결과) 추출
        List<NtService.NtResult> sonnetResults;
        try {
            JsonNode root = objectMapper.readTree(entity.getFullResult());
            sonnetResults = parseTaggedNews(runId, root.path("taggedNews"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "fullResult 파싱 실패: " + e.getMessage()));
        }

        // NtResult → NewsItem 역변환 (title + excerpt + url + source 보존됨)
        List<NewsItem> newsItems = sonnetResults.stream()
                .map(r -> new NewsItem(r.title(), r.url(), r.excerpt(), r.sourceName(), Instant.now()))
                .toList();

        // Haiku로 재태깅
        System.out.println("[NtCompare] Haiku 재태깅 시작 — runId: " + runId + ", 항목 수: " + newsItems.size());
        List<NtService.NtResult> haikuResults = ntService.tagBatch(haikuClient, runId + "_haiku", newsItems, ticker);
        System.out.println("[NtCompare] Haiku 재태깅 완료");

        // 비교 결과 생성
        List<Map<String, Object>> items = new ArrayList<>();
        int matchCount = 0;
        int relevanceDiff = 0, importanceDiff = 0, toneDiff = 0;

        for (int i = 0; i < sonnetResults.size(); i++) {
            NtService.NtResult s = sonnetResults.get(i);
            NtService.NtResult h = i < haikuResults.size() ? haikuResults.get(i) : null;

            boolean relMatch = eq(s.relevance(), h != null ? h.relevance() : null);
            boolean impMatch = eq(s.importance(), h != null ? h.importance() : null);
            boolean toneMatch = eq(s.tone(), h != null ? h.tone() : null);
            boolean allMatch = relMatch && impMatch && toneMatch;

            if (allMatch) matchCount++;
            if (!relMatch) relevanceDiff++;
            if (!impMatch) importanceDiff++;
            if (!toneDiff(toneMatch)) toneDiff++;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("index", i);
            item.put("title", s.title());
            item.put("sonnet", Map.of(
                    "relevance", s.relevance() != null ? s.relevance() : "null",
                    "importance", s.importance() != null ? s.importance() : "null",
                    "tone", s.tone() != null ? s.tone() : "null"
            ));
            item.put("haiku", h != null ? Map.of(
                    "relevance", h.relevance() != null ? h.relevance() : "null",
                    "importance", h.importance() != null ? h.importance() : "null",
                    "tone", h.tone() != null ? h.tone() : "null"
            ) : Map.of());
            item.put("match", allMatch);
            items.add(item);
        }

        int total = sonnetResults.size();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", total);
        summary.put("matchCount", matchCount);
        summary.put("matchRate", total > 0 ? Math.round((double) matchCount / total * 1000) / 10.0 : 0);
        summary.put("relevanceDiff", relevanceDiff);
        summary.put("importanceDiff", importanceDiff);
        summary.put("toneDiff", toneDiff);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runId", runId);
        result.put("ticker", ticker);
        result.put("summary", summary);
        result.put("items", items);

        return ResponseEntity.ok(result);
    }

    private List<NtService.NtResult> parseTaggedNews(String runId, JsonNode array) {
        List<NtService.NtResult> list = new ArrayList<>();
        for (JsonNode n : array) {
            list.add(new NtService.NtResult(
                    runId,
                    n.path("sourceName").asText(""),
                    n.path("title").asText(""),
                    n.path("excerpt").asText(""),
                    n.path("url").asText(""),
                    n.path("taggedAt").asText(""),
                    nullableText(n, "relevance"),
                    nullableText(n, "type"),
                    nullableText(n, "importance"),
                    nullableText(n, "tone"),
                    null, null, null, null
            ));
        }
        return list;
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode child = node.path(field);
        if (child.isNull() || child.isMissingNode()) return null;
        String val = child.asText();
        return "null".equals(val) ? null : val;
    }

    private boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    // 메서드명 충돌 방지용 래퍼
    private boolean toneDiff(boolean match) {
        return match;
    }
}
