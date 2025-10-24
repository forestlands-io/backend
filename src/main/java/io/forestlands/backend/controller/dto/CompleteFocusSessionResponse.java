package io.forestlands.backend.controller.dto;

import io.forestlands.backend.entity.FocusSessionState;

import java.util.List;

public record CompleteFocusSessionResponse(
        FocusSessionState state,
        int validatedMinutes,
        int softCurrencyAwarded,
        WalletSummary wallet,
        List<String> anomalies
) {
    public record WalletSummary(int softCurrency, int hardCurrency) {
    }
}
