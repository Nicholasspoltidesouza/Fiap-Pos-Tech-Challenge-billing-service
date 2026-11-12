package com.postech.billing.infrastructure.persistence;

import com.postech.billing.application.port.OrcamentoRepositoryPort;
import com.postech.billing.domain.model.Orcamento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrcamentoRepositoryAdapter implements OrcamentoRepositoryPort {

    private final OrcamentoMongoRepository repository;

    public OrcamentoRepositoryAdapter(OrcamentoMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Orcamento save(Orcamento orcamento) {
        return toDomain(repository.save(toDocument(orcamento)));
    }

    @Override
    public Optional<Orcamento> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Orcamento> findByOrdemServicoId(String ordemServicoId) {
        return repository.findByOrdemServicoId(ordemServicoId).stream().map(this::toDomain).toList();
    }

    private OrcamentoDocument toDocument(Orcamento orcamento) {
        OrcamentoDocument document = new OrcamentoDocument();
        document.setId(orcamento.getId());
        document.setOrdemServicoId(orcamento.getOrdemServicoId());
        document.setClienteId(orcamento.getClienteId());
        document.setValorTotal(orcamento.getValorTotal());
        document.setItens(orcamento.getItens());
        document.setStatus(orcamento.getStatus());
        document.setCreatedAt(orcamento.getCreatedAt());
        return document;
    }

    private Orcamento toDomain(OrcamentoDocument document) {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(document.getId());
        orcamento.setOrdemServicoId(document.getOrdemServicoId());
        orcamento.setClienteId(document.getClienteId());
        orcamento.setValorTotal(document.getValorTotal());
        orcamento.setItens(document.getItens());
        orcamento.setStatus(document.getStatus());
        orcamento.setCreatedAt(document.getCreatedAt());
        return orcamento;
    }
}
