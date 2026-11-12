package com.postech.billing.infrastructure.mercadopago;

import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.infrastructure.config.MercadoPagoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoHttpGatewayTest {

    @Test
    void shouldCreatePreferenceAndFetchPayment() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("token");
        properties.setBaseUrl("http://localhost");
        properties.setNotificationUrl("http://localhost/webhook");
        properties.setSuccessUrl("http://ok");
        properties.setFailureUrl("http://fail");
        properties.setPendingUrl("http://pending");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoHttpGateway gateway = new MercadoPagoHttpGateway(builder, properties);

        server.expect(requestTo("http://localhost/checkout/preferences"))
                .andRespond(withSuccess("""
                        {"id":"pref-123","init_point":"https://pay/123"}
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("http://localhost/v1/payments/pay-9"))
                .andRespond(withSuccess("""
                        {"id":"pay-9","status":"approved"}
                        """, MediaType.APPLICATION_JSON));

        Orcamento orcamento = Orcamento.create("os", "cli", List.of());
        Pagamento pagamento = Pagamento.create(orcamento.getId(), "os", BigDecimal.TEN);

        MercadoPagoGateway.CheckoutPreference preference = gateway.createPreference(orcamento, pagamento);
        assertEquals("pref-123", preference.preferenceId());
        assertEquals("https://pay/123", preference.initPoint());

        MercadoPagoGateway.PaymentStatus status = gateway.getPaymentStatus("pay-9");
        assertEquals("approved", status.status());
        server.verify();
    }

    @Test
    void shouldFailWhenPreferenceResponseIsInvalid() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setAccessToken("token");
        properties.setBaseUrl("http://localhost");
        properties.setNotificationUrl("http://localhost/webhook");
        properties.setSuccessUrl("http://ok");
        properties.setFailureUrl("http://fail");
        properties.setPendingUrl("http://pending");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoHttpGateway gateway = new MercadoPagoHttpGateway(builder, properties);

        server.expect(requestTo("http://localhost/checkout/preferences"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        Orcamento orcamento = Orcamento.create("os", "cli", List.of());
        Pagamento pagamento = Pagamento.create(orcamento.getId(), "os", BigDecimal.TEN);

        assertThrows(BusinessException.class, () -> gateway.createPreference(orcamento, pagamento));
    }
}
