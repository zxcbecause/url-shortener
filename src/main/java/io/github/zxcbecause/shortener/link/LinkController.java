package io.github.zxcbecause.shortener.link;

import io.github.zxcbecause.shortener.link.dto.LinkResponse;
import io.github.zxcbecause.shortener.link.dto.ShortenRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class LinkController {

    private final LinkService service;

    public LinkController(LinkService service) {
        this.service = service;
    }

    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        LinkResponse link = service.shorten(request);
        return ResponseEntity.created(URI.create("/api/links/" + link.code())).body(link);
    }

    @GetMapping("/api/links/{code}")
    public LinkResponse stats(@PathVariable String code) {
        return service.stats(code);
    }

    @DeleteMapping("/api/links/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.delete(code);
    }

    /** The short link itself: 302 redirect to the original URL. */
    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, target)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }
}
