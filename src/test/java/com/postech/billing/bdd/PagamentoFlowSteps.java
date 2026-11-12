package com.postech.billing.bdd;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.port.EventPublisherPort;
import com.postech.billing.application.port.MercadoPagoGateway;
import com.postech.billing.application.port.OrcamentoRepositoryPort;
import com.postech.billing.application.port.PagamentoRepositoryPort;
import com.postech.billing.application.service.OrcamentoService;
import com.postech.billing.application.service.PagamentoService;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.domain.model.StatusPagamento;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PagamentoFlowSteps {

    private final Map<String, Orcamento> orcamentos = new ConcurrentHashMap<>();
    private final Map<String, Pagamento> pagamentos = new ConcurrentHashMap<>();
    private final List<PublishedEvent> publishedEvents = new ArrayList<>();

    private String ordemServicoId;
    private String clienteId;
    private Orcamento orcamento;
    private Pagamento pagamento;

    private OrcamentoService orcamentoService;
    private PagamentoService pagamentoService;

    @Given("a service order ready for billing")
    public void aServiceOrderReadyForBilling() {
        ordemServicoId = UUID.randomUUID().toString();
        clienteId = UUID.randomUUID().toString();
        publishedEvents.clear();
        orcamentos.clear();
        pagamentos.clear();

        OrcamentoRepositoryPort orcamentoRepository = new OrcamentoRepositoryPort() {
            @Override
            public Orcamento save(Orcamento entity) {
                orcamentos.put(entity.getId(), entity);
                return entity;
            }

            @Override
            public Optional<Orcamento> findById(String id) {
                return Optional.ofNullable(orcamentos.get(id));
            }

            @Override
            public List<Orcamento> findByOrdemServicoId(String id) {
                return orcamentos.values().stream().filter(o -> o.getOrdemServicoId().equals(id)).toList();
            }
        };

        PagamentoRepositoryPort pagamentoRepository = new PagamentoRepositoryPort() {
            @Override
            public Pagamento save(Pagamento entity) {
                pagamentos.put(entity.getId(), entity);
                return entity;
            }

            @Override
            public Optional<Pagamento> findById(String id) {
                return Optional.ofNullable(pagamentos.get(id));
            }

            @Override
            public Optional<Pagamento> findByOrcamentoId(String orcamentoId) {
                return pagamentos.values().stream().filter(p -> p.getOrcamentoId().equals(orcamentoId)).findFirst();
            }

            @Override
            public Optional<Pagamento> findByMercadoPagoId(String mercadoPagoId) {
                return pagamentos.values().stream().filter(p -> mercadoPagoId.equals(p.getMercadoPagoId())).findFirst();
            }

            @Override
            public Optional<Pagamento> findByOrdemServicoId(String id) {
                return pagamentos.values().stream().filter(p -> p.getOrdemServicoId().equals(id)).findFirst();
            }
        };

        EventPublisherPort publisher = (routingKey, event) -> publishedEvents.add(new PublishedEvent(routingKey, event));
        MercadoPagoGateway gateway = new MercadoPagoGateway() {
            @Override
            public CheckoutPreference createPreference(Orcamento o, Pagamento p) {
                return new CheckoutPreference("pref-" + p.getId(), "https://mock-checkout/" + p.getId());
            }

            @Override
            public PaymentStatus getPaymentStatus(String mercadoPagoPaymentId) {
                return new PaymentStatus(mercadoPagoPaymentId, "approved", Map.of());
            }
        };

        BillingRabbitProperties properties = new BillingRabbitProperties();
        orcamentoService = new OrcamentoService(orcamentoRepository, publisher, properties);
        pagamentoService = new PagamentoService(pagamentoRepository, orcamentoService, gateway, publisher, properties);
    }

    @When("a budget is generated with total {bigdecimal}")
    public void aBudgetIsGeneratedWithTotal(BigDecimal total) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("itens", List.of(Map.of(
                "descricao", "Service package",
                "tipo", "SERVICO",
                "quantidade", 1,
                "valorUnitario", total,
                "valorTotal", total
        )));
        DomainEvent event = DomainEvent.of("OrcamentoGerar", ordemServicoId, clienteId, payload);
        orcamento = orcamentoService.gerarOrcamento(event);
    }

    @And("a checkout is created for the budget")
    public void aCheckoutIsCreatedForTheBudget() {
        var checkout = pagamentoService.criarCheckout(orcamento.getId());
        pagamento = pagamentos.get(checkout.pagamentoId());
    }

    @And("Mercado Pago approves the payment")
    public void mercadoPagoApprovesThePayment() {
        pagamentoService.applyPaymentStatus(pagamento, "approved", pagamento.getMercadoPagoId(), clienteId);
    }

    @And("Mercado Pago rejects the payment")
    public void mercadoPagoRejectsThePayment() {
        pagamentoService.applyPaymentStatus(pagamento, "rejected", pagamento.getMercadoPagoId(), clienteId);
    }

    @Then("the payment status should be {word}")
    public void thePaymentStatusShouldBe(String status) {
        assertEquals(StatusPagamento.valueOf(status), pagamento.getStatus());
    }

    @And("a payment approved event should be published")
    public void aPaymentApprovedEventShouldBePublished() {
        assertTrue(publishedEvents.stream().anyMatch(e -> "pagamento.aprovado".equals(e.routingKey())));
    }

    @And("a payment failed event should be published")
    public void aPaymentFailedEventShouldBePublished() {
        assertTrue(publishedEvents.stream().anyMatch(e -> "pagamento.falhou".equals(e.routingKey())));
    }

    private record PublishedEvent(String routingKey, DomainEvent event) {
    }
}
