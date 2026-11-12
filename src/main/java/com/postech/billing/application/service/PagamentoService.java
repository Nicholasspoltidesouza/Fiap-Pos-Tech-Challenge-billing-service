package com.postech.billing.application.service;

import com.postech.billing.application.dto.CheckoutResponse;
import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.dto.PagamentoResponse;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PagamentoService {

    private final PagamentoRepositoryPort pagamentoRepository;
    private final OrcamentoService orcamentoService;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final EventPublisherPort eventPublisher;
    private final BillingRabbitProperties rabbitProperties;

    public PagamentoService(
            PagamentoRepositoryPort pagamentoRepository,
            OrcamentoService orcamentoService,
            MercadoPagoGateway mercadoPagoGateway,
            EventPublisherPort eventPublisher,
            BillingRabbitProperties rabbitProperties
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.orcamentoService = orcamentoService;
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.eventPublisher = eventPublisher;
        this.rabbitProperties = rabbitProperties;
    }

    public Pagamento solicitarPagamento(DomainEvent event) {
        Orcamento orcamento = resolveOrcamento(event);
        if (orcamento.getStatus() == StatusOrcamento.CANCELADO) {
            throw new BusinessException("Cannot request payment for cancelled budget");
        }

        BigDecimal valor = resolveValor(event, orcamento);

        Pagamento pagamento = pagamentoRepository.findByOrdemServicoId(orcamento.getOrdemServicoId())
                .orElseGet(() -> Pagamento.create(orcamento.getId(), orcamento.getOrdemServicoId(), valor));

        if (pagamento.getCheckoutUrl() == null || pagamento.getCheckoutUrl().isBlank()) {
            MercadoPagoGateway.CheckoutPreference preference = mercadoPagoGateway.createPreference(orcamento, pagamento);
            pagamento.attachCheckout(preference.preferenceId(), preference.initPoint());
        }

        pagamento = pagamentoRepository.save(pagamento);

        if (mercadoPagoGateway.autoApproveOnRequest()) {
            applyPaymentStatus(pagamento, "approved", pagamento.getMercadoPagoId(), event.clienteId());
        }

        return pagamento;
    }

    private Orcamento resolveOrcamento(DomainEvent event) {
        String orcamentoId = extractString(event.payload(), "orcamentoId");
        if (orcamentoId != null && !orcamentoId.isBlank()) {
            return orcamentoService.findEntityById(orcamentoId);
        }
        return orcamentoService.findByOrdemServicoId(event.ordemServicoId()).stream()
                .findFirst()
                .map(response -> orcamentoService.findEntityById(response.id()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found for service order: " + event.ordemServicoId()));
    }

    private BigDecimal resolveValor(DomainEvent event, Orcamento orcamento) {
        if (event.payload() == null) {
            return orcamento.getValorTotal();
        }
        if (event.payload().containsKey("valor")) {
            return toBigDecimal(event.payload().get("valor"));
        }
        if (event.payload().containsKey("valorOrcamento")) {
            return toBigDecimal(event.payload().get("valorOrcamento"));
        }
        return orcamento.getValorTotal();
    }

    public CheckoutResponse criarCheckout(String orcamentoId) {
        Orcamento orcamento = orcamentoService.findEntityById(orcamentoId);
        if (orcamento.getStatus() == StatusOrcamento.CANCELADO) {
            throw new BusinessException("Cannot checkout a cancelled budget");
        }

        Pagamento pagamento = pagamentoRepository.findByOrcamentoId(orcamentoId)
                .orElseGet(() -> Pagamento.create(orcamento.getId(), orcamento.getOrdemServicoId(), orcamento.getValorTotal()));

        if (pagamento.getCheckoutUrl() == null || pagamento.getCheckoutUrl().isBlank()) {
            MercadoPagoGateway.CheckoutPreference preference = mercadoPagoGateway.createPreference(orcamento, pagamento);
            pagamento.attachCheckout(preference.preferenceId(), preference.initPoint());
            pagamento = pagamentoRepository.save(pagamento);
        }

        return new CheckoutResponse(
                pagamento.getId(),
                pagamento.getMercadoPagoId(),
                pagamento.getCheckoutUrl(),
                pagamento.getStatus().name()
        );
    }

    public PagamentoResponse findById(String id) {
        return PagamentoResponse.from(findEntityById(id));
    }

    public Pagamento findEntityById(String id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    public void processWebhook(WebhookNotificationRequest request) {
        if (request == null || request.data() == null) {
            return;
        }
        Object paymentId = request.data().get("id");
        if (paymentId == null) {
            return;
        }

        MercadoPagoGateway.PaymentStatus status = mercadoPagoGateway.getPaymentStatus(String.valueOf(paymentId));
        Pagamento pagamento = pagamentoRepository.findByMercadoPagoId(status.id())
                .or(() -> pagamentoRepository.findByMercadoPagoId(String.valueOf(paymentId)))
                .orElse(null);

        if (pagamento == null) {
            return;
        }

        applyPaymentStatus(pagamento, status.status(), status.id(), null);
    }

    public void applyPaymentStatus(Pagamento pagamento, String mpStatus, String mercadoPagoId, String clienteId) {
        String normalized = mpStatus == null ? "" : mpStatus.toLowerCase();
        Orcamento orcamento = orcamentoService.findEntityById(pagamento.getOrcamentoId());

        if (isApproved(normalized)) {
            pagamento.aprovar(mercadoPagoId);
            orcamento.aprovar();
            pagamentoRepository.save(pagamento);
            orcamentoService.save(orcamento);
            publishApproved(pagamento, orcamento, clienteId);
            return;
        }

        if (isRejected(normalized)) {
            pagamento.recusar();
            orcamento.rejeitar();
            pagamentoRepository.save(pagamento);
            orcamentoService.save(orcamento);
            publishFailed(pagamento, orcamento, clienteId, "Payment rejected by Mercado Pago");
            return;
        }

        pagamentoRepository.save(pagamento);
    }

    public void cancelar(String pagamentoId) {
        Pagamento pagamento = findEntityById(pagamentoId);
        pagamento.cancelar();
        pagamentoRepository.save(pagamento);
    }

    public void cancelarPorOrdemServico(String ordemServicoId) {
        pagamentoRepository.findByOrdemServicoId(ordemServicoId).ifPresent(pagamento -> {
            if (pagamento.getStatus() != StatusPagamento.CANCELADO) {
                pagamento.cancelar();
                pagamentoRepository.save(pagamento);
            }
        });
    }

    private void publishApproved(Pagamento pagamento, Orcamento orcamento, String clienteId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pagamentoId", pagamento.getId());
        payload.put("mercadoPagoId", pagamento.getMercadoPagoId());
        payload.put("valor", pagamento.getValor());
        payload.put("status", pagamento.getStatus().name());

        DomainEvent event = DomainEvent.of(
                "PagamentoAprovado",
                pagamento.getOrdemServicoId(),
                clienteId != null ? clienteId : orcamento.getClienteId(),
                payload
        );
        eventPublisher.publish(rabbitProperties.getRoutingKeys().getPagamentoAprovado(), event);
    }

    private void publishFailed(Pagamento pagamento, Orcamento orcamento, String clienteId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pagamentoId", pagamento.getId());
        payload.put("mercadoPagoId", pagamento.getMercadoPagoId());
        payload.put("valor", pagamento.getValor());
        payload.put("status", pagamento.getStatus().name());
        payload.put("motivo", reason);

        DomainEvent event = DomainEvent.of(
                "PagamentoFalhou",
                pagamento.getOrdemServicoId(),
                clienteId != null ? clienteId : orcamento.getClienteId(),
                payload
        );
        eventPublisher.publish(rabbitProperties.getRoutingKeys().getPagamentoFalhou(), event);
    }

    private boolean isApproved(String status) {
        return "approved".equals(status) || "aprovado".equals(status);
    }

    private boolean isRejected(String status) {
        return "rejected".equals(status)
                || "cancelled".equals(status)
                || "recusado".equals(status)
                || "refunded".equals(status);
    }

    private String extractString(Map<String, Object> payload, String key) {
        if (payload == null || !payload.containsKey(key) || payload.get(key) == null) {
            return null;
        }
        return String.valueOf(payload.get(key));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }
}
