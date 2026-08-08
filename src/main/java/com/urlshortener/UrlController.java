package com.urlshortener;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UrlController {

    private final URLMapping urlMapping;

    public UrlController(URLMapping urlMapping) {
        this.urlMapping = urlMapping;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody ShortenRequest req) {
        if (req.getOriginalUrl() == null || req.getOriginalUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "originalUrl is required"));
        }
        try {
            String shortCode = urlMapping.encode(req.getOriginalUrl(), req.getAlias());
            String shortUrl = "http://localhost:8080/" + shortCode;
            return ResponseEntity.ok(new ShortenResponse(shortCode, shortUrl, req.getOriginalUrl()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        String originalUrl = urlMapping.decode(code);
        if (originalUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(301)
                .header("Location", originalUrl)
                .build();
    }
}
