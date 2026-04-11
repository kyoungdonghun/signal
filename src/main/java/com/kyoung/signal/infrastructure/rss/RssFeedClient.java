package com.kyoung.signal.infrastructure.rss;

import com.kyoung.signal.domain.NewsItem;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RssFeedClient {

    private static final int EXCERPT_LENGTH = 300;

    public List<NewsItem> fetch(String feedUrl, String sourceName) {
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(feedUrl)));

            return feed.getEntries().stream()
                    .map(entry -> toNewsItem(entry, sourceName))
                    .collect(Collectors.toList());

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
