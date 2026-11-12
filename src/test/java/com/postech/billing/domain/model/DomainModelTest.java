package com.postech.billing.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainModelTest {

    @Test
    void shouldCreateBudgetWithTotal() {
        Orcamento orcamento = Orcamento.create("os", "cli", List.of(
                new OrcamentoItem("Part", "PECA", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        ));
        assertEquals(new BigDecimal("20.00"), orcamento.getValorTotal());
        assertEquals(StatusOrcamento.PENDENTE, orcamento.getStatus());
    }

    @Test
    void shouldNotChangeCancelledBudget() {
        Orcamento orcamento = Orcamento.create("os", "cli", List.of());
        orcamento.cancelar();
        assertThrows(IllegalStateException.class, orcamento::markAsEnviado);
        assertThrows(IllegalStateException.class, orcamento::aprovar);
        assertThrows(IllegalStateException.class, orcamento::rejeitar);
    }

    @Test
    void shouldManagePaymentLifecycle() {
        Pagamento pagamento = Pagamento.create("orc", "os", BigDecimal.TEN);
        pagamento.attachCheckout("mp", "url");
        pagamento.aprovar("mp-approved");
        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());

        Pagamento rejected = Pagamento.create("orc", "os", BigDecimal.ONE);
        rejected.recusar();
        assertEquals(StatusPagamento.RECUSADO, rejected.getStatus());

        Pagamento cancelled = Pagamento.create("orc", "os", BigDecimal.ONE);
        cancelled.cancelar();
        assertEquals(StatusPagamento.CANCELADO, cancelled.getStatus());
        assertThrows(IllegalStateException.class, () -> cancelled.aprovar("x"));

        Pagamento refunded = Pagamento.create("orc", "os", BigDecimal.ONE);
        refunded.reembolsar();
        assertEquals(StatusPagamento.REEMBOLSADO, refunded.getStatus());
    }
}
