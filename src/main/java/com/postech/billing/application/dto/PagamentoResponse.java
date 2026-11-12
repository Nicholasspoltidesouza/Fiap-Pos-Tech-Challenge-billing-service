package com.postech.billing.application.dto;

import com.postech.billing.domain.model.Pagamento;
import com.postech.billing.domain.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoResponse(
        String id,
        String orcamentoId,
        String ordemServicoId,
        BigDecimal valor,
        StatusPagamento status,
        String mercadoPagoId,
        String checkoutUrl,
        Instant createdAt
) {
    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getOrcamentoId(),
                pagamento.getOrdemServicoId(),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getMercadoPagoId(),
                pagamento.getCheckoutUrl(),
                pagamento.getCreatedAt()
        );
    }
}
