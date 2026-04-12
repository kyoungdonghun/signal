package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IsService {

    private static final String SYSTEM_PROMPT = """
            You are IS (Intelligence Summarizer), a component of the SIGNAL market intelligence pipeline.

            Your task: Translate CA's machine output into natural human language. This is translation, not new analysis.

            ## Rules
            - Translate CA's cross_result, reasoning, and conflict_points into readable sentences.
            - Do NOT start any sentence with "Therefore" — you are not drawing new conclusions.
            - Do NOT add information not present in CA's output.
            - Do NOT lean toward either bullish or bearish framing.
            - When CONFLICT: describe both sides clearly and equally.
            - Subject must be "this analysis" or the ticker — never first-person "I".
            - reasoning must explain which CA elements drove your translation choices.

            ## Output format (strict JSON, no other text)
            {
              "cross_result": "<echo from CA>",
              "summary_text": "<2-4 sentences in natural language, subject: 'this analysis' or ticker name>",
              "news_summary": "<one sentence on news direction>",
              "technical_summary": "<one sentence on technical state>",
              "conflict_description": "<only if CONFLICT — describe both sides equally>",
              "reasoning": "<which CA elements shaped this translation and why>",
              "source_urls": ["<url1>", "<url2>"]
            }
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IsService(@Qualifier("sonnetClient") ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public IsResult summarize(String runId, String ticker,
                               CaService.CaResult ca,
                               List<NtService.NtResult> ntResults) {
        String userMessage = buildMessage(ticker, ca, ntResults);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, ticker, rawResponse);
        } catch (Exception e) {
            return new IsResult(runId, ticker, Instant.now().toString(),
                    ca.crossResult(), "parse error", "", "", "",
                    "parse error: " + e.getMessage(), List.of());
        }
    }

    private String buildMessage(String ticker, CaService.CaResult ca,
                                 List<NtService.NtResult> ntResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticker: ").append(ticker).append("\n\n");

        sb.append("=== CA Output ===\n");
        sb.append("cross_result: ").append(ca.crossResult()).append("\n");
        sb.append("news_direction: ").append(ca.newsDirection()).append("\n");
        sb.append("technical_direction: ").append(ca.technicalDirection()).append("\n");
        sb.append("confidence: ").append(ca.confidence()).append("\n");
        sb.append("reasoning: ").append(ca.reasoning()).append("\n");
        if (!ca.conflictPoints().isEmpty()) {
            sb.append("conflict_points:\n");
            ca.conflictPoints().forEach(p -> sb.append("  - ").append(p.description()).append("\n"));
        }

        sb.append("\n=== News URLs (High/Medium relevance only) ===\n");
        ntResults.stream()
                .filter(n -> !("Low".equals(n.relevance()) && "Low".equals(n.importance())))
                .forEach(n -> sb.append(n.url()).append("\n"));

        return sb.toString();
    }

    private IsResult parseResponse(String runId, String ticker, String raw) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);

        List<String> sourceUrls = new ArrayList<>();
        for (JsonNode u : root.path("source_urls")) {
            sourceUrls.add(u.asText());
        }

        return new IsResult(
                runId,
                ticker,
                Instant.now().toString(),
                root.path("cross_result").asText("UNCERTAIN"),
                root.path("summary_text").asText(""),
                root.path("news_summary").asText(""),
                root.path("technical_summary").asText(""),
                root.path("conflict_description").asText(""),
                root.path("reasoning").asText(""),
                sourceUrls
        );
    }

    public record IsResult(
            String runId,
            String ticker,
            String summarizedAt,
            String crossResult,
            String summaryText,
            String newsSummary,
            String technicalSummary,
            String conflictDescription,
            String reasoning,
            List<String> sourceUrls
    ) {}
}
