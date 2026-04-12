package com.culturaalsur.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    // Same Base64 secret used in application-test.yaml
    private static final String SECRET =
            "dGVzdFNlY3JldEtleUZvclRlc3RpbmdPbmx5MTIzNDU2Nzg5MA==";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", EXPIRATION_MS);
    }

    // ── generateToken ────────────────────────────────────────────────────────────

    @Test
    void generateToken_returnsNonBlankToken() {
        assertThat(jwtUtils.generateToken("juanjls")).isNotBlank();
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String t1 = jwtUtils.generateToken("alice");
        String t2 = jwtUtils.generateToken("bob");
        assertThat(t1).isNotEqualTo(t2);
    }

    // ── getUsernameFromToken ──────────────────────────────────────────────────────

    @Test
    void getUsernameFromToken_returnsCorrectSubject() {
        String token = jwtUtils.generateToken("juanjls");
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("juanjls");
    }

    // ── validateToken ─────────────────────────────────────────────────────────────

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtils.generateToken("juanjls");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_tamperedSignature_returnsFalse() {
        String token = jwtUtils.generateToken("juanjls") + "X";
        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_randomString_returnsFalse() {
        assertThat(jwtUtils.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        // Build a token that expired before it was created (negative expiry)
        JwtUtils shortLived = new JwtUtils();
        ReflectionTestUtils.setField(shortLived, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expirationMs", -1000L);
        String expired = shortLived.generateToken("juanjls");

        assertThat(jwtUtils.validateToken(expired)).isFalse();
    }

    @Test
    void validateToken_tokenSignedWithDifferentSecret_returnsFalse() {
        String otherSecret =
                "b3RoZXJTZWNyZXRLZXlGb3JUZXN0aW5nT25seVhYWFhYWFg=";
        JwtUtils other = new JwtUtils();
        ReflectionTestUtils.setField(other, "jwtSecret", otherSecret);
        ReflectionTestUtils.setField(other, "expirationMs", EXPIRATION_MS);
        String foreignToken = other.generateToken("juanjls");

        assertThat(jwtUtils.validateToken(foreignToken)).isFalse();
    }
}
