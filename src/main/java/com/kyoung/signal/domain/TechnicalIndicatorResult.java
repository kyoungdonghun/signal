package com.kyoung.signal.domain;

import java.time.Instant;

public class TechnicalIndicatorResult {

    private final String runId;
    private final String ticker;
    private final String calculatedAt;
    private final Price price;
    private final Rsi rsi;
    private final Volume volume;
    private final String dataQuality;
    private final String errorDetail;

    private TechnicalIndicatorResult(Builder builder) {
        this.runId = builder.runId;
        this.ticker = builder.ticker;
        this.calculatedAt = Instant.now().toString();
        this.price = builder.price;
        this.rsi = builder.rsi;
        this.volume = builder.volume;
        this.dataQuality = builder.dataQuality;
        this.errorDetail = builder.errorDetail;
    }

    public static TechnicalIndicatorResult error(String runId, String ticker, String errorDetail) {
        return new Builder(runId, ticker)
                .dataQuality("error")
                .errorDetail(errorDetail)
                .build();
    }

    public String getRunId()        { return runId; }
    public String getTicker()       { return ticker; }
    public String getCalculatedAt() { return calculatedAt; }
    public Price getPrice()         { return price; }
    public Rsi getRsi()             { return rsi; }
    public Volume getVolume()       { return volume; }
    public String getDataQuality()  { return dataQuality; }
    public String getErrorDetail()  { return errorDetail; }

    public static class Price {
        private final double current;
        private final double ma20;
        private final double ma60;

        public Price(double current, double ma20, double ma60) {
            this.current = current;
            this.ma20 = ma20;
            this.ma60 = ma60;
        }

        public double getCurrent() { return current; }
        public double getMa20()    { return ma20; }
        public double getMa60()    { return ma60; }
    }

    public static class Rsi {
        private final double value;
        private final int period;

        public Rsi(double value, int period) {
            this.value = value;
            this.period = period;
        }

        public double getValue()  { return value; }
        public int getPeriod()    { return period; }
    }

    public static class Volume {
        private final long current;
        private final double avg20;
        private final double ratio;

        public Volume(long current, double avg20, double ratio) {
            this.current = current;
            this.avg20 = avg20;
            this.ratio = ratio;
        }

        public long getCurrent()  { return current; }
        public double getAvg20()  { return avg20; }
        public double getRatio()  { return ratio; }
    }

    public static class Builder {
        private final String runId;
        private final String ticker;
        private Price price;
        private Rsi rsi;
        private Volume volume;
        private String dataQuality = "ok";
        private String errorDetail;

        public Builder(String runId, String ticker) {
            this.runId = runId;
            this.ticker = ticker;
        }

        public Builder price(Price price)           { this.price = price; return this; }
        public Builder rsi(Rsi rsi)                 { this.rsi = rsi; return this; }
        public Builder volume(Volume volume)         { this.volume = volume; return this; }
        public Builder dataQuality(String quality)  { this.dataQuality = quality; return this; }
        public Builder errorDetail(String detail)   { this.errorDetail = detail; return this; }
        public TechnicalIndicatorResult build()     { return new TechnicalIndicatorResult(this); }
    }
}
