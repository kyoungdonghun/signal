---
name: technical-indicator-calc
description: MA, RSI, 거래량 비율을 공식으로 직접 계산한다. TC 에이전트의 핵심 계산 로직.
---

# Technical Indicator Calc

## Purpose

yahoo-finance-fetch로 수집한 OHLCV 데이터를 받아 MA(이동평균), RSI(상대강도지수), 거래량 비율을 수학 공식으로 직접 계산한다.
TC(Tech Calculator) 에이전트의 핵심 로직이다.
계산만 한다. 해석은 TR의 역할이다.

## When to Use

- TC 에이전트가 기술적 지표를 계산할 때
- 지표 정확성을 검증할 때
- 새로운 지표 추가 시 기존 구현 참조용

## Core Principle

- **외부 라이브러리 사용 금지**: TA-Lib, ta4j 등 사용하지 않는다
- **결정론적 계산**: 동일 입력 → 동일 출력 보장
- **계산만**: 과매수/과매도, 골든크로스 등 해석은 TR의 역할

## Calculations

### 1. Moving Average (이동평균)

```java
/**
 * 단순이동평균 계산
 * @param closePrices 종가 배열 (날짜 오름차순)
 * @param period 기간 (20 또는 60)
 * @return 이동평균값
 */
public static double calculateMA(double[] closePrices, int period) {
    if (closePrices.length < period) {
        throw new IllegalArgumentException(
            "데이터 부족: " + closePrices.length + "일 < " + period + "일");
    }
    double sum = 0;
    int start = closePrices.length - period;
    for (int i = start; i < closePrices.length; i++) {
        sum += closePrices[i];
    }
    return sum / period;
}
```

- MA(20): 최근 20일 종가 평균
- MA(60): 최근 60일 종가 평균

### 2. RSI (Relative Strength Index)

```java
/**
 * RSI 계산 (14일 기준)
 * @param closePrices 종가 배열 (날짜 오름차순, 최소 15개)
 * @return RSI 값 (0~100)
 */
public static double calculateRSI(double[] closePrices) {
    int period = 14;
    if (closePrices.length < period + 1) {
        throw new IllegalArgumentException(
            "RSI 계산에 최소 " + (period + 1) + "일 데이터 필요");
    }

    double avgGain = 0;
    double avgLoss = 0;

    // 최초 14일 평균
    for (int i = 1; i <= period; i++) {
        double change = closePrices[i] - closePrices[i - 1];
        if (change > 0) avgGain += change;
        else avgLoss += Math.abs(change);
    }
    avgGain /= period;
    avgLoss /= period;

    // 이후 smoothing
    for (int i = period + 1; i < closePrices.length; i++) {
        double change = closePrices[i] - closePrices[i - 1];
        if (change > 0) {
            avgGain = (avgGain * (period - 1) + change) / period;
            avgLoss = (avgLoss * (period - 1)) / period;
        } else {
            avgGain = (avgGain * (period - 1)) / period;
            avgLoss = (avgLoss * (period - 1) + Math.abs(change)) / period;
        }
    }

    if (avgLoss == 0) return 100.0;
    double rs = avgGain / avgLoss;
    return 100.0 - (100.0 / (1.0 + rs));
}
```

### 3. Volume Ratio (거래량 비율)

```java
/**
 * 거래량 비율 계산
 * @param volumes 거래량 배열 (날짜 오름차순, 최소 20개)
 * @return 현재 거래량 / 20일 평균 거래량
 */
public static double calculateVolumeRatio(long[] volumes) {
    int period = 20;
    if (volumes.length < period) {
        throw new IllegalArgumentException(
            "거래량 비율 계산에 최소 " + period + "일 데이터 필요");
    }

    long currentVolume = volumes[volumes.length - 1];
    double sum = 0;
    int start = volumes.length - period;
    for (int i = start; i < volumes.length; i++) {
        sum += volumes[i];
    }
    double avg20 = sum / period;

    if (avg20 == 0) return 0;
    return currentVolume / avg20;
}
```

## Output Format

TR 에이전트에 전달하는 JSON 형식:

```json
{
  "ticker": "AAPL",
  "calculated_at": "2026-03-29T09:00:00Z",
  "price": {
    "current": 151.30,
    "ma20": 148.75,
    "ma60": 145.20
  },
  "rsi": {
    "value": 62.4,
    "period": 14
  },
  "volume": {
    "current": 45000000,
    "avg20": 38000000,
    "ratio": 1.18
  },
  "data_quality": "ok",
  "error_detail": null
}
```

## Validation Criteria

| 항목 | 기준 |
|------|------|
| MA 오차 | 알려진 차트 데이터 대비 0.01% 이내 |
| RSI 오차 | 0.1 이내 |
| 거래량 비율 | 소수점 2자리 정확 |
| 입력 검증 | 데이터 부족 시 error 반환 (계산 시도 금지) |

## Absolute Prohibitions

- TA-Lib, ta4j 등 외부 라이브러리 사용 금지
- 과매수/과매도 판단 금지 (TR의 역할)
- 골든크로스/데드크로스 판단 금지 (TR의 역할)
- 거래량 이상 판단 금지 (TR의 역할)
- 계산 결과에 해석을 추가하는 어떤 행위도 금지
