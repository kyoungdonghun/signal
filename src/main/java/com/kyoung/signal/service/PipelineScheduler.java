package com.kyoung.signal.service;

import com.kyoung.signal.domain.SchedulerLogEntity;
import com.kyoung.signal.repository.SchedulerLogRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@EnableScheduling
@EnableConfigurationProperties(PipelineScheduler.WatchlistProperties.class)
public class PipelineScheduler {

    private final PipelineService pipelineService;
    private final OutcomeService outcomeService;
    private final WatchlistProperties watchlistProperties;
    private final SchedulerLogRepository schedulerLogRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PipelineScheduler(PipelineService pipelineService,
                              OutcomeService outcomeService,
                              WatchlistProperties watchlistProperties,
                              SchedulerLogRepository schedulerLogRepository) {
        this.pipelineService = pipelineService;
        this.outcomeService = outcomeService;
        this.watchlistProperties = watchlistProperties;
        this.schedulerLogRepository = schedulerLogRepository;
    }

    @Scheduled(cron = "${pipeline.schedule.cron:0 0 9 * * MON-FRI}")
    public void runScheduled() {
        if (!running.compareAndSet(false, true)) {
            System.out.println("[Scheduler] 이미 실행 중 — 중복 트리거 무시");
            return;
        }
        System.out.println("[Scheduler] 파이프라인 스케줄 실행 시작");

        List<String> allTickers = watchlistProperties.getWatchlist().stream()
                .map(WatchlistItem::getTicker).toList();
        String tickersAttempted = String.join(",", allTickers);

        SchedulerLogEntity log = SchedulerLogEntity.start(LocalDateTime.now(), tickersAttempted);
        schedulerLogRepository.save(log);

        // 1. 과거 run outcome 채움 (1d / 1w)
        try {
            outcomeService.fillOutcomes();
        } catch (Exception e) {
            System.err.println("[Scheduler] outcome 채움 실패: " + e.getMessage());
        }

        // 2. 오늘 파이프라인 실행
        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        try {
            List<WatchlistItem> watchlist = watchlistProperties.getWatchlist();
            for (int i = 0; i < watchlist.size(); i++) {
                WatchlistItem item = watchlist.get(i);
                try {
                    System.out.println("[Scheduler] 실행 중: " + item.getTicker());
                    pipelineService.run(item.getTicker(), item.getRssFeedUrl(), item.getRssFeedSource());
                    System.out.println("[Scheduler] 완료: " + item.getTicker());
                    succeeded.add(item.getTicker());
                } catch (Exception e) {
                    System.err.println("[Scheduler] 실패 (" + item.getTicker() + "): " + e.getMessage());
                    failed.add(item.getTicker());
                }

                // 마지막 종목 제외 — 종목 간 딜레이로 rate limit 완화
                if (i < watchlist.size() - 1) {
                    try { Thread.sleep(10_000); } catch (InterruptedException ignored) {}
                }
            }

            log.complete(LocalDateTime.now(), String.join(",", succeeded), String.join(",", failed));
            schedulerLogRepository.save(log);

            System.out.println("[Scheduler] 스케줄 실행 완료 — 성공: " + succeeded + " 실패: " + failed);
        } finally {
            running.set(false);
        }
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
