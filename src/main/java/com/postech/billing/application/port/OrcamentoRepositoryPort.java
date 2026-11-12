package com.postech.billing.application.port;

import com.postech.billing.domain.model.Orcamento;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepositoryPort {
    Orcamento save(Orcamento orcamento);

    Optional<Orcamento> findById(String id);

    List<Orcamento> findByOrdemServicoId(String ordemServicoId);
}
