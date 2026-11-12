package com.postech.billing.application.service;

import com.postech.billing.application.dto.CheckoutResponse;
import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.dto.WebhookNotificationRequest;
import com.postech.billing.application.port.EventPublisherPort;
import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.application.port.PagamentoRepositoryPort;
import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.exception.ResourceNotFoundException;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.domain.model.StatusOrcamento;
import com.postech.billing.domain.model.StatusPagamento;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepositoryPort pagamentoRepository;
    @Mock
    private OrcamentoService orcamentoService;
    @Mock
    private MercadoPagoGateway mercadoPagoGateway;
    @Mock
    private EventPublisherPort eventPublisher;

    private PagamentoService service;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(
                pagamentoRepository,
                orcamentoService,
                mercadoPagoGateway,
                eventPublisher,
                new BillingRabbitProperties()
        );
    }

    @Test
    void shouldRequestPayment() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoService.findEntityById(orcamento.getId())).thenReturn(orcamento);
        when(pagamentoRepository.findByOrdemServicoId("os-1")).thenReturn(Optional.empty());
        when(mercadoPagoGateway.createPreference(any(), any()))
                .thenReturn(new MercadoPagoGateway.CheckoutPreference("pref-1", "https://checkout"));
        when(mercadoPagoGateway.autoApproveOnRequest()).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DomainEvent event = DomainEvent.of(
                "PagamentoSolicitar",
                "os-1",
                "cli-1",
                Map.of("orcamentoId", orcamento.getId(), "valor", 200)
        );

        Pagamento pagamento = service.solicitarPagamento(event);

        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
        assertEquals("pref-1", pagamento.getMercadoPagoId());
        assertEquals("https://checkout", pagamento.getCheckoutUrl());
    }

    @Test
    void shouldRejectPaymentWithoutBudget() {
        when(orcamentoService.findByOrdemServicoId("os-1")).thenReturn(List.of());
        DomainEvent event = DomainEvent.of("PagamentoSolicitar", "os-1", "cli-1", Map.of());
        assertThrows(ResourceNotFoundException.class, () -> service.solicitarPagamento(event));
    }

    @Test
    void shouldRejectPaymentForCancelledBudget() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        orcamento.cancelar();
        when(orcamentoService.findEntityById(orcamento.getId())).thenReturn(orcamento);

        DomainEvent event = DomainEvent.of(
                "PagamentoSolicitar",
                "os-1",
                "cli-1",
                Map.of("orcamentoId", orcamento.getId())
        );

        assertThrows(BusinessException.class, () -> service.solicitarPagamento(event));
    }

    @Test
    void shouldCreateCheckout() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoService.findEntityById(orcamento.getId())).thenReturn(orcamento);
        when(pagamentoRepository.findByOrcamentoId(orcamento.getId())).thenReturn(Optional.empty());
        when(mercadoPagoGateway.createPreference(any(), any()))
                .thenReturn(new MercadoPagoGateway.CheckoutPreference("pref-2", "https://mp"));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = service.criarCheckout(orcamento.getId());

        assertEquals("pref-2", response.preferenceId());
        assertEquals("https://mp", response.initPoint());
    }

    @Test
    void shouldApprovePaymentViaWebhook() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        Pagamento pagamento = Pagamento.create(orcamento.getId(), "os-1", BigDecimal.TEN);
        pagamento.attachCheckout("pref-1", "https://mp");

        when(mercadoPagoGateway.getPaymentStatus("pay-1"))
                .thenReturn(new MercadoPagoGateway.PaymentStatus("pref-1", "approved", Map.of()));
        when(pagamentoRepository.findByMercadoPagoId("pref-1")).thenReturn(Optional.of(pagamento));
        when(orcamentoService.findEntityById(orcamento.getId())).thenReturn(orcamento);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoService.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processWebhook(new WebhookNotificationRequest("1", "payment", "payment.updated", Map.of("id", "pay-1")));

        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        verify(eventPublisher).publish(eq("pagamento.aprovado"), any(DomainEvent.class));
    }

    @Test
    void shouldRejectPaymentViaStatus() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        Pagamento pagamento = Pagamento.create(orcamento.getId(), "os-1", BigDecimal.TEN);
        when(orcamentoService.findEntityById(orcamento.getId())).thenReturn(orcamento);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoService.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyPaymentStatus(pagamento, "rejected", "mp-1", "cli-1");

        assertEquals(StatusPagamento.RECUSADO, pagamento.getStatus());
        verify(eventPublisher).publish(eq("pagamento.falhou"), any(DomainEvent.class));
    }

    @Test
    void shouldCancelPayment() {
        Pagamento pagamento = Pagamento.create(UUID.randomUUID().toString(), "os-1", BigDecimal.ONE);
        when(pagamentoRepository.findById(pagamento.getId())).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelar(pagamento.getId());

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(pagamentoRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById("x"));
    }

    @Test
    void shouldIgnoreWebhookWithoutData() {
        service.processWebhook(null);
        service.processWebhook(new WebhookNotificationRequest(null, null, null, null));
        verify(mercadoPagoGateway, never()).getPaymentStatus(any());
    }
}
