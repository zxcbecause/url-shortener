package io.github.zxcbecause.shortener.link.dto;

import io.github.zxcbecause.shortener.link.Link;

import java.time.Instant;

public record LinkResponse(
        String code,
        String shortUrl,
        String targetUrl,
        long clicks,
        Instant createdAt,
        Instant expiresAt,
        Instant lastAccessedAt
) {

    public static LinkResponse from(Link link, String baseUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return new LinkResponse(
                link.getCode(),
                base + "/" + link.getCode(),
                link.getTargetUrl(),
                link.getClicks(),
                link.getCreatedAt(),
                link.getExpiresAt(),
                link.getLastAccessedAt());
    }
}
