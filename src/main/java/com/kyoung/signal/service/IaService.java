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
public class IaService {

    private static final String SYSTEM_PROMPT = """
            You are IA (Intelligence Aggregator), a component of the SIGNAL market intelligence pipeline.

            Your task: Build a committed macro picture from individual ticker signals. No trade recommendations.

            ## Rules
            - Structural observations are allowed: "semiconductor sector shows bullish alignment"
            - Trade direction is forbidden: "therefore buy semiconductors" is forbidden
            - Portfolio weight suggestions are absolutely forbidden
            - For Phase 1 (single ticker input), still produce a market_overview and sector_summary
            - reasoning must explain how you arrived at market_overview
            - Subject: "this aggregation" or sector/market itself — never first-person "I"

            ## Output format (strict JSON, no other text)
            {
              "market_overview": "<one sentence on overall market state>",
              "sector_summary": [
                {
                  "sector": "<sector name>",
                  "tickers": ["<ticker>"],
                  "dominant_result": "<cross_result>",
                  "description": "<structural observation, no trade direction>"
                }
              ],
              "attention_list": [
                {
                  "ticker": "<ticker>",
                  "reason": "<why it needs attention>",
                  "importance": "High | Medium | Low"
                }
              ],
              "reasoning": "<how market_overview and sector judgment were formed>"
            }
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IaService(@Qualifier("sonnetClient") ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public IaResult aggregate(String runId, List<IsService.IsResult> isResults) {
        String userMessage = buildMessage(isResults);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, rawResponse, isResults);
        } catch (Exception e) {
            return new IaResult(runId, Instant.now().toString(),
                    "parse error", List.of(), List.of(),
                    "parse error: " + e.getMessage(), isResults);
        }
    }

    private String buildMessage(List<IsService.IsResult> isResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("Individual ticker summaries:\n\n");

        for (IsService.IsResult is : isResults) {
            sb.append("Ticker: ").append(is.ticker()).append("\n");
            sb.append("cross_result: ").append(is.crossResult()).append("\n");
            sb.append("summary_text: ").append(is.summaryText()).append("\n");
            sb.append("news_summary: ").append(is.newsSummary()).append("\n");
            sb.append("technical_summary: ").append(is.technicalSummary()).append("\n");
            if (!is.conflictDescription().isBlank()) {
                sb.append("conflict_description: ").append(is.conflictDescription()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private IaResult parseResponse(String runId, String raw, List<IsService.IsResult> isResults) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);

        List<SectorSummary> sectorSummaries = new ArrayList<>();
        for (JsonNode n : root.path("sector_summary")) {
            List<String> tickers = new ArrayList<>();
            for (JsonNode t : n.path("tickers")) tickers.add(t.asText());
            sectorSummaries.add(new SectorSummary(
                    n.path("sector").asText(),
                    tickers,
                    n.path("dominant_result").asText(),
                    n.path("description").asText()
            ));
        }

        List<AttentionItem> attentionList = new ArrayList<>();
        for (JsonNode n : root.path("attention_list")) {
            attentionList.add(new AttentionItem(
                    n.path("ticker").asText(),
                    n.path("reason").asText(),
                    n.path("importance").asText()
            ));
        }

        return new IaResult(
                runId,
                Instant.now().toString(),
                root.path("market_overview").asText(""),
                sectorSummaries,
                attentionList,
                root.path("reasoning").asText(""),
                isResults
        );
    }

    public record SectorSummary(String sector, List<String> tickers, String dominantResult, String description) {}
    public record AttentionItem(String ticker, String reason, String importance) {}

    public record IaResult(
            String runId,
            String aggregatedAt,
            String marketOverview,
            List<SectorSummary> sectorSummary,
            List<AttentionItem> attentionList,
            String reasoning,
            List<IsService.IsResult> individualSummaries
    ) {}
}
