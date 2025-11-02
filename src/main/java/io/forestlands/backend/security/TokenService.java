package io.forestlands.backend.security;

import io.forestlands.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    private final String issuer;
    private final long accessMinutes;
    private final long refreshDays;

    public TokenService(
            JwtEncoder encoder,
            JwtDecoder decoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-token-minutes}") long accessMinutes,
            @Value("${jwt.refresh-token-days}") long refreshDays) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public String createAccessToken(User user, String clientId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessMinutes, ChronoUnit.MINUTES))
                .subject(user.getUuid().toString())
                .id(UUID.randomUUID().toString()) // jti
                .claim("email", user.getEmail())
                .claim("cid", clientId)           // client identifier (from mobile)
                .claim("typ", "access")           // our own token-type
                .claim("scope", "user")           // minimal authority
                .build();

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public String createRefreshToken(User user, String clientId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(refreshDays, ChronoUnit.DAYS))
                .subject(user.getUuid().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("cid", clientId)
                .claim("typ", "refresh")
                .build();

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    /**
     * Decode & basic checks for refresh tokens. Throws on invalid/expired.
     */
    public Jwt decodeAndValidateRefresh(String refreshToken) {
        Jwt jwt = decoder.decode(refreshToken); // signature + exp checked here
        String typ = jwt.getClaimAsString("typ");
        if (!"refresh".equals(typ)) {
            throw new JwtException("Invalid token type");
        }
        return jwt;
    }
}
