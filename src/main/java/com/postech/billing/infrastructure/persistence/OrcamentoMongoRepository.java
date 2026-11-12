package com.postech.billing.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrcamentoMongoRepository extends MongoRepository<OrcamentoDocument, String> {
    List<OrcamentoDocument> findByOrdemServicoId(String ordemServicoId);
}
