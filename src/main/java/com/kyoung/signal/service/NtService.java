package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.NewsItem;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NtService {

    private static final int BATCH_SIZE = 20;

    private static final String SYSTEM_PROMPT = """
            You are NT (News Tagger), a component of the SIGNAL market intelligence pipeline.

            Your task: Tag each news item in the list with exactly 4 fields. Each tag is a commit — state the value and your reasoning.

            ## Fields

            1. relevance — How directly does this news touch a specific ticker, sector, or macro?
               Allowed values: "High" | "Medium" | "Low" | null

            2. type — Is the claim backed by hard data/official figures, or is it opinion/forecast?
               Allowed values: "Fact" | "Opinion" | null

            3. importance — Can this news cause an immediate market reaction?
               Allowed values: "High" | "Medium" | "Low" | null

            4. tone — What is the linguistic direction of the text itself?
               (Not "is this good for the market" — only the language used in the text.)
               Allowed values: "Positive" | "Negative" | "Neutral" | null

            ## Rules
            - If uncertain, use null — never guess.
            - Every field (including null) requires a reasoning sentence.
            - Do NOT interpret market impact or suggest trade direction.
            - Do NOT use first-person "I". Subject must be "this tagging" or the news item itself.
            - Process each item independently. Do not let one item influence another.

            ## Output format (strict JSON array, no other text)
            [
              {
                "index": 0,
                "tags": {
                  "relevance": "<value>",
                  "type": "<value>",
                  "importance": "<value>",
                  "tone": "<value>"
                },
                "reasoning": {
                  "relevance": "<one sentence>",
                  "type": "<one sentence>",
                  "importance": "<one sentence>",
                  "tone": "<one sentence>"
                }
              }
            ]
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NtService(@Qualifier("sonnetClient") ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public List<NtResult> tagBatch(String runId, List<NewsItem> items, String ticker) {
        List<NtResult> results = new ArrayList<>();

        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<NewsItem> chunk = items.subList(i, Math.min(i + BATCH_SIZE, items.size()));
            results.addAll(processChunk(runId, chunk, ticker, i));
        }

        return results;
    }

    private List<NtResult> processChunk(String runId, List<NewsItem> chunk, String ticker, int offset) {
        String userMessage = buildBatchMessage(chunk, ticker);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseBatchResponse(runId, chunk, rawResponse, offset);
        } catch (Exception e) {
            // 청크 파싱 실패 시 해당 청크 전체를 null 태깅으로 폴백
            List<NtResult> fallback = new ArrayList<>();
            for (NewsItem item : chunk) {
                fallback.add(nullResult(runId, item));
            }
            return fallback;
        }
    }

    private String buildBatchMessage(List<NewsItem> items, String ticker) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticker context: ").append(ticker).append("\n\n");

        for (int i = 0; i < items.size(); i++) {
            NewsItem item = items.get(i);
            sb.append("--- Item ").append(i).append(" ---\n");
            sb.append("Source: ").append(item.getSource()).append("\n");
            sb.append("Title: ").append(item.getTitle()).append("\n");
            sb.append("Excerpt: ").append(item.getExcerpt()).append("\n\n");
        }

        return sb.toString();
    }

    private List<NtResult> parseBatchResponse(String runId, List<NewsItem> items,
                                               String raw, int offset) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode array = objectMapper.readTree(json);
        List<NtResult> results = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            NewsItem item = items.get(i);
            JsonNode node = findByIndex(array, i);

            if (node == null) {
                results.add(nullResult(runId, item));
                continue;
            }

            JsonNode tags = node.path("tags");
            JsonNode reasoning = node.path("reasoning");

            results.add(new NtResult(
                    runId,
                    item.getSource(),
                    item.getTitle(),
                    item.getExcerpt(),
                    item.getLink(),
                    Instant.now().toString(),
                    tags.path("relevance").asText(null),
                    tags.path("type").asText(null),
                    tags.path("importance").asText(null),
                    tags.path("tone").asText(null),
                    reasoning.path("relevance").asText(null),
                    reasoning.path("type").asText(null),
                    reasoning.path("importance").asText(null),
                    reasoning.path("tone").asText(null)
            ));
        }

        return results;
    }

    private JsonNode findByIndex(JsonNode array, int index) {
        for (JsonNode node : array) {
            if (node.path("index").asInt(-1) == index) return node;
        }
        return null;
    }

    private NtResult nullResult(String runId, NewsItem item) {
        return new NtResult(runId, item.getSource(), item.getTitle(),
                item.getExcerpt(), item.getLink(), Instant.now().toString(),
                null, null, null, null,
                "parse error", "parse error", "parse error", "parse error");
    }

    public record NtResult(
            String runId,
            String sourceName,
            String title,
            String excerpt,
            String url,
            String taggedAt,
            String relevance,
            String type,
            String importance,
            String tone,
            String reasoningRelevance,
            String reasoningType,
            String reasoningImportance,
            String reasoningTone
    ) {}
}
