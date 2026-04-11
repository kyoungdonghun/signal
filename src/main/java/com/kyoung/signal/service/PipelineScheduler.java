package com.kyoung.signal.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
@EnableConfigurationProperties(PipelineScheduler.WatchlistProperties.class)
public class PipelineScheduler {

    private final PipelineService pipelineService;
    private final OutcomeService outcomeService;
    private final WatchlistProperties watchlistProperties;

    public PipelineScheduler(PipelineService pipelineService,
                              OutcomeService outcomeService,
                              WatchlistProperties watchlistProperties) {
        this.pipelineService = pipelineService;
        this.outcomeService = outcomeService;
        this.watchlistProperties = watchlistProperties;
    }

    @Scheduled(cron = "${pipeline.schedule.cron:0 0 9 * * MON-FRI}")
    public void runScheduled() {
        System.out.println("[Scheduler] 파이프라인 스케줄 실행 시작");

        // 1. 과거 run outcome 채움 (1d / 1w)
        try {
            outcomeService.fillOutcomes();
        } catch (Exception e) {
            System.err.println("[Scheduler] outcome 채움 실패: " + e.getMessage());
        }

        // 2. 오늘 파이프라인 실행
        for (WatchlistItem item : watchlistProperties.getWatchlist()) {
            try {
                System.out.println("[Scheduler] 실행 중: " + item.getTicker());
                pipelineService.run(item.getTicker(), item.getRssFeedUrl(), item.getRssFeedSource());
                System.out.println("[Scheduler] 완료: " + item.getTicker());
            } catch (Exception e) {
                System.err.println("[Scheduler] 실패 (" + item.getTicker() + "): " + e.getMessage());
            }
        }

        System.out.println("[Scheduler] 스케줄 실행 완료");
    }

    @ConfigurationProperties(prefix = "pipeline")
    public static class WatchlistProperties {

        private List<WatchlistItem> watchlist = List.of();

        public List<WatchlistItem> getWatchlist() { return watchlist; }
        public void setWatchlist(List<WatchlistItem> watchlist) { this.watchlist = watchlist; }
    }

    public static class WatchlistItem {
        private String ticker;
        private String rssFeedUrl;
        private String rssFeedSource;

        public String getTicker()       { return ticker; }
        public String getRssFeedUrl()   { return rssFeedUrl; }
        public String getRssFeedSource(){ return rssFeedSource; }
        public void setTicker(String v)       { this.ticker = v; }
        public void setRssFeedUrl(String v)   { this.rssFeedUrl = v; }
        public void setRssFeedSource(String v){ this.rssFeedSource = v; }
    }
}
