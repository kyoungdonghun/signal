package com.kyoung.signal.service;

import com.kyoung.signal.domain.OutcomeRecordEntity;
import com.kyoung.signal.domain.PipelineRunEntity;
import com.kyoung.signal.infrastructure.yahoo.YahooFinanceClient;
import com.kyoung.signal.repository.OutcomeRecordRepository;
import com.kyoung.signal.repository.PipelineRunRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class OutcomeService {

    private final PipelineRunRepository pipelineRunRepository;
    private final OutcomeRecordRepository outcomeRecordRepository;
    private final YahooFinanceClient yahooFinanceClient;

    public OutcomeService(PipelineRunRepository pipelineRunRepository,
                          OutcomeRecordRepository outcomeRecordRepository,
                          YahooFinanceClient yahooFinanceClient) {
        this.pipelineRunRepository = pipelineRunRepository;
        this.outcomeRecordRepository = outcomeRecordRepository;
        this.yahooFinanceClient = yahooFinanceClient;
    }

    /**
     * 스케줄러 실행 시점에 호출.
     * - 1일 전 run → price_1d_after 기록
     * - 7일 전 run → price_1w_after 기록
     */
    public void fillOutcomes() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        fill1dOutcomes(now);
        fill1wOutcomes(now);
    }

    // 어제(24~48시간 전) 실행된 run에 오늘 가격 기록
    private void fill1dOutcomes(LocalDateTime now) {
        LocalDateTime from = now.minusHours(48);
        LocalDateTime to   = now.minusHours(20);

        List<PipelineRunEntity> targets = pipelineRunRepository.findRunsWithoutOutcome(from, to);

        for (PipelineRunEntity run : targets) {
            try {
                double currentPrice = yahooFinanceClient.fetchCurrentPrice(run.getTicker());
                double basePrice    = run.getPrice() != null ? run.getPrice() : currentPrice;
                double changePct    = ((currentPrice - basePrice) / basePrice) * 100.0;

                OutcomeRecordEntity outcome = OutcomeRecordEntity.of(
                        run.getRunId(),
                        run.getTicker(),
                        currentPrice,
                        null,        // price_1w_after는 7일 후에 채움
                        changePct
                );
                outcomeRecordRepository.save(outcome);

                System.out.printf("[OutcomeService] 1d outcome 저장: %s / %s → %.2f (%.2f%%)%n",
                        run.getRunId(), run.getTicker(), currentPrice, changePct);

            } catch (Exception e) {
                System.err.println("[OutcomeService] 1d outcome 실패 (" + run.getTicker() + "): " + e.getMessage());
            }
        }
    }

    // 7일 전(168~192시간 전) 실행된 run에 오늘 가격 기록 (price_1w_after 업데이트)
    private void fill1wOutcomes(LocalDateTime now) {
        LocalDateTime from = now.minusHours(192);
        LocalDateTime to   = now.minusHours(144);

        // 이미 outcome 레코드가 있는 run을 찾아 price_1w_after만 업데이트
        List<PipelineRunEntity> targets = pipelineRunRepository
                .findRunsNeedingWeeklyOutcome(from, to);

        for (PipelineRunEntity run : targets) {
            outcomeRecordRepository.findByRunId(run.getRunId()).ifPresent(outcome -> {
                if (outcome.getPrice1wAfter() != null) return; // 이미 채워진 경우 스킵

                try {
                    double currentPrice = yahooFinanceClient.fetchCurrentPrice(run.getTicker());
                    outcome.setPrice1wAfter(currentPrice);
                    outcomeRecordRepository.save(outcome);

                    System.out.printf("[OutcomeService] 1w outcome 업데이트: %s / %s → %.2f%n",
                            run.getRunId(), run.getTicker(), currentPrice);

                } catch (Exception e) {
                    System.err.println("[OutcomeService] 1w outcome 실패 (" + run.getTicker() + "): " + e.getMessage());
                }
            });
        }
    }
}
