package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.TechnicalIndicatorResult;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrService {

    private static final String SYSTEM_PROMPT = """
            You are TR (Tech Reasoner), a component of the SIGNAL market intelligence pipeline.

            Your task: Detect instability and conflicts among technical indicators, then commit a state with reasoning.

            ## What to detect

            1. conflicts — Do MA20, MA60, RSI, and volume ratio point in different directions?
               List each conflict as {type, description}.

            2. warnings — Does any indicator significantly deviate from normal range for this ticker/context?
               List each warning as {type, description}.

            3. stability — Overall state: "stable" | "unstable" | "unknown"

            4. confidence — How reliable is this detection: "High" | "Medium" | "Low"

            5. level_commit (optional) — Are there specific price levels that are verifiably significant?
               If MA20, MA60, or price structure suggests a meaningful support or resistance level,
               publicly commit that observation. This is NOT a trade signal.
               "This analysis sees MA20 = X as short-term support" = allowed (verifiable observation).
               "Buy at MA20" = forbidden (trade signal).
               If no meaningful level exists, return an empty array [].

            ## Rules
            - Do NOT produce buy/sell signals or timing conclusions.
            - Do NOT translate instability into trade language.
            - Every commit (stability, confidence) requires a reasoning sentence.
            - Subject must be "this detection" or the indicator itself — never first-person "I".
            - reasoning must be specific enough for post-hoc verification.

            ## Output format (strict JSON, no other text)
            {
              "stability": "stable | unstable | unknown",
              "confidence": "High | Medium | Low",
              "conflicts": [
                { "type": "<label>", "description": "<one sentence>" }
              ],
              "warnings": [
                { "type": "<label>", "description": "<one sentence>" }
              ],
              "level_commit": [
                {
                  "level": 183500.0,
                  "type": "support | resistance",
                  "basis": "MA20 | MA60 | price_action",
                  "description": "<why this level is significant — verifiable, no trade directive>"
                }
              ],
              "reasoning": "<why this stability/confidence was committed, referencing specific values>"
            }
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrService(@Qualifier("sonnetClient") ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public TrResult detect(String runId, TechnicalIndicatorResult technical) {
        if (technical.getPrice() == null || technical.getRsi() == null || technical.getVolume() == null) {
            return new TrResult(runId, technical.getTicker(), Instant.now().toString(),
                    "unknown", "Low", List.of(), List.of(), List.of(),
                    "TC 데이터 부족으로 TR 분석 불가: " + technical.getErrorDetail());
        }

        String userMessage = buildMessage(technical);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, technical.getTicker(), rawResponse);
        } catch (Exception e) {
            return new TrResult(runId, technical.getTicker(), Instant.now().toString(),
                    "unknown", "Low", List.of(), List.of(), List.of(),
                    "parse error: " + e.getMessage());
        }
    }

    private String buildMessage(TechnicalIndicatorResult t) {
        return """
                Ticker: %s
                Current Price: %.2f
                MA20: %.2f
                MA60: %.2f
                RSI(14): %.2f
                Volume (current): %d
                Volume Avg20: %.0f
                Volume Ratio: %.4f
                Data Quality: %s
                """.formatted(
                t.getTicker(),
                t.getPrice().getCurrent(),
                t.getPrice().getMa20(),
                t.getPrice().getMa60(),
                t.getRsi().getValue(),
                t.getVolume().getCurrent(),
                t.getVolume().getAvg20(),
                t.getVolume().getRatio(),
                t.getDataQuality()
        );
    }

    private TrResult parseResponse(String runId, String ticker, String raw) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);

        List<ConflictItem> conflicts = new ArrayList<>();
        for (JsonNode n : root.path("conflicts")) {
            conflicts.add(new ConflictItem(n.path("type").asText(), n.path("description").asText()));
        }

        List<WarningItem> warnings = new ArrayList<>();
        for (JsonNode n : root.path("warnings")) {
            warnings.add(new WarningItem(n.path("type").asText(), n.path("description").asText()));
        }

        List<LevelCommitItem> levelCommits = new ArrayList<>();
        for (JsonNode n : root.path("level_commit")) {
            levelCommits.add(new LevelCommitItem(
                    n.path("level").asDouble(0),
                    n.path("type").asText(""),
                    n.path("basis").asText(""),
                    n.path("description").asText("")
            ));
        }

        return new TrResult(
                runId,
                ticker,
                Instant.now().toString(),
                root.path("stability").asText("unknown"),
                root.path("confidence").asText("Low"),
                conflicts,
                warnings,
                levelCommits,
                root.path("reasoning").asText("")
        );
    }

    public record ConflictItem(String type, String description) {}
    public record WarningItem(String type, String description) {}
    public record LevelCommitItem(double level, String type, String basis, String description) {}

    public record TrResult(
            String runId,
            String ticker,
            String reasonedAt,
            String stability,
            String confidence,
            List<ConflictItem> conflicts,
            List<WarningItem> warnings,
            List<LevelCommitItem> levelCommits,
            String reasoning
    ) {}
}
