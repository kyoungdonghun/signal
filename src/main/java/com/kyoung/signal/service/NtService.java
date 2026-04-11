package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.NewsItem;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NtService {

    private static final String SYSTEM_PROMPT = """
            You are NT (News Tagger), a component of the SIGNAL market intelligence pipeline.

            Your task: Tag a single news item with exactly 4 fields. Each tag is a commit — state the value and your reasoning.

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

            ## Output format (strict JSON, no other text)
            {
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
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NtService(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public NtResult tag(String runId, NewsItem item, String ticker) {
        String userMessage = buildUserMessage(item, ticker);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, item, rawResponse);
        } catch (Exception e) {
            throw new RuntimeException("NT 응답 파싱 실패 (title=" + item.getTitle() + ")", e);
        }
    }

    private String buildUserMessage(NewsItem item, String ticker) {
        return """
                Ticker context: %s
                Source: %s
                Title: %s
                Excerpt: %s
                """.formatted(ticker, item.getSource(), item.getTitle(), item.getExcerpt());
    }

    private NtResult parseResponse(String runId, NewsItem item, String raw) throws Exception {
        // Claude가 JSON 블록으로 감싸는 경우 처리
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode tags = root.path("tags");
        JsonNode reasoning = root.path("reasoning");

        return new NtResult(
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
        );
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
