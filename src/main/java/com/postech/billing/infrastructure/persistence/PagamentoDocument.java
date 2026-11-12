package com.postech.billing.infrastructure.persistence;

import com.postech.billing.domain.model.StatusPagamento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "pagamentos")
public class PagamentoDocument {

    @Id
    private String id;
    private String orcamentoId;
    private String ordemServicoId;
    private BigDecimal valor;
    private StatusPagamento status;
    private String mercadoPagoId;
    private String checkoutUrl;
    private Instant createdAt;

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
