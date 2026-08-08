package com.urlshortener;

import jakarta.persistence.*;

@Entity
@Table(name = "url_mappings")
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String shortCode;

    @Column(nullable = false, unique = true)
    private String originalUrl;

    protected UrlMappingEntity() {} // required by JPA

    // Used by tests to create entities with a known ID
    UrlMappingEntity(Long id, String shortCode, String originalUrl) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }

    public UrlMappingEntity(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }

    public Long getId()            { return id; }
    public String getShortCode()   { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
}
