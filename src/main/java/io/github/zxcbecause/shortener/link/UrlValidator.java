package io.github.zxcbecause.shortener.link;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

/**
 * Accepts only absolute http(s) URLs with a host. Rejects things like
 * "javascript:alert(1)" or "ftp://..." that should never be redirect targets.
 */
public final class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_LENGTH = 2048;

    private UrlValidator() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidUrlException("URL must not be empty");
        }
        String url = raw.trim();
        if (url.length() > MAX_LENGTH) {
            throw new InvalidUrlException("URL is longer than " + MAX_LENGTH + " characters");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
                throw new InvalidUrlException("Only http and https URLs are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException("URL must contain a host");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Malformed URL: " + e.getReason());
        }
    }
}
