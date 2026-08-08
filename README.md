# URL Shortener

A simple Java service that shortens URLs and redirects visitors to the original link.

## Running

Compile and run `Main.java`. The server starts on **port 8080**. No external dependencies — uses only the Java standard library (`com.sun.net.httpserver`).

## API

### POST /shorten
Accepts a JSON body and returns a short code.

```bash
# Auto-generated code
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com/very/long/path"}'

# Custom alias
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com", "alias": "myalias"}'
```

Response:
```json
{"shortCode": "1", "shortUrl": "http://localhost:8080/1"}
```

### GET /{code}
Redirects (301) to the original URL. Returns 404 for unknown codes.

```bash
curl -L http://localhost:8080/1
```

## Design Decisions

### Short-code generator (no collisions)
Auto-generated codes use an incrementing `AtomicLong` counter encoded in Base62 (chars `0-9a-zA-Z`). Because the counter only moves forward and each value maps to exactly one Base62 string, codes are unique by construction. On restart, the counter is restored from the persisted store by decoding the highest numeric code found, so previously-issued codes are never re-used.

A `do/while` loop additionally skips any counter value whose Base62 encoding happens to match an existing custom alias, so auto-generated and custom codes can never collide with each other.

### Duplicate URLs — idempotent
Shortening the same URL twice returns the **same code**. This is intentional: the service maintains a `longToShort` reverse map and returns the existing entry without creating a new row. A caller can safely shorten the same URL multiple times and always gets a stable, shareable link.

### Custom aliases
If an `alias` field is provided, that string is used as the short code as-is. If the alias is already taken by the **same** URL, the call is idempotent and returns the alias. If the alias is taken by a **different** URL, the service returns HTTP 409 Conflict. This prevents silent overwrites of existing links.

### URL validation
Only `http://` and `https://` URLs with a non-empty host are accepted. Everything else returns HTTP 400.

### Persistence
Mappings are appended to `url_mappings.tsv` (tab-separated: `shortCode\toriginalUrl`) in the working directory. The file is loaded on startup so data survives restarts.
