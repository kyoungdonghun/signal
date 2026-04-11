package com.kyoung.signal.service;

import com.kyoung.signal.domain.NewsItem;
import com.kyoung.signal.domain.OhlcvData;
import com.kyoung.signal.domain.TechnicalIndicatorResult;
import com.kyoung.signal.infrastructure.rss.RssFeedClient;
import com.kyoung.signal.infrastructure.yahoo.YahooFinanceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class PipelineService {

    @Value("${pipeline.news.max-items:50}")
    private int newsMaxItems;

    private final YahooFinanceClient yahooFinanceClient;
    private final TechnicalIndicatorCalculator technicalIndicatorCalculator;
    private final RssFeedClient rssFeedClient;
    private final NtService ntService;
    private final TrService trService;
    private final CaService caService;
    private final IsService isService;
    private final IaService iaService;
    private final IpService ipService;

    public PipelineService(YahooFinanceClient yahooFinanceClient,
                           TechnicalIndicatorCalculator technicalIndicatorCalculator,
                           RssFeedClient rssFeedClient,
                           NtService ntService,
                           TrService trService,
                           CaService caService,
                           IsService isService,
                           IaService iaService,
                           IpService ipService) {
        this.yahooFinanceClient = yahooFinanceClient;
        this.technicalIndicatorCalculator = technicalIndicatorCalculator;
        this.rssFeedClient = rssFeedClient;
        this.ntService = ntService;
        this.trService = trService;
        this.caService = caService;
        this.isService = isService;
        this.iaService = iaService;
        this.ipService = ipService;
    }

    public PipelineResult run(String ticker, String rssFeedUrl, String rssFeedSource) {
        String runId = generateRunId(ticker, rssFeedUrl);

        // TA Pipeline: TC → TR
        OhlcvData ohlcvData = yahooFinanceClient.fetch(ticker);
        TechnicalIndicatorResult technical = technicalIndicatorCalculator.calculate(runId, ohlcvData);
        TrService.TrResult trResult = trService.detect(runId, technical);

        // NI Pipeline: NC → NT (NF 생략 — Phase 1 단순화)
        List<NewsItem> news = rssFeedClient.fetch(rssFeedUrl, rssFeedSource);
        List<NewsItem> limitedNews = news.stream().limit(newsMaxItems).toList();
        List<NtService.NtResult> taggedNews = ntService.tagBatch(runId, limitedNews, ticker);

        // IB Pipeline: CA → IS → IA → IP
        CaService.CaResult caResult = caService.aggregate(runId, ticker, taggedNews, trResult);
        IsService.IsResult isResult = isService.summarize(runId, ticker, caResult, taggedNews);
        IaService.IaResult iaResult = iaService.aggregate(runId, List.of(isResult));
        IpService.IpResult ipResult = ipService.present(runId, isResult, iaResult);

        return new PipelineResult(runId, ticker, Instant.now().toString(),
                technical, trResult, taggedNews, caResult, isResult, iaResult, ipResult);
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
            TrService.TrResult tr,
            List<NtService.NtResult> taggedNews,
            CaService.CaResult ca,
            IsService.IsResult is,
            IaService.IaResult ia,
            IpService.IpResult ip
    ) {}
}
