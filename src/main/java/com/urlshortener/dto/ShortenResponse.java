package com.urlshortener.dto;

public class ShortenResponse {
    private final String shortCode;
    private final String shortUrl;
    private final String originalUrl;

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
    }

    public String getShortCode()   { return shortCode; }
    public String getShortUrl()    { return shortUrl; }
    public String getOriginalUrl() { return originalUrl; }
}
