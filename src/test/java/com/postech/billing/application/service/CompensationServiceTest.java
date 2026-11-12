package com.postech.billing.application.service;

import com.postech.billing.application.dto.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompensationServiceTest {

    @Mock
    private OrcamentoService orcamentoService;
    @Mock
    private PagamentoService pagamentoService;

    private CompensationService service;

    @BeforeEach
    void setUp() {
        service = new CompensationService(orcamentoService, pagamentoService);
    }

    @Test
    void shouldCompensateBudgetById() {
        DomainEvent event = DomainEvent.of("CompensarOrcamento", "os-1", "cli-1", Map.of("orcamentoId", "orc-1"));
        service.compensarOrcamento(event);
        verify(orcamentoService).cancelar("orc-1");
    }

    @Test
    void shouldCompensateBudgetByOrdemServico() {
        DomainEvent event = DomainEvent.of("CompensarOrcamento", "os-1", "cli-1", Map.of());
        service.compensarOrcamento(event);
        verify(orcamentoService).cancelarPorOrdemServico("os-1");
    }

    @Test
    void shouldCompensatePaymentById() {
        DomainEvent event = DomainEvent.of("CompensarPagamento", "os-1", "cli-1", Map.of("pagamentoId", "pay-1"));
        service.compensarPagamento(event);
        verify(pagamentoService).cancelar("pay-1");
    }

    @Test
    void shouldCompensatePaymentByOrdemServico() {
        DomainEvent event = DomainEvent.of("CompensarPagamento", "os-1", "cli-1", Map.of());
        service.compensarPagamento(event);
        verify(pagamentoService).cancelarPorOrdemServico("os-1");
    }
}
