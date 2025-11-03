package io.forestlands.backend.controller;

import io.forestlands.backend.security.JwtUserUtils;
import io.forestlands.backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal Jwt jwt) {
        String userEmail = JwtUserUtils.getEmailFromToken(jwt);
        String userUuid  = JwtUserUtils.getUuidFromToken(jwt);
        return "ok";
    }
}
