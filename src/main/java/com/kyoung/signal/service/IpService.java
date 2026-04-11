package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IpService {

    private static final String SYSTEM_PROMPT = """
            You are IP (Intelligence Presenter), a component of the SIGNAL market intelligence pipeline.

            Your task: Structure IS and IA outputs into an inverted pyramid briefing, then invite the user's view.

            ## Inverted pyramid structure
            - Level 1 (top): Items needing immediate attention — CONFLICT or High importance only, max 3
            - Level 2 (middle): Overall market flow and sector highlights
            - Level 3 (bottom): Per-ticker detail with source URLs

            ## Rules
            - closing_statement must be an invitation to dialogue, NOT a disclaimer.
              Required form: "이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?"
            - Do NOT execute trades or suggest one-click actions.
            - Do NOT use language like "looks good", "promising" — post-hoc unverifiable expressions forbidden.
            - Every committed view must carry its reasoning.
            - Do NOT add information not present in IS/IA outputs.
            - Subject: "이번 분석" or "이번 run" — never first-person "저".

            ## Output format (strict JSON, no other text)
            {
              "top_attention": [
                {
                  "ticker": "<ticker>",
                  "status": "<cross_result>",
                  "one_line": "<one sentence describing the conflict or alert>"
                }
              ],
              "market_overview": "<one sentence from IA>",
              "sector_highlights": [
                {
                  "sector": "<name>",
                  "dominant_result": "<result>",
                  "description": "<structural observation>"
                }
              ],
              "details": [
                {
                  "ticker": "<ticker>",
                  "cross_result": "<result>",
                  "summary_text": "<IS summary>",
                  "technical_summary": "<IS technical>",
                  "news_summary": "<IS news>",
                  "source_urls": ["<url>"]
                }
              ],
              "reasoning": "<why these items were placed in top_attention, how the pyramid was structured>",
              "closing_statement": "이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?"
            }
            """;

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IpService(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public IpResult present(String runId, IsService.IsResult isResult, IaService.IaResult iaResult) {
        String userMessage = buildMessage(isResult, iaResult);
        String rawResponse = claudeApiClient.call(SYSTEM_PROMPT, userMessage);

        try {
            return parseResponse(runId, rawResponse);
        } catch (Exception e) {
            return new IpResult(runId, Instant.now().toString(),
                    List.of(), "parse error", List.of(), List.of(),
                    "parse error: " + e.getMessage(),
                    "이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?");
        }
    }

    private String buildMessage(IsService.IsResult is, IaService.IaResult ia) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== IS Output ===\n");
        sb.append("ticker: ").append(is.ticker()).append("\n");
        sb.append("cross_result: ").append(is.crossResult()).append("\n");
        sb.append("summary_text: ").append(is.summaryText()).append("\n");
        sb.append("news_summary: ").append(is.newsSummary()).append("\n");
        sb.append("technical_summary: ").append(is.technicalSummary()).append("\n");
        if (!is.conflictDescription().isBlank()) {
            sb.append("conflict_description: ").append(is.conflictDescription()).append("\n");
        }
        sb.append("source_urls: ").append(is.sourceUrls()).append("\n");

        sb.append("\n=== IA Output ===\n");
        sb.append("market_overview: ").append(ia.marketOverview()).append("\n");
        sb.append("reasoning: ").append(ia.reasoning()).append("\n");
        if (!ia.attentionList().isEmpty()) {
            sb.append("attention_list:\n");
            ia.attentionList().forEach(a ->
                sb.append("  - ").append(a.ticker()).append(": ").append(a.reason()).append(" [").append(a.importance()).append("]\n"));
        }
        if (!ia.sectorSummary().isEmpty()) {
            sb.append("sector_summary:\n");
            ia.sectorSummary().forEach(s ->
                sb.append("  - ").append(s.sector()).append(": ").append(s.description()).append("\n"));
        }

        return sb.toString();
    }

    private IpResult parseResponse(String runId, String raw) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }

        JsonNode root = objectMapper.readTree(json);

        List<TopAttentionItem> topAttention = new ArrayList<>();
        for (JsonNode n : root.path("top_attention")) {
            topAttention.add(new TopAttentionItem(
                    n.path("ticker").asText(),
                    n.path("status").asText(),
                    n.path("one_line").asText()
            ));
        }

        List<SectorHighlight> sectorHighlights = new ArrayList<>();
        for (JsonNode n : root.path("sector_highlights")) {
            sectorHighlights.add(new SectorHighlight(
                    n.path("sector").asText(),
                    n.path("dominant_result").asText(),
                    n.path("description").asText()
            ));
        }

        List<TickerDetail> details = new ArrayList<>();
        for (JsonNode n : root.path("details")) {
            List<String> urls = new ArrayList<>();
            for (JsonNode u : n.path("source_urls")) urls.add(u.asText());
            details.add(new TickerDetail(
                    n.path("ticker").asText(),
                    n.path("cross_result").asText(),
                    n.path("summary_text").asText(),
                    n.path("technical_summary").asText(),
                    n.path("news_summary").asText(),
                    urls
            ));
        }

        return new IpResult(
                runId,
                Instant.now().toString(),
                topAttention,
                root.path("market_overview").asText(""),
                sectorHighlights,
                details,
                root.path("reasoning").asText(""),
                root.path("closing_statement").asText("이번 분석은 이렇게 봤습니다. 당신은 어떻게 보시나요?")
        );
    }

    public record TopAttentionItem(String ticker, String status, String oneLine) {}
    public record SectorHighlight(String sector, String dominantResult, String description) {}
    public record TickerDetail(String ticker, String crossResult, String summaryText,
                               String technicalSummary, String newsSummary, List<String> sourceUrls) {}

    public record IpResult(
            String runId,
            String presentedAt,
            List<TopAttentionItem> topAttention,
            String marketOverview,
            List<SectorHighlight> sectorHighlights,
            List<TickerDetail> details,
            String reasoning,
            String closingStatement
    ) {}
}
