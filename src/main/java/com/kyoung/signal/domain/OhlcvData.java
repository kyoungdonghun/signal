package com.kyoung.signal.domain;

import java.time.LocalDate;
import java.util.List;

public class OhlcvData {

    private final String ticker;
    private final List<Bar> bars;

    public OhlcvData(String ticker, List<Bar> bars) {
        this.ticker = ticker;
        this.bars = bars;
    }

    public String getTicker() { return ticker; }
    public List<Bar> getBars() { return bars; }

    public static class Bar {
        private final LocalDate date;
        private final double open;
        private final double high;
        private final double low;
        private final double close;
        private final long volume;

        public Bar(LocalDate date, double open, double high, double low, double close, long volume) {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }

        public LocalDate getDate()  { return date; }
        public double getOpen()     { return open; }
        public double getHigh()     { return high; }
        public double getLow()      { return low; }
        public double getClose()    { return close; }
        public long getVolume()     { return volume; }
    }
}
