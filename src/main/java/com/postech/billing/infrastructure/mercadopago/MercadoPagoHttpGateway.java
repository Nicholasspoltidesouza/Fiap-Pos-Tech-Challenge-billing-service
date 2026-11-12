package com.postech.billing.infrastructure.mercadopago;

import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.infrastructure.config.MercadoPagoProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "mercado-pago", name = "mock-enabled", havingValue = "false")
public class MercadoPagoHttpGateway implements MercadoPagoGateway {

    private final RestClient restClient;
    private final MercadoPagoProperties properties;

    public MercadoPagoHttpGateway(RestClient.Builder restClientBuilder, MercadoPagoProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getAccessToken())
                .build();
    }

    @Override
    public CheckoutPreference createPreference(Orcamento orcamento, Pagamento pagamento) {
        Map<String, Object> body = new HashMap<>();
        body.put("external_reference", pagamento.getId());
        body.put("notification_url", properties.getNotificationUrl());
        body.put("items", List.of(Map.of(
                "title", "Service Order " + orcamento.getOrdemServicoId(),
                "quantity", 1,
                "currency_id", "BRL",
                "unit_price", orcamento.getValorTotal()
        )));
        body.put("back_urls", Map.of(
                "success", properties.getSuccessUrl(),
                "failure", properties.getFailureUrl(),
                "pending", properties.getPendingUrl()
        ));
        body.put("auto_return", "approved");

        Map<String, Object> response = restClient.post()
                .uri("/checkout/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || response.get("id") == null) {
            throw new BusinessException("Mercado Pago preference creation failed");
        }

        String preferenceId = String.valueOf(response.get("id"));
        Object initPoint = response.getOrDefault("init_point", response.get("sandbox_init_point"));
        return new CheckoutPreference(preferenceId, String.valueOf(initPoint));
    }

    @Override
    public PaymentStatus getPaymentStatus(String mercadoPagoPaymentId) {
        Map<String, Object> response = restClient.get()
                .uri("/v1/payments/{id}", mercadoPagoPaymentId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || response.get("id") == null) {
            throw new BusinessException("Mercado Pago payment not found: " + mercadoPagoPaymentId);
        }

        return new PaymentStatus(
                String.valueOf(response.get("id")),
                String.valueOf(response.get("status")),
                response
        );
    }
}
