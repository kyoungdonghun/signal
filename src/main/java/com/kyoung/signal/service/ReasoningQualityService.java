package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.PipelineRunEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReasoningQualityService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ReasoningCheck> evaluate(PipelineRunEntity run) {
        List<ReasoningCheck> checks = new ArrayList<>();
        if (run.getFullResult() == null || run.getFullResult().isBlank()) {
            return checks;
        }

        try {
            JsonNode root = objectMapper.readTree(run.getFullResult());
            addCheck(checks, "tr.reasoning", root.path("tr").path("reasoning").asText(""));
            addCheck(checks, "ca.reasoning", root.path("ca").path("reasoning").asText(""));
            addCheck(checks, "is.reasoning", root.path("is").path("reasoning").asText(""));
            addCheck(checks, "ia.reasoning", root.path("ia").path("reasoning").asText(""));
            addCheck(checks, "ip.reasoning", root.path("ip").path("reasoning").asText(""));
        } catch (Exception e) {
            checks.add(new ReasoningCheck("full_result", "invalid", 0, false, false, false, "Could not parse full_result JSON."));
        }

        return checks;
    }

    private void addCheck(List<ReasoningCheck> checks, String path, String reasoning) {
        String trimmed = reasoning != null ? reasoning.trim() : "";
        int score = 0;

        boolean hasSpecificReference = containsDigit(trimmed) || containsKeyword(trimmed);
        boolean hasConnection = containsConnectionWord(trimmed);
        boolean hasFalsifiability = containsFalsifiabilityCue(trimmed);

        if (hasSpecificReference) score++;
        if (hasConnection) score++;
        if (hasFalsifiability) score++;

        String status = switch (score) {
            case 3 -> "strong";
            case 2 -> "partial";
            default -> "weak";
        };

        String note = switch (status) {
            case "strong" -> "Meets the current observational reasoning baseline.";
            case "partial" -> "Has some reasoning structure but is still incomplete.";
            default -> "Too vague for reliable post-hoc review.";
        };

        checks.add(new ReasoningCheck(path, status, score, hasSpecificReference, hasConnection, hasFalsifiability, note));
    }

    private boolean containsDigit(String text) {
        return text != null && text.chars().anyMatch(Character::isDigit);
    }

    private boolean containsKeyword(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return lower.contains("rsi")
                || lower.contains("ma20")
                || lower.contains("ma60")
                || lower.contains("volume")
                || lower.contains("cross")
                || lower.contains("conflict")
                || text.contains("거래량")
                || text.contains("지지")
                || text.contains("저항")
                || text.contains("뉴스")
                || text.contains("근거");
    }

    private boolean containsConnectionWord(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return lower.contains("because")
                || lower.contains("therefore")
                || lower.contains("driven by")
                || lower.contains("led to")
                || text.contains("때문")
                || text.contains("따라서")
                || text.contains("보므로")
                || text.contains("이어져")
                || text.contains("근거로");
    }

    private boolean containsFalsifiabilityCue(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return lower.contains("if")
                || lower.contains("unless")
                || lower.contains("would weaken")
                || lower.contains("would invalidate")
                || text.contains("깨지면")
                || text.contains("유지되면")
                || text.contains("아니면")
                || text.contains("반증");
    }

    public record ReasoningCheck(
            String path,
            String status,
            int score,
            boolean hasSpecificReference,
            boolean hasConnection,
            boolean hasFalsifiability,
            String note
    ) {}
}
