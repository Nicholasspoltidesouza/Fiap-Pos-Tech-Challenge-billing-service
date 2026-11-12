package com.postech.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Pagamento {

    private String id;
    private String orcamentoId;
    private String ordemServicoId;
    private BigDecimal valor;
    private StatusPagamento status;
    private String mercadoPagoId;
    private String checkoutUrl;
    private Instant createdAt;

    public static Pagamento create(String orcamentoId, String ordemServicoId, BigDecimal valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.id = UUID.randomUUID().toString();
        pagamento.orcamentoId = orcamentoId;
        pagamento.ordemServicoId = ordemServicoId;
        pagamento.valor = valor;
        pagamento.status = StatusPagamento.PENDENTE;
        pagamento.createdAt = Instant.now();
        return pagamento;
    }

    public void attachCheckout(String mercadoPagoId, String checkoutUrl) {
        this.mercadoPagoId = mercadoPagoId;
        this.checkoutUrl = checkoutUrl;
    }

    public void aprovar(String mercadoPagoId) {
        ensureActive();
        this.mercadoPagoId = mercadoPagoId;
        this.status = StatusPagamento.APROVADO;
    }

    public void recusar() {
        ensureActive();
        this.status = StatusPagamento.RECUSADO;
    }

    public void cancelar() {
        this.status = StatusPagamento.CANCELADO;
    }

    public void reembolsar() {
        this.status = StatusPagamento.REEMBOLSADO;
    }

    private void ensureActive() {
        if (this.status == StatusPagamento.CANCELADO || this.status == StatusPagamento.REEMBOLSADO) {
            throw new IllegalStateException("Payment is not active");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrcamentoId() {
        return orcamentoId;
    }

    public void setOrcamentoId(String orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public String getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(String ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public String getMercadoPagoId() {
        return mercadoPagoId;
    }

    public void setMercadoPagoId(String mercadoPagoId) {
        this.mercadoPagoId = mercadoPagoId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
