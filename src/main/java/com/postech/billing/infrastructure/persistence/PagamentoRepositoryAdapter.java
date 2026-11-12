package com.postech.billing.infrastructure.persistence;

import com.postech.billing.application.port.PagamentoRepositoryPort;
import com.postech.billing.domain.model.Pagamento;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {

    private final PagamentoMongoRepository repository;

    public PagamentoRepositoryAdapter(PagamentoMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        return toDomain(repository.save(toDocument(pagamento)));
    }

    @Override
    public Optional<Pagamento> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Pagamento> findByOrcamentoId(String orcamentoId) {
        return repository.findByOrcamentoId(orcamentoId).map(this::toDomain);
    }

    @Override
    public Optional<Pagamento> findByMercadoPagoId(String mercadoPagoId) {
        return repository.findByMercadoPagoId(mercadoPagoId).map(this::toDomain);
    }

    @Override
    public Optional<Pagamento> findByOrdemServicoId(String ordemServicoId) {
        return repository.findByOrdemServicoId(ordemServicoId).map(this::toDomain);
    }

    private PagamentoDocument toDocument(Pagamento pagamento) {
        PagamentoDocument document = new PagamentoDocument();
        document.setId(pagamento.getId());
        document.setOrcamentoId(pagamento.getOrcamentoId());
        document.setOrdemServicoId(pagamento.getOrdemServicoId());
        document.setValor(pagamento.getValor());
        document.setStatus(pagamento.getStatus());
        document.setMercadoPagoId(pagamento.getMercadoPagoId());
        document.setCheckoutUrl(pagamento.getCheckoutUrl());
        document.setCreatedAt(pagamento.getCreatedAt());
        return document;
    }

    private Pagamento toDomain(PagamentoDocument document) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(document.getId());
        pagamento.setOrcamentoId(document.getOrcamentoId());
        pagamento.setOrdemServicoId(document.getOrdemServicoId());
        pagamento.setValor(document.getValor());
        pagamento.setStatus(document.getStatus());
        pagamento.setMercadoPagoId(document.getMercadoPagoId());
        pagamento.setCheckoutUrl(document.getCheckoutUrl());
        pagamento.setCreatedAt(document.getCreatedAt());
        return pagamento;
    }
}
