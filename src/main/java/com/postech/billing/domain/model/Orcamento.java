package com.postech.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Orcamento {

    private String id;
    private String ordemServicoId;
    private String clienteId;
    private BigDecimal valorTotal;
    private List<OrcamentoItem> itens;
    private StatusOrcamento status;
    private Instant createdAt;

    public Orcamento() {
        this.itens = new ArrayList<>();
    }

    public static Orcamento create(String ordemServicoId, String clienteId, List<OrcamentoItem> itens) {
        Orcamento orcamento = new Orcamento();
        orcamento.id = UUID.randomUUID().toString();
        orcamento.ordemServicoId = ordemServicoId;
        orcamento.clienteId = clienteId;
        orcamento.itens = itens != null ? new ArrayList<>(itens) : new ArrayList<>();
        orcamento.valorTotal = orcamento.itens.stream()
                .map(OrcamentoItem::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orcamento.status = StatusOrcamento.PENDENTE;
        orcamento.createdAt = Instant.now();
        return orcamento;
    }

    public void markAsEnviado() {
        ensureNotCancelled();
        this.status = StatusOrcamento.ENVIADO;
    }

    public void aprovar() {
        ensureNotCancelled();
        this.status = StatusOrcamento.APROVADO;
    }

    public void rejeitar() {
        ensureNotCancelled();
        this.status = StatusOrcamento.REJEITADO;
    }

    public void cancelar() {
        this.status = StatusOrcamento.CANCELADO;
    }

    private void ensureNotCancelled() {
        if (this.status == StatusOrcamento.CANCELADO) {
            throw new IllegalStateException("Budget already cancelled");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(String ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<OrcamentoItem> getItens() {
        return itens;
    }

    public void setItens(List<OrcamentoItem> itens) {
        this.itens = itens;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
