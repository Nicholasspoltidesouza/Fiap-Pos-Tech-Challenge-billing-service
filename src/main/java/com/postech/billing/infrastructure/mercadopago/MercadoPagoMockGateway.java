package com.postech.billing.infrastructure.mercadopago;

import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.infrastructure.config.MercadoPagoProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "mercado-pago", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MercadoPagoMockGateway implements MercadoPagoGateway {

    private final MercadoPagoProperties properties;

    public MercadoPagoMockGateway(MercadoPagoProperties properties) {
        this.properties = properties;
    }

    @Override
    public CheckoutPreference createPreference(Orcamento orcamento, Pagamento pagamento) {
        String preferenceId = "mock-pref-" + UUID.randomUUID();
        String initPoint = "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=" + preferenceId
                + "&external_reference=" + pagamento.getId();
        return new CheckoutPreference(preferenceId, initPoint);
    }

    @Override
    public PaymentStatus getPaymentStatus(String mercadoPagoPaymentId) {
        return new PaymentStatus(
                mercadoPagoPaymentId,
                "approved",
                Map.of("id", mercadoPagoPaymentId, "status", "approved", "mock", true)
        );
    }

    @Override
    public boolean autoApproveOnRequest() {
        return properties.isAutoApproveOnRequest();
    }
}
