package com.urlshortener;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @Test
    void encode_one_returnsSingleCharacter() {
        assertEquals("1", Base62.encode(1));
    }

    @Test
    void encode_large_number_returnsMultipleCharacters() {
        // 62^2 = 3844, so values >= 62 need more than one char
        assertTrue(Base62.encode(62).length() > 1);
    }

    @Test
    void encode_decode_roundtrip() {
        for (long i = 1; i <= 10_000; i++) {
            assertEquals(i, Base62.decode(Base62.encode(i)));
        }
    }

    @Test
    void encode_onlyUrlSafeCharacters() {
        for (long i = 1; i <= 10_000; i++) {
            assertTrue(Base62.encode(i).matches("[0-9a-zA-Z]+"),
                "Code contains non-URL-safe characters: " + Base62.encode(i));
        }
    }

    @Test
    void decode_invalidCharacter_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("hello!"));
    }

    @Test
    void encode_distinctInputs_produceDistinctCodes() {
        long a = 1, b = 2, c = 61, d = 62;
        assertNotEquals(Base62.encode(a), Base62.encode(b));
        assertNotEquals(Base62.encode(c), Base62.encode(d));
    }
}
