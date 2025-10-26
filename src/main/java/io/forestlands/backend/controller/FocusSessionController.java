package io.forestlands.backend.controller;

import io.forestlands.backend.controller.dto.CompleteFocusSessionRequest;
import io.forestlands.backend.controller.dto.CompleteFocusSessionResponse;
import io.forestlands.backend.controller.dto.CompleteFocusSessionResponse.WalletSummary;
import io.forestlands.backend.controller.dto.StartFocusSessionRequest;
import io.forestlands.backend.controller.dto.StartFocusSessionResponse;
import io.forestlands.backend.entity.FocusSession;
import io.forestlands.backend.entity.User;
import io.forestlands.backend.service.FocusSessionService;
import io.forestlands.backend.service.FocusSessionService.FocusSessionCompletionResult;
import io.forestlands.backend.service.FocusSessionService.FocusSessionStartResult;
import io.forestlands.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/focus/sessions")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;
    private final UserService userService;

    public FocusSessionController(FocusSessionService focusSessionService, UserService userService) {
        this.focusSessionService = focusSessionService;
        this.userService = userService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartFocusSessionResponse> startSession(@Valid @RequestBody StartFocusSessionRequest request,
                                                                  Authentication authentication) {
        User user = resolveUser(authentication);
        try {
            FocusSessionStartResult result = focusSessionService.startSession(
                    user,
                    request.sessionUuid(),
                    request.speciesCode(),
                    request.clientStartTime(),
                    request.plannedMinutes(),
                    request.tag()
            );

            FocusSession session = result.session();
            StartFocusSessionResponse response = new StartFocusSessionResponse(
                    session.getId(),
                    session.getUuid(),
                    session.getServerStartTime(),
                    session.getState()
            );

            HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (IllegalArgumentException ex) {
            throw mapException(ex);
        }
    }

    @PostMapping("/{sessionUuid}/end")
    public ResponseEntity<CompleteFocusSessionResponse> completeSession(@PathVariable UUID sessionUuid,
                                                                        @Valid @RequestBody CompleteFocusSessionRequest request,
                                                                        Authentication authentication) {
        User user = resolveUser(authentication);
        try {
            FocusSessionCompletionResult result = focusSessionService.completeSession(
                    user,
                    sessionUuid,
                    request.clientEndTime(),
                    request.state()
            );

            CompleteFocusSessionResponse response = new CompleteFocusSessionResponse(
                    result.session().getState(),
                    result.validatedMinutes(),
                    result.softCurrencyAwarded(),
                    new WalletSummary(result.wallet().getSoftCurrency(), result.wallet().getHardCurrency())
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw mapException(ex);
        }
    }

    private User resolveUser(Authentication authentication) {
        String email = authentication.getName();
        return userService
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private ResponseStatusException mapException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Invalid request" : ex.getMessage();
        if (message.toLowerCase().contains("not found")) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (message.toLowerCase().contains("unauthorized")) {
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
        }
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message, ex);
    }
}
