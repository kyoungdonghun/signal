package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CaService {

    private static final String SYSTEM_PROMPT = """
            You are CA (Context Aggregator), a component of the SIGNAL market intelligence pipeline.

            Your task: Cross-validate NT (news tags) and TR (technical state), then commit one of four states with reasoning.

            ## Cross-validation states
            - ALIGNED_BULLISH — news and technical both point positive/stable
            - ALIGNED_BEARISH — news and technical both point negative/unstable
            - CONFLICT — news and technical point in opposite directions
            - UNCERTAIN — one or both inputs are unclear, contradictory within themselves

            ## Rules
            - Do NOT resolve conflicts — surface them clearly. In CONFLICT, do not decide which side is right.
            - Do NOT issue buy/sell conclusions.
            - Read the full NT reasoning and TR reasoning — not just the tag values.
            - Your judgment criteria are your own — SIGNAL does not impose fixed lookup tables.
            - reasoning must be specific and post-hoc verifiable.
            - Subject must be "this cross-validation" or the data itself — never first-person "I".

            ## Output format (strict JSON, no other text)
            {
              "cross_result": "ALIGNED_BULLISH | ALIGNED_BEARISH | CONFLICT | UNCERTAIN",
              "news_direction": "bullish | bearish | unclear",
              "technical_direction": "stable | unstable | unclear",
              "conflict_points": [
                { "source": "NT vs TR", "description": "<specific conflict>" }
              ],
              "confidence": "High | Medium | Low",
              "reasoning": "<why this cross_result, referencing specific NT and TR data>"
            }
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CaService(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public CaResult aggregate(String runId, String ticker,
                               List<NtService.NtResult> ntResults,
                               TrService.TrResult trResult) {
        String userMessage = buildMessage(ticker, ntResults, trResult);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, ticker, rawResponse);
        } catch (Exception e) {
            return new CaResult(runId, ticker, Instant.now().toString(),
                    "UNCERTAIN", "unclear", "unclear", List.of(), "Low",
                    "parse error: " + e.getMessage());
        }
    }

    private String buildMessage(String ticker, List<NtService.NtResult> ntResults,
                                 TrService.TrResult tr) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticker: ").append(ticker).append("\n\n");

        sb.append("=== TR (Technical) ===\n");
        sb.append("Stability: ").append(tr.stability()).append("\n");
        sb.append("Confidence: ").append(tr.confidence()).append("\n");
        sb.append("Reasoning: ").append(tr.reasoning()).append("\n");
        if (!tr.conflicts().isEmpty()) {
            sb.append("Conflicts:\n");
            tr.conflicts().forEach(c -> sb.append("  - [").append(c.type()).append("] ").append(c.description()).append("\n"));
        }
        if (!tr.warnings().isEmpty()) {
            sb.append("Warnings:\n");
            tr.warnings().forEach(w -> sb.append("  - [").append(w.type()).append("] ").append(w.description()).append("\n"));
        }

        sb.append("\n=== NT (News Tags) ===\n");
        for (int i = 0; i < ntResults.size(); i++) {
            NtService.NtResult n = ntResults.get(i);
            sb.append("Item ").append(i).append(": ").append(n.title()).append("\n");
            sb.append("  relevance=").append(n.relevance())
              .append(", importance=").append(n.importance())
              .append(", tone=").append(n.tone()).append("\n");
            sb.append("  reasoning(tone): ").append(n.reasoningTone()).append("\n");
        }

        return sb.toString();
    }

    private CaResult parseResponse(String runId, String ticker, String raw) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);

        List<ConflictPoint> conflictPoints = new ArrayList<>();
        for (JsonNode n : root.path("conflict_points")) {
            conflictPoints.add(new ConflictPoint(n.path("source").asText(), n.path("description").asText()));
        }

        return new CaResult(
                runId,
                ticker,
                Instant.now().toString(),
                root.path("cross_result").asText("UNCERTAIN"),
                root.path("news_direction").asText("unclear"),
                root.path("technical_direction").asText("unclear"),
                conflictPoints,
                root.path("confidence").asText("Low"),
                root.path("reasoning").asText("")
        );
    }

    public record ConflictPoint(String source, String description) {}

    public record CaResult(
            String runId,
            String ticker,
            String aggregatedAt,
            String crossResult,
            String newsDirection,
            String technicalDirection,
            List<ConflictPoint> conflictPoints,
            String confidence,
            String reasoning
    ) {}
}
