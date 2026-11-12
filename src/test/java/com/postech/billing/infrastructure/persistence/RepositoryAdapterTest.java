package com.postech.billing.infrastructure.persistence;

import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.domain.model.StatusOrcamento;
import com.postech.billing.domain.model.StatusPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryAdapterTest {

    @Mock
    private OrcamentoMongoRepository orcamentoMongoRepository;
    @Mock
    private PagamentoMongoRepository pagamentoMongoRepository;

    @Test
    void shouldMapOrcamento() {
        OrcamentoRepositoryAdapter adapter = new OrcamentoRepositoryAdapter(orcamentoMongoRepository);
        Orcamento orcamento = Orcamento.create("os", "cli", List.of());

        when(orcamentoMongoRepository.save(any(OrcamentoDocument.class))).thenAnswer(invocation -> {
            OrcamentoDocument document = invocation.getArgument(0);
            document.setStatus(StatusOrcamento.ENVIADO);
            return document;
        });
        when(orcamentoMongoRepository.findById(orcamento.getId())).thenAnswer(invocation -> {
            OrcamentoDocument document = new OrcamentoDocument();
            document.setId(orcamento.getId());
            document.setOrdemServicoId("os");
            document.setClienteId("cli");
            document.setValorTotal(BigDecimal.ZERO);
            document.setItens(List.of());
            document.setStatus(StatusOrcamento.ENVIADO);
            document.setCreatedAt(Instant.now());
            return Optional.of(document);
        });
        when(orcamentoMongoRepository.findByOrdemServicoId("os")).thenReturn(List.of());

        Orcamento saved = adapter.save(orcamento);
        assertEquals(StatusOrcamento.ENVIADO, saved.getStatus());
        assertTrue(adapter.findById(orcamento.getId()).isPresent());
        assertEquals(0, adapter.findByOrdemServicoId("os").size());
    }

    @Test
    void shouldMapPagamento() {
        PagamentoRepositoryAdapter adapter = new PagamentoRepositoryAdapter(pagamentoMongoRepository);
        Pagamento pagamento = Pagamento.create("orc", "os", BigDecimal.TEN);

        when(pagamentoMongoRepository.save(any(PagamentoDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pagamentoMongoRepository.findById(pagamento.getId())).thenAnswer(invocation -> {
            PagamentoDocument document = new PagamentoDocument();
            document.setId(pagamento.getId());
            document.setOrcamentoId("orc");
            document.setOrdemServicoId("os");
            document.setValor(BigDecimal.TEN);
            document.setStatus(StatusPagamento.PENDENTE);
            document.setCreatedAt(Instant.now());
            return Optional.of(document);
        });
        when(pagamentoMongoRepository.findByOrcamentoId("orc")).thenReturn(Optional.empty());
        when(pagamentoMongoRepository.findByMercadoPagoId("mp")).thenReturn(Optional.empty());
        when(pagamentoMongoRepository.findByOrdemServicoId("os")).thenReturn(Optional.empty());

        assertEquals(StatusPagamento.PENDENTE, adapter.save(pagamento).getStatus());
        assertTrue(adapter.findById(pagamento.getId()).isPresent());
        assertTrue(adapter.findByOrcamentoId("orc").isEmpty());
        assertTrue(adapter.findByMercadoPagoId("mp").isEmpty());
        assertTrue(adapter.findByOrdemServicoId("os").isEmpty());
    }
}
