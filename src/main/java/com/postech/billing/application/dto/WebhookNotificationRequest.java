package com.postech.billing.application.dto;

import java.util.Map;

public record WebhookNotificationRequest(
        String id,
        String type,
        String action,
        Map<String, Object> data
) {
}
