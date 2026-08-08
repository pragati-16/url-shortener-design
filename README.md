# URL Shortener

A URL shortening service built with Spring Boot and H2. Turns long URLs into short codes and redirects visitors to the original link.

---

## Prerequisites

- Java 17+
- Maven 3.6+

Verify:
```bash
java -version
mvn -version
```

> **Corporate / office network:** Maven downloads dependencies from the internet on first run. If you are behind a corporate firewall, use a personal hotspot for the first `mvn` command. Dependencies are cached permanently in `~/.m2` and the network is not needed again.

---

## Install

```bash
git clone <repo-url>
cd url-shortener-design
```

No additional installation steps. Maven downloads all dependencies automatically on first run.

---

## Run

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

To stop it, press `Ctrl + C`.

---

## Test the API

### Shorten a URL

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com/very/long/path"}'
```

Response:
```json
{
  "shortCode": "1",
  "shortUrl": "http://localhost:8080/1",
  "originalUrl": "https://example.com/very/long/path"
}
```

### Shorten with a custom alias

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com", "alias": "mylink"}'
```

Response:
```json
{
  "shortCode": "mylink",
  "shortUrl": "http://localhost:8080/mylink",
  "originalUrl": "https://example.com"
}
```

### Follow a short link

```bash
curl -v http://localhost:8080/1
# → HTTP 301, Location: https://example.com/very/long/path

curl -L http://localhost:8080/1
# → follows the redirect automatically
```

### Unknown code returns 404

```bash
curl -v http://localhost:8080/doesnotexist
# → HTTP 404
```

### Error responses

| Scenario | Status | Body |
|---|---|---|
| `originalUrl` missing or blank | 400 | `{"error": "originalUrl is required"}` |
| URL uses non-http/https scheme | 400 | `{"error": "URL must use http or https scheme"}` |
| Alias already taken by a different URL | 409 | `{"error": "Alias '...' is already taken by a different URL"}` |
| Unknown short code | 404 | `{"error": "Short code not found"}` |

---

## Run the Automated Tests

```bash
mvn test
```

Tests use an in-memory H2 database and do not touch the file-based database used at runtime.

### What is tested

| Test class | What it covers |
|---|---|
| `Base62Test` | Encode/decode roundtrip, URL-safe characters, distinct outputs |
| `URLMappingTest` | Core logic: new URL, duplicate URL, custom alias, alias conflict, URL validation, decode |
| `UrlControllerTest` | HTTP layer: correct status codes, response bodies, Location header on redirect |

---

## Browse the Database

While the server is running, open **http://localhost:8080/h2-console** and connect with:

- JDBC URL: `jdbc:h2:file:./data/urlshortener`
- User Name: `sa`
- Password: *(leave blank)*

Then run:
```sql
SELECT * FROM URL_MAPPINGS;
```

---

## Design Decisions

### Short-code generator — no collisions

Auto-generated codes use H2's auto-increment `id` column encoded in Base62 (`0-9a-zA-Z`). The database guarantees each `id` is unique and monotonically increasing, so `Base62(id)` can never collide. Custom aliases occupy their own rows and never affect the numeric sequence.

### Duplicate URLs — idempotent

Shortening the same URL twice returns the **same code**. The service checks for an existing `originalUrl` before inserting. This gives every URL one canonical short link — useful for sharing and analytics.

### Custom aliases

If an `alias` is provided it is stored as the short code as-is. If the alias already maps to the **same** URL the call is idempotent and returns the alias. If it maps to a **different** URL the service returns **409 Conflict**, preventing silent overwrites.

### URL validation

Only `http://` and `https://` URLs with a non-empty host are accepted. Everything else returns **400 Bad Request**. This blocks schemes like `javascript:`, `ftp://`, and bare domain names.

### Persistence

Mappings are stored in `./data/urlshortener.mv.db` (H2 file-mode). Data survives server restarts. The auto-increment `id` is managed by H2, so the short-code sequence also survives restarts without any manual counter recovery.
