package com.kyoung.signal.service;

import com.kyoung.signal.domain.OutcomeRecordEntity;
import com.kyoung.signal.domain.UserCommitEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CalibrationService {

    public Map<String, Object> evaluate(OutcomeRecordEntity outcome, UserCommitEntity userCommit, String aiCrossResult) {
        Map<String, Object> row = new LinkedHashMap<>();
        String marketOutcome = classifyMarketOutcome(outcome);
        String aiVerdict = classifyCommit(aiCrossResult, marketOutcome);
        String userVerdict = userCommit != null
                ? classifyCommit(userCommit.getUserCrossResult(), marketOutcome)
                : "no_commit";

        row.put("marketOutcome", marketOutcome);
        row.put("aiVerdict", aiVerdict);
        row.put("userVerdict", userVerdict);
        row.put("comparison", compare(aiVerdict, userVerdict));
        return row;
    }

    private String classifyMarketOutcome(OutcomeRecordEntity outcome) {
        if (outcome == null || outcome.getPriceChangePct() == null) {
            return "unknown";
        }
        double changePct = outcome.getPriceChangePct();
        if (changePct > 0) {
            return "bullish";
        }
        if (changePct < 0) {
            return "bearish";
        }
        return "flat";
    }

    private String classifyCommit(String commit, String marketOutcome) {
        if (commit == null || commit.isBlank()) {
            return "no_commit";
        }

        return switch (commit) {
            case "ALIGNED_BULLISH" -> "bullish".equals(marketOutcome) ? "correct" : "incorrect";
            case "ALIGNED_BEARISH" -> "bearish".equals(marketOutcome) ? "correct" : "incorrect";
            case "CONFLICT", "UNCERTAIN" -> "unresolved";
            default -> "unresolved";
        };
    }

    private String compare(String aiVerdict, String userVerdict) {
        if ("correct".equals(aiVerdict) && "correct".equals(userVerdict)) {
            return "both_correct";
        }
        if ("correct".equals(aiVerdict) && "incorrect".equals(userVerdict)) {
            return "ai_leads";
        }
        if ("incorrect".equals(aiVerdict) && "correct".equals(userVerdict)) {
            return "user_leads";
        }
        if ("incorrect".equals(aiVerdict) && "incorrect".equals(userVerdict)) {
            return "both_incorrect";
        }
        if ("correct".equals(aiVerdict) && "no_commit".equals(userVerdict)) {
            return "ai_only";
        }
        return "unresolved";
    }
}
