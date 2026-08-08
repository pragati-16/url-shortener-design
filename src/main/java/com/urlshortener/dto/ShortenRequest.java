package com.urlshortener.dto;

public class ShortenRequest {
    private String originalUrl;
    private String alias;

    public ShortenRequest() {}

    public ShortenRequest(String originalUrl,String alias) {
        this.originalUrl = originalUrl;
        this.alias = alias;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
}
