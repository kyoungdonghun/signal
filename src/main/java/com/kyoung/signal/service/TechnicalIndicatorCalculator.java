package com.kyoung.signal.service;

import com.kyoung.signal.domain.OhlcvData;
import com.kyoung.signal.domain.TechnicalIndicatorResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicalIndicatorCalculator {

    private static final int MA_SHORT  = 20;
    private static final int MA_LONG   = 60;
    private static final int RSI_PERIOD = 14;
    private static final int MIN_BARS  = MA_LONG + 1;

    public TechnicalIndicatorResult calculate(String runId, OhlcvData data) {
        List<OhlcvData.Bar> bars = data.getBars();

        if (bars == null || bars.size() < MIN_BARS) {
            return TechnicalIndicatorResult.error(
                runId, data.getTicker(),
                "데이터 부족: MA60 계산에 최소 " + MIN_BARS + "개 필요, 실제 " + (bars == null ? 0 : bars.size()) + "개"
            );
        }

        try {
            OhlcvData.Bar latest = bars.get(bars.size() - 1);

            double ma20  = calcMa(bars, MA_SHORT);
            double ma60  = calcMa(bars, MA_LONG);
            double rsi   = calcRsi(bars, RSI_PERIOD);
            double avg20 = calcVolumeAvg(bars, MA_SHORT);
            double ratio = avg20 > 0 ? latest.getVolume() / avg20 : 0.0;

            return new TechnicalIndicatorResult.Builder(runId, data.getTicker())
                    .price(new TechnicalIndicatorResult.Price(latest.getClose(), ma20, ma60))
                    .rsi(new TechnicalIndicatorResult.Rsi(rsi, RSI_PERIOD))
                    .volume(new TechnicalIndicatorResult.Volume(latest.getVolume(), avg20, ratio))
                    .dataQuality("ok")
                    .build();

        } catch (Exception e) {
            return TechnicalIndicatorResult.error(runId, data.getTicker(), "계산 중 오류: " + e.getMessage());
        }
    }

    // MA(n) = 최근 n개 종가의 단순 평균
    private double calcMa(List<OhlcvData.Bar> bars, int period) {
        int from = bars.size() - period;
        double sum = 0.0;
        for (int i = from; i < bars.size(); i++) {
            sum += bars.get(i).getClose();
        }
        return round(sum / period);
    }

    // RSI(14) — Wilder's Smoothing
    // 1단계: 첫 14개 변화량의 단순 평균으로 초기 avg_gain, avg_loss 산출
    // 2단계: 이후 Wilder's smoothing: avg = (prev_avg * 13 + current) / 14
    private double calcRsi(List<OhlcvData.Bar> bars, int period) {
        int start = bars.size() - period - 1;

        double avgGain = 0.0;
        double avgLoss = 0.0;

        for (int i = start; i < start + period; i++) {
            double change = bars.get(i + 1).getClose() - bars.get(i).getClose();
            if (change > 0) avgGain += change;
            else            avgLoss += Math.abs(change);
        }
        avgGain /= period;
        avgLoss /= period;

        for (int i = start + period; i < bars.size() - 1; i++) {
            double change = bars.get(i + 1).getClose() - bars.get(i).getClose();
            double gain = change > 0 ? change : 0.0;
            double loss = change < 0 ? Math.abs(change) : 0.0;
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
        }

        if (avgLoss == 0.0) return 100.0;

        double rs = avgGain / avgLoss;
        return round(100.0 - (100.0 / (1.0 + rs)));
    }

    // 최근 n개 거래량의 단순 평균
    private double calcVolumeAvg(List<OhlcvData.Bar> bars, int period) {
        int from = bars.size() - period;
        double sum = 0.0;
        for (int i = from; i < bars.size(); i++) {
            sum += bars.get(i).getVolume();
        }
        return sum / period;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
