package com.postech.billing.application.service;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.port.EventPublisherPort;
import com.postech.billing.application.port.OrcamentoRepositoryPort;
import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.exception.ResourceNotFoundException;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.StatusOrcamento;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepositoryPort orcamentoRepository;
    @Mock
    private EventPublisherPort eventPublisher;

    private OrcamentoService service;

    @BeforeEach
    void setUp() {
        BillingRabbitProperties properties = new BillingRabbitProperties();
        service = new OrcamentoService(orcamentoRepository, eventPublisher, properties);
    }

    @Test
    void shouldGenerateBudgetAndPublishEvent() {
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DomainEvent event = DomainEvent.of(
                "OrcamentoGerar",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                Map.of("itens", List.of(Map.of(
                        "descricao", "Oil change",
                        "tipo", "SERVICO",
                        "quantidade", 1,
                        "valorUnitario", 150.00
                )))
        );

        Orcamento saved = service.gerarOrcamento(event);

        assertEquals(StatusOrcamento.ENVIADO, saved.getStatus());
        assertEquals(new BigDecimal("150.0"), saved.getValorTotal());
        verify(eventPublisher).publish(eq("orcamento.gerado"), any(DomainEvent.class));
    }

    @Test
    void shouldRejectMissingOrdemServicoId() {
        DomainEvent event = DomainEvent.of("OrcamentoGerar", " ", "cliente", Map.of());
        assertThrows(BusinessException.class, () -> service.gerarOrcamento(event));
    }

    @Test
    void shouldFindById() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoRepository.findById(orcamento.getId())).thenReturn(Optional.of(orcamento));

        assertEquals(orcamento.getId(), service.findById(orcamento.getId()).id());
    }

    @Test
    void shouldThrowWhenBudgetNotFound() {
        when(orcamentoRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void shouldCancelBudget() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoRepository.findById(orcamento.getId())).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelar(orcamento.getId());

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoRepository).save(captor.capture());
        assertEquals(StatusOrcamento.CANCELADO, captor.getValue().getStatus());
    }

    @Test
    void shouldCancelByOrdemServico() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoRepository.findByOrdemServicoId("os-1")).thenReturn(List.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelarPorOrdemServico("os-1");

        verify(orcamentoRepository).save(any(Orcamento.class));
        assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
    }

    @Test
    void shouldListByOrdemServico() {
        Orcamento orcamento = Orcamento.create("os-1", "cli-1", List.of());
        when(orcamentoRepository.findByOrdemServicoId("os-1")).thenReturn(List.of(orcamento));

        assertEquals(1, service.findByOrdemServicoId("os-1").size());
    }
}
