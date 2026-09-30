package io.github.zxcbecause.shortener.link;

import io.github.zxcbecause.shortener.config.ShortenerProperties;
import io.github.zxcbecause.shortener.link.dto.LinkResponse;
import io.github.zxcbecause.shortener.link.dto.ShortenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-30T12:00:00Z");
    private static final ShortenerProperties PROPS = new ShortenerProperties("https://sho.rt/", 7);

    @Mock
    private LinkRepository repository;

    private LinkService service;

    @BeforeEach
    void setUp() {
        Iterator<String> codes = List.of("taken01", "free002").iterator();
        service = new LinkService(repository, PROPS, Clock.fixed(NOW, ZoneOffset.UTC), len -> codes.next());
    }

    @Test
    void generatedCodeSkipsCollisions() {
        when(repository.existsByCode("taken01")).thenReturn(true);
        when(repository.existsByCode("free002")).thenReturn(false);
        when(repository.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service.shorten(new ShortenRequest("https://github.com", null, null));

        assertThat(response.code()).isEqualTo("free002");
        assertThat(response.shortUrl()).isEqualTo("https://sho.rt/free002");
        assertThat(response.expiresAt()).isNull();
    }

    @Test
    void ttlSetsExpiryDate() {
        when(repository.existsByCode("my-cv")).thenReturn(false);
        when(repository.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service.shorten(new ShortenRequest("https://github.com", "my-cv", 7));

        assertThat(response.code()).isEqualTo("my-cv");
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-10-07T12:00:00Z"));
    }

    @Test
    void takenAliasIsRejected() {
        when(repository.existsByCode("my-cv")).thenReturn(true);

        assertThatThrownBy(() -> service.shorten(new ShortenRequest("https://github.com", "my-cv", null)))
                .isInstanceOf(LinkExceptions.AliasTaken.class);
    }

    @Test
    void invalidUrlIsRejectedBeforeTouchingDatabase() {
        assertThatThrownBy(() -> service.shorten(new ShortenRequest("javascript:alert(1)", null, null)))
                .isInstanceOf(InvalidUrlException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void expiredLinkCannotBeResolved() {
        Link link = new Link("old", "https://example.com", NOW.minusSeconds(3600), NOW.minusSeconds(1));
        when(repository.findByCode("old")).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> service.resolve("old")).isInstanceOf(LinkExceptions.Expired.class);
        verify(repository, never()).registerClick(anyLong(), any());
    }

    @Test
    void unknownCodeThrowsNotFound() {
        when(repository.findByCode("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve("nope")).isInstanceOf(LinkExceptions.NotFound.class);
    }
}
