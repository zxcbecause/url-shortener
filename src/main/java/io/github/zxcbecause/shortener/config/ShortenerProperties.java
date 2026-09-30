package io.github.zxcbecause.shortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param baseUrl    public address used to build short links, e.g. https://sho.rt
 * @param codeLength length of generated codes
 */
@ConfigurationProperties(prefix = "shortener")
public record ShortenerProperties(
        @DefaultValue("http://localhost:8080") String baseUrl,
        @DefaultValue("7") int codeLength
) {
}
