package com.kyoung.signal.service;

import com.kyoung.signal.domain.NewsItem;
import com.kyoung.signal.domain.OhlcvData;
import com.kyoung.signal.domain.TechnicalIndicatorResult;
import com.kyoung.signal.infrastructure.rss.RssFeedClient;
import com.kyoung.signal.infrastructure.yahoo.YahooFinanceClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class PipelineService {

    private final YahooFinanceClient yahooFinanceClient;
    private final TechnicalIndicatorCalculator technicalIndicatorCalculator;
    private final RssFeedClient rssFeedClient;
    private final NtService ntService;

    public PipelineService(YahooFinanceClient yahooFinanceClient,
                           TechnicalIndicatorCalculator technicalIndicatorCalculator,
                           RssFeedClient rssFeedClient,
                           NtService ntService) {
        this.yahooFinanceClient = yahooFinanceClient;
        this.technicalIndicatorCalculator = technicalIndicatorCalculator;
        this.rssFeedClient = rssFeedClient;
        this.ntService = ntService;
    }

    public PipelineResult run(String ticker, String rssFeedUrl, String rssFeedSource) {
        String runId = generateRunId(ticker, rssFeedUrl);

        // TA Pipeline: TC
        OhlcvData ohlcvData = yahooFinanceClient.fetch(ticker);
        TechnicalIndicatorResult technical = technicalIndicatorCalculator.calculate(runId, ohlcvData);

        // NI Pipeline: NC → NT (NF 생략 — Phase 1 단순화)
        List<NewsItem> news = rssFeedClient.fetch(rssFeedUrl, rssFeedSource);
        List<NtService.NtResult> taggedNews = ntService.tagBatch(runId, news, ticker);

        return new PipelineResult(runId, ticker, Instant.now().toString(), technical, taggedNews);
    }

    // run_id: ticker + feedUrl + 현재 날짜(일 단위) 해시
    // 같은 날 같은 입력 → 동일 run_id (재현성 방어선)
    private String generateRunId(String ticker, String feedUrl) {
        try {
            String today = Instant.now().toString().substring(0, 10);
            String raw = ticker + "|" + feedUrl + "|" + today;
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("run_id 생성 실패", e);
        }
    }

    public record PipelineResult(
            String runId,
            String ticker,
            String executedAt,
            TechnicalIndicatorResult technical,
            List<NtService.NtResult> taggedNews
    ) {}
}
