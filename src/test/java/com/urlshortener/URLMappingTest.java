package com.urlshortener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class URLMappingTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private URLMapping urlMapping;

    private static final String VALID_URL = "https://example.com/some/long/path";

    // --- encode: auto-generated codes ---

    @Test
    void encode_newUrl_returnsBase62OfDatabaseId() {
        UrlMappingEntity savedEntity = new UrlMappingEntity(10L, null, VALID_URL);
        when(repository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(savedEntity);

        String code = urlMapping.encode(VALID_URL, null);

        assertEquals(Base62.encode(10L), code);
    }

    @Test
    void encode_sameUrlTwice_returnsSameCode_noExtraRow() {
        UrlMappingEntity existing = new UrlMappingEntity(1L, "1", VALID_URL);
        when(repository.findByOriginalUrl(VALID_URL)).thenReturn(Optional.of(existing));

        String code = urlMapping.encode(VALID_URL, null);

        assertEquals("1", code);
        verify(repository, never()).save(any()); // no new DB row
    }

    // --- encode: custom aliases ---

    @Test
    void encode_customAlias_storesAndReturnsAlias() {
        when(repository.findByShortCode("myalias")).thenReturn(Optional.empty());

        String code = urlMapping.encode(VALID_URL, "myalias");

        assertEquals("myalias", code);
        verify(repository).save(any());
    }

    @Test
    void encode_customAlias_sameUrl_isIdempotent() {
        UrlMappingEntity existing = new UrlMappingEntity(1L, "myalias", VALID_URL);
        when(repository.findByShortCode("myalias")).thenReturn(Optional.of(existing));

        String code = urlMapping.encode(VALID_URL, "myalias");

        assertEquals("myalias", code);
        verify(repository, never()).save(any());
    }

    @Test
    void encode_customAlias_differentUrl_throws409() {
        UrlMappingEntity existing = new UrlMappingEntity(1L, "myalias", "https://other.com");
        when(repository.findByShortCode("myalias")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> urlMapping.encode(VALID_URL, "myalias"));
    }

    // --- encode: URL validation ---

    @Test
    void encode_ftpScheme_throwsBadRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> urlMapping.encode("ftp://example.com/file", null));
    }

    @Test
    void encode_noScheme_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> urlMapping.encode("example.com", null));
    }

    @Test
    void encode_plainText_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> urlMapping.encode("not-a-url", null));
    }

    @Test
    void encode_httpUrl_accepted() {
        UrlMappingEntity savedEntity = new UrlMappingEntity(1L, null, "http://example.com");
        when(repository.findByOriginalUrl("http://example.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(savedEntity);

        assertDoesNotThrow(() -> urlMapping.encode("http://example.com", null));
    }

    // --- decode ---

    @Test
    void decode_knownCode_returnsOriginalUrl() {
        UrlMappingEntity entity = new UrlMappingEntity(1L, "1", VALID_URL);
        when(repository.findByShortCode("1")).thenReturn(Optional.of(entity));

        assertEquals(VALID_URL, urlMapping.decode("1"));
    }

    @Test
    void decode_unknownCode_returnsNull() {
        when(repository.findByShortCode("xyz")).thenReturn(Optional.empty());

        assertNull(urlMapping.decode("xyz"));
    }
}
