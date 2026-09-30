package io.github.zxcbecause.shortener.link;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com",
            "http://example.com/path?q=1#top",
            "  https://example.com/trimmed  "
    })
    void acceptsHttpUrls(String url) {
        assertThat(UrlValidator.normalize(url)).isEqualTo(url.trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "github.com",
            "ftp://example.com/file",
            "javascript:alert(1)",
            "https://",
            "http://exa mple.com"
    })
    void rejectsBadUrls(String url) {
        assertThatThrownBy(() -> UrlValidator.normalize(url)).isInstanceOf(InvalidUrlException.class);
    }
}
