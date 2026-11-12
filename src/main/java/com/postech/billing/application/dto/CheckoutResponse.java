package com.postech.billing.application.dto;

public record CheckoutResponse(
        String pagamentoId,
        String preferenceId,
        String initPoint,
        String status
) {
}
