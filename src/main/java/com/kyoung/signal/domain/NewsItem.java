package com.kyoung.signal.domain;

import java.time.Instant;

public class NewsItem {

    private final String title;
    private final String link;
    private final String excerpt;
    private final String source;
    private final Instant publishedAt;

    public NewsItem(String title, String link, String excerpt, String source, Instant publishedAt) {
        this.title = title;
        this.link = link;
        this.excerpt = excerpt;
        this.source = source;
        this.publishedAt = publishedAt;
    }

    public String getTitle()         { return title; }
    public String getLink()          { return link; }
    public String getExcerpt()       { return excerpt; }
    public String getSource()        { return source; }
    public Instant getPublishedAt()  { return publishedAt; }
}
