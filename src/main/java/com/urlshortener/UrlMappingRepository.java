package com.urlshortener;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMappingEntity, Long> {
    Optional<UrlMappingEntity> findByShortCode(String shortCode);
    Optional<UrlMappingEntity> findByOriginalUrl(String originalUrl);
}
