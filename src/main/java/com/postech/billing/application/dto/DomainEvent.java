package com.postech.billing.application.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainEvent(
        String eventId,
        String eventType,
        Instant ocorridoEm,
        String ordemServicoId,
        String clienteId,
        Map<String, Object> payload
) {
    public static DomainEvent of(String eventType, String ordemServicoId, String clienteId, Map<String, Object> payload) {
        return new DomainEvent(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now(),
                ordemServicoId,
                clienteId,
                payload
        );
    }
}
