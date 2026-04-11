package com.kyoung.signal.infrastructure.yahoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoung.signal.domain.OhlcvData;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceClient {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=3mo";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OhlcvData fetch(String ticker) {
        String url = String.format(BASE_URL, ticker);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Yahoo Finance API 응답 오류: HTTP " + response.statusCode() + " (ticker=" + ticker + ")");
            }

            return parse(ticker, response.body());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Yahoo Finance API 호출 실패 (ticker=" + ticker + ")", e);
        }
    }

    private OhlcvData parse(String ticker, String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode result = root.path("chart").path("result").get(0);

        if (result == null || result.isMissingNode()) {
            throw new RuntimeException("Yahoo Finance 응답에 result 없음 (ticker=" + ticker + ")");
        }

        JsonNode timestamps = result.path("timestamp");
        JsonNode quote = result.path("indicators").path("quote").get(0);

        JsonNode opens   = quote.path("open");
        JsonNode highs   = quote.path("high");
        JsonNode lows    = quote.path("low");
        JsonNode closes  = quote.path("close");
        JsonNode volumes = quote.path("volume");

        int size = timestamps.size();
        List<OhlcvData.Bar> bars = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            if (opens.get(i).isNull() || highs.get(i).isNull() ||
                lows.get(i).isNull() || closes.get(i).isNull() || volumes.get(i).isNull()) {
                continue;
            }

            LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong())
                    .atZone(ZoneId.of("America/New_York"))
                    .toLocalDate();

            bars.add(new OhlcvData.Bar(
                    date,
                    opens.get(i).asDouble(),
                    highs.get(i).asDouble(),
                    lows.get(i).asDouble(),
                    closes.get(i).asDouble(),
                    volumes.get(i).asLong()
            ));
        }

        if (bars.isEmpty()) {
            throw new RuntimeException("Yahoo Finance 응답에 유효한 OHLCV 데이터 없음 (ticker=" + ticker + ")");
        }

        return new OhlcvData(ticker, bars);
    }
}
