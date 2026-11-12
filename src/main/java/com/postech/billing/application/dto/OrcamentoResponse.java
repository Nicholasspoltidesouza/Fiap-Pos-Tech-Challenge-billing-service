package com.postech.billing.application.dto;

import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.OrcamentoItem;
import com.postech.billing.domain.model.StatusOrcamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrcamentoResponse(
        String id,
        String ordemServicoId,
        String clienteId,
        BigDecimal valorTotal,
        List<OrcamentoItem> itens,
        StatusOrcamento status,
        Instant createdAt
) {
    public static OrcamentoResponse from(Orcamento orcamento) {
        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getOrdemServicoId(),
                orcamento.getClienteId(),
                orcamento.getValorTotal(),
                orcamento.getItens(),
                orcamento.getStatus(),
                orcamento.getCreatedAt()
        );
    }
}
