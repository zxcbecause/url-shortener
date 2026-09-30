# URL Shortener

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen) ![Tests](https://img.shields.io/badge/tests-JUnit%205-green)

A small link-shortening service, a bit like bit.ly. You send a long URL and get back a short code, and opening `/{code}` redirects you to the original page. It counts clicks, supports custom aliases and links that expire.

## Features

- Random 7-character Base62 codes (`0-9a-zA-Z`, about 3.5 trillion combinations) with a retry on collision
- Custom aliases (`/my-cv`) with a uniqueness check
- Optional expiry (`ttlDays`) – expired links return `410 Gone`
- Click counter and last access time. The counter is increased atomically in SQL, so clicks at the same moment are not lost
- Only `http`/`https` targets are allowed (blocks `javascript:` and other unsafe schemes)
- Error responses in the standard format (RFC 7807)
- Minimal web page at `/` for trying the service in a browser

## API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/links` | Create a short link |
| `GET` | `/{code}` | Redirect (302) to the original URL |
| `GET` | `/api/links/{code}` | Link info and click statistics |
| `DELETE` | `/api/links/{code}` | Delete a link |

### Example

```bash
curl -X POST localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com/zxcbecause", "alias": "my-gh", "ttlDays": 30}'
```

```json
{
  "code": "my-gh",
  "shortUrl": "http://localhost:8080/my-gh",
  "targetUrl": "https://github.com/zxcbecause",
  "clicks": 0,
  "createdAt": "2026-09-30T12:00:00Z",
  "expiresAt": "2026-10-30T12:00:00Z",
  "lastAccessedAt": null
}
```

```bash
curl -i localhost:8080/my-gh
# HTTP/1.1 302
# Location: https://github.com/zxcbecause
```

## Running

Requirements: JDK 21+, Maven 3.9+.

```bash
mvn spring-boot:run
# open http://localhost:8080
```

Configuration (environment variables):

| Variable | Default | Meaning |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | Public address used in `shortUrl` |
| `DB_URL` | in-memory H2 | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/links` |
| `DB_USERNAME` / `DB_PASSWORD` | `sa` / empty | Database credentials |

## Tests

```bash
mvn test
```

- `Base62Test`, `UrlValidatorTest` – pure unit tests, including parameterized cases
- `LinkServiceTest` – business rules with Mockito: collisions, aliases, expiry
- `LinkApiIntegrationTest` – end-to-end HTTP flow through MockMvc

## Design notes

- **Why random codes instead of encoding the DB id?** Sequential ids make it easy to guess and list all links. Random codes avoid that, and with 62^7 combinations collisions are rare. The retry loop handles them anyway.
- **Why 302 and not 301?** Browsers cache 301 redirects forever, so later clicks would never reach the server and would not be counted.

## Possible improvements

- Redis cache for hot links
- Rate limiting for link creation
- QR code generation
