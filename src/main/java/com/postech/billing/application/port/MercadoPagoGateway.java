package com.postech.billing.application.port;

import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;

import java.util.Map;

public interface MercadoPagoGateway {

    CheckoutPreference createPreference(Orcamento orcamento, Pagamento pagamento);

    PaymentStatus getPaymentStatus(String mercadoPagoPaymentId);

    default boolean autoApproveOnRequest() {
        return false;
    }

    record CheckoutPreference(String preferenceId, String initPoint) {
    }

    record PaymentStatus(String id, String status, Map<String, Object> raw) {
    }
}
