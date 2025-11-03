package io.forestlands.backend.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.UUID;

public final class JwtUserUtils {

    private static final String CLAIM_EMAIL = "email";

    private JwtUserUtils() {
    }

    public static String getUuidFromToken(Jwt jwt) {
        UUID uuid = getUuidAsUuid(jwt);
        return uuid.toString();
    }

    public static UUID getUuidAsUuid(Jwt jwt) {
        Objects.requireNonNull(jwt, "jwt must not be null");
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("JWT subject (sub) is missing");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT subject (sub) is not a valid UUID: " + sub, ex);
        }
    }

    public static String getEmailFromToken(Jwt jwt) {
        Objects.requireNonNull(jwt, "jwt must not be null");
        String email = jwt.getClaimAsString(CLAIM_EMAIL);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT does not contain an email claim");
        }
        return email;
    }
}