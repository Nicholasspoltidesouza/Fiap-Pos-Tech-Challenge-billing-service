package com.postech.billing.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PagamentoMongoRepository extends MongoRepository<PagamentoDocument, String> {
    Optional<PagamentoDocument> findByOrcamentoId(String orcamentoId);

    Optional<PagamentoDocument> findByMercadoPagoId(String mercadoPagoId);

    Optional<PagamentoDocument> findByOrdemServicoId(String ordemServicoId);
}
