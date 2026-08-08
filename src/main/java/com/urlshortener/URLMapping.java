package com.urlshortener;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
public class URLMapping {

    private final UrlMappingRepository repository;

    public URLMapping(UrlMappingRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns a short code for the given URL.
     *
     * Duplicate URLs: returns the existing code (idempotent).
     * Custom alias:   stored as-is; 409 if the alias is taken by a different URL.
     * Auto-generated: saves the row to get a DB-generated ID, then sets
     *                 shortCode = Base62(id) — guaranteed unique and collision-free.
     */
    @Transactional
    public String encode(String originalUrl, String alias) {
        validateUrl(originalUrl);

        if (alias != null && !alias.isBlank()) {
            Optional<UrlMappingEntity> existing = repository.findByShortCode(alias);
            if (existing.isPresent()) {
                if (existing.get().getOriginalUrl().equals(originalUrl)) return alias; // idempotent
                throw new IllegalStateException("Alias '" + alias + "' is already taken by a different URL");
            }
            repository.save(new UrlMappingEntity(alias, originalUrl));
            return alias;
        }

        // Same URL → same code (idempotent)
        Optional<UrlMappingEntity> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) return existing.get().getShortCode();

        // Save first to get the DB-generated ID, then derive the short code from it
        UrlMappingEntity entity = repository.save(new UrlMappingEntity(null, originalUrl));
        String shortCode = Base62.encode(entity.getId());
        entity.setShortCode(shortCode);
        repository.save(entity);
        return shortCode;
    }

    /** Returns the original URL for a short code, or null if the code is unknown. */
    public String decode(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMappingEntity::getOriginalUrl)
                .orElse(null);
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new IllegalArgumentException("URL must use http or https scheme");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL must have a valid host");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + e.getReason());
        }
    }
}
