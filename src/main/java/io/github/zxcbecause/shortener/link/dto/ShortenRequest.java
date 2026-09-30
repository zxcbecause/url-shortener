package io.github.zxcbecause.shortener.link.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * @param url      the long URL to shorten
 * @param alias    optional custom code, e.g. "my-cv"
 * @param ttlDays  optional lifetime in days; link never expires when null
 */
public record ShortenRequest(
        @NotBlank(message = "url is required")
        String url,

        @Pattern(regexp = "[A-Za-z0-9_-]{3,32}",
                message = "alias must be 3-32 characters: letters, digits, '-' or '_'")
        String alias,

        @Min(value = 1, message = "ttlDays must be at least 1")
        @Max(value = 3650, message = "ttlDays must be at most 3650")
        Integer ttlDays
) {
}
