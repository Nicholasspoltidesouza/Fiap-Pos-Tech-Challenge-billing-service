package com.postech.billing.application.port;

import com.postech.billing.domain.model.Pagamento;

import java.util.Optional;

public interface PagamentoRepositoryPort {
    Pagamento save(Pagamento pagamento);

    Optional<Pagamento> findById(String id);

    Optional<Pagamento> findByOrcamentoId(String orcamentoId);

    Optional<Pagamento> findByMercadoPagoId(String mercadoPagoId);

    Optional<Pagamento> findByOrdemServicoId(String ordemServicoId);
}
