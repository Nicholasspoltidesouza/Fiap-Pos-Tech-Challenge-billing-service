package com.postech.billing.domain.model;

import java.math.BigDecimal;

public record OrcamentoItem(
        String descricao,
        String tipo,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {
}
