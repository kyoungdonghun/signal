package com.kyoung.signal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.*;
import com.kyoung.signal.infrastructure.rss.RssFeedClient;
import com.kyoung.signal.infrastructure.yahoo.YahooFinanceClient;
import com.kyoung.signal.repository.PipelineRunRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final PipelineRunRepository pipelineRunRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PipelineService(YahooFinanceClient yahooFinanceClient,
                           TechnicalIndicatorCalculator technicalIndicatorCalculator,
                           RssFeedClient rssFeedClient,
                           NtService ntService,
                           TrService trService,
                           CaService caService,
                           IsService isService,
                           IaService iaService,
                           IpService ipService,
                           PipelineRunRepository pipelineRunRepository) {
        this.yahooFinanceClient = yahooFinanceClient;
        this.technicalIndicatorCalculator = technicalIndicatorCalculator;
        this.rssFeedClient = rssFeedClient;
        this.ntService = ntService;
        this.trService = trService;
        this.caService = caService;
        this.isService = isService;
        this.iaService = iaService;
        this.ipService = ipService;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public PipelineResult run(String ticker, String rssFeedUrl, String rssFeedSource) {
        String runId = generateRunId(ticker, rssFeedUrl);

        // 같은 날 동일 run_id가 이미 저장돼 있으면 저장 스킵 (재실행은 허용)
        boolean alreadySaved = pipelineRunRepository.existsByRunId(runId);
        if (alreadySaved) {
            System.out.println("[PipelineService] 이미 저장된 run_id — 재실행하되 저장 스킵: " + runId);
        }

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

        PipelineResult result = new PipelineResult(runId, ticker, Instant.now().toString(),
                technical, trResult, taggedNews, caResult, isResult, iaResult, ipResult);

        // DB 저장 (중복 run_id는 스킵)
        if (!alreadySaved) {
            save(result, technical);
        }

        return result;
    }

    private void save(PipelineResult result, TechnicalIndicatorResult technical) {
        try {
            String fullJson = objectMapper.writeValueAsString(result);

            PipelineRunEntity entity = PipelineRunEntity.of(
                    result.runId(),
                    result.ticker(),
                    LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                    result.tr().stability(),
                    result.tr().confidence(),
                    result.ca().crossResult(),
                    result.ca().confidence(),
                    technical.getPrice() != null ? technical.getPrice().getCurrent() : null,
                    technical.getPrice() != null ? technical.getPrice().getMa20() : null,
                    technical.getPrice() != null ? technical.getPrice().getMa60() : null,
                    technical.getRsi() != null ? technical.getRsi().getValue() : null,
                    technical.getVolume() != null ? technical.getVolume().getRatio() : null,
                    fullJson
            );

            pipelineRunRepository.save(entity);
        } catch (Exception e) {
            // 저장 실패는 파이프라인 결과에 영향 주지 않음 — 로그만
            System.err.println("[PipelineService] DB 저장 실패: " + e.getMessage());
        }
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
