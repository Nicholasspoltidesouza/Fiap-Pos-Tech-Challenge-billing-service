package com.postech.billing.infrastructure.messaging;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.service.CompensationService;
import com.postech.billing.application.service.OrcamentoService;
import com.postech.billing.application.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingEventListenerTest {

    @Mock
    private OrcamentoService orcamentoService;
    @Mock
    private PagamentoService pagamentoService;
    @Mock
    private CompensationService compensationService;

    private BillingEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BillingEventListener(orcamentoService, pagamentoService, compensationService);
    }

    @Test
    void shouldDelegateEvents() {
        DomainEvent event = DomainEvent.of("Any", "os", "cli", Map.of());

        listener.onOrcamentoGerar(event);
        listener.onPagamentoSolicitar(event);
        listener.onCompensarOrcamento(event);
        listener.onCompensarPagamento(event);

        verify(orcamentoService).gerarOrcamento(event);
        verify(pagamentoService).solicitarPagamento(event);
        verify(compensationService).compensarOrcamento(event);
        verify(compensationService).compensarPagamento(event);
    }
}
