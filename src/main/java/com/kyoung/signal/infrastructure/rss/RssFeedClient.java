package com.kyoung.signal.infrastructure.rss;

import com.kyoung.signal.domain.NewsItem;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RssFeedClient {

    private static final int EXCERPT_LENGTH = 300;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public List<NewsItem> fetch(String feedUrl, String sourceName) {
        try {
            // Java HttpClient로 직접 가져와 User-Agent 설정 (봇 차단 우회)
            String referer = URI.create(feedUrl).getScheme() + "://" + URI.create(feedUrl).getHost();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(feedUrl))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "identity")
                    .header("Referer", referer)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new RuntimeException("RSS 피드 HTTP 오류: " + response.statusCode());
            }

            SyndFeed feed = new SyndFeedInput().build(
                    new XmlReader(new ByteArrayInputStream(response.body()))
            );

            return feed.getEntries().stream()
                    .map(entry -> toNewsItem(entry, sourceName))
                    .collect(Collectors.toList());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("RSS 피드 수집 실패 (source=" + sourceName + ", url=" + feedUrl + ")", e);
        }
    }

    private NewsItem toNewsItem(SyndEntry entry, String sourceName) {
        String title = entry.getTitle() != null ? entry.getTitle().trim() : "";
        String link  = entry.getLink()  != null ? entry.getLink().trim()  : "";

        String rawText = "";
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            rawText = entry.getDescription().getValue();
        } else if (!entry.getContents().isEmpty() && entry.getContents().get(0).getValue() != null) {
            rawText = entry.getContents().get(0).getValue();
        }

        String excerpt = extractExcerpt(rawText);

        Instant publishedAt = entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant()
                : Instant.now();

        return new NewsItem(title, link, excerpt, sourceName, publishedAt);
    }

    // HTML 태그 제거 후 앞 300자 추출 (NC 에이전트 스펙)
    private String extractExcerpt(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String stripped = raw.replaceAll("<[^>]+>", "").trim();
        return stripped.length() <= EXCERPT_LENGTH
                ? stripped
                : stripped.substring(0, EXCERPT_LENGTH);
    }
}
