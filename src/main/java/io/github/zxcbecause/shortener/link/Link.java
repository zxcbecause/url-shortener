package io.github.zxcbecause.shortener.link;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 2048)
    private String targetUrl;

    @Column(nullable = false)
    private long clicks;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant expiresAt;

    private Instant lastAccessedAt;

    protected Link() {
        // for JPA
    }

    public Link(String code, String targetUrl, Instant createdAt, Instant expiresAt) {
        this.code = code;
        this.targetUrl = targetUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public long getClicks() {
        return clicks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }
}
