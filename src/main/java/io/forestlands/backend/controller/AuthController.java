package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.LoginRequest;
import io.forestlands.backend.controller.dto.RefreshRequest;
import io.forestlands.backend.controller.dto.TokenResponse;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.security.TokenService;
import io.forestlands.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService users;
    private final TokenService tokens;

    private final long accessMinutes;

    public AuthController(UserService users,
                          TokenService tokens,
                          @Value("${jwt.access-token-minutes}") long accessMinutes) {
        this.users = users;
        this.tokens = tokens;
        this.accessMinutes = accessMinutes;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest body) {
        String clientId = (body.clientId() == null || body.clientId().isBlank()) ? "default" : body.clientId();

        User user = users.findByEmail(body.email())
                .filter(u -> users.matchesPassword(u, body.password()))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String access = tokens.createAccessToken(user, clientId);
        String refresh = tokens.createRefreshToken(user, clientId);

        return ResponseEntity.ok(new TokenResponse(
                access,
                accessMinutes * 60,
                refresh,
                "Bearer"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest body) {
        String clientId = (body.clientId() == null || body.clientId().isBlank()) ? "default" : body.clientId();

        Jwt refreshJwt = tokens.decodeAndValidateRefresh(body.refreshToken());

        // (Optional) You could check clientId consistency:
        // if (!clientId.equals(refreshJwt.getClaimAsString("cid"))) { throw ... }

        // Load user to ensure it still exists (no token persistence required)
        String userId = refreshJwt.getSubject();
        var user = users.findByUuid(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));

        String newAccess = tokens.createAccessToken(user, clientId);
        String newRefresh = tokens.createRefreshToken(user, clientId); // rotate (even without persistence)

        return ResponseEntity.ok(new TokenResponse(
                newAccess,
                accessMinutes * 60,
                newRefresh,
                "Bearer"
        ));
    }
}
