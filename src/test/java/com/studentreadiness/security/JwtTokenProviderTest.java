package com.studentreadiness.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    @Test
    void acceptsASecretWithAtLeast256Bits() {
        assertDoesNotThrow(() -> new JwtTokenProvider(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", 86_400_000L));
    }

    @Test
    void rejectsAnInvalidShortSecretAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenProvider("too-short", 86_400_000L));
    }
}
