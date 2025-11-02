package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.LoginRequest;
import io.forestlands.backend.controller.dto.LoginResponse;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public String me() {
        return "ok";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body) {
        User user = userService
                .findByEmail(body.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!userService.matchesPassword(user, body.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        LoginResponse response = new LoginResponse(user.getUuid(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}
