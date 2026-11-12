package com.postech.billing.infrastructure.mercadopago;

import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.infrastructure.config.MercadoPagoProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoMockGatewayTest {

    private final MercadoPagoMockGateway gateway = new MercadoPagoMockGateway(new MercadoPagoProperties());

    @Test
    void shouldCreateMockPreference() {
        Orcamento orcamento = Orcamento.create("os", "cli", List.of());
        Pagamento pagamento = Pagamento.create(orcamento.getId(), "os", BigDecimal.TEN);

        MercadoPagoGateway.CheckoutPreference preference = gateway.createPreference(orcamento, pagamento);

        assertTrue(preference.preferenceId().startsWith("mock-pref-"));
        assertTrue(preference.initPoint().contains(preference.preferenceId()));
    }

    @Test
    void shouldReturnApprovedStatus() {
        MercadoPagoGateway.PaymentStatus status = gateway.getPaymentStatus("mock-pay-1");
        assertEquals("approved", status.status());
        assertEquals("mock-pay-1", status.id());
    }
}
