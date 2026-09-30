package io.github.zxcbecause.shortener.link;

import io.github.zxcbecause.shortener.config.ShortenerProperties;
import io.github.zxcbecause.shortener.link.dto.LinkResponse;
import io.github.zxcbecause.shortener.link.dto.ShortenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.IntFunction;

@Service
public class LinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final LinkRepository repository;
    private final ShortenerProperties properties;
    private final Clock clock;
    private final IntFunction<String> codeGenerator;

    @Autowired
    public LinkService(LinkRepository repository, ShortenerProperties properties, Clock clock) {
        this(repository, properties, clock, Base62::random);
    }

    LinkService(LinkRepository repository, ShortenerProperties properties, Clock clock,
                IntFunction<String> codeGenerator) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public LinkResponse shorten(ShortenRequest request) {
        String url = UrlValidator.normalize(request.url());
        String code = request.alias() != null ? claimAlias(request.alias()) : generateUniqueCode();

        Instant now = clock.instant();
        Instant expiresAt = request.ttlDays() == null ? null : now.plus(Duration.ofDays(request.ttlDays()));

        Link saved = repository.save(new Link(code, url, now, expiresAt));
        return LinkResponse.from(saved, properties.baseUrl());
    }

    /** Returns the target URL and counts the click. */
    @Transactional
    public String resolve(String code) {
        Link link = findActive(code);
        repository.registerClick(link.getId(), clock.instant());
        return link.getTargetUrl();
    }

    @Transactional(readOnly = true)
    public LinkResponse stats(String code) {
        Link link = repository.findByCode(code).orElseThrow(() -> new LinkExceptions.NotFound(code));
        return LinkResponse.from(link, properties.baseUrl());
    }

    @Transactional
    public void delete(String code) {
        Link link = repository.findByCode(code).orElseThrow(() -> new LinkExceptions.NotFound(code));
        repository.delete(link);
    }

    private Link findActive(String code) {
        Link link = repository.findByCode(code).orElseThrow(() -> new LinkExceptions.NotFound(code));
        if (link.isExpired(clock.instant())) {
            throw new LinkExceptions.Expired(code);
        }
        return link;
    }

    private String claimAlias(String alias) {
        if (repository.existsByCode(alias)) {
            throw new LinkExceptions.AliasTaken(alias);
        }
        return alias;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.apply(properties.codeLength());
            if (!repository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique code, try again");
    }
}
