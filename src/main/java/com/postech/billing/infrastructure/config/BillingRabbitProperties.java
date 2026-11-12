package com.postech.billing.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing.rabbitmq")
public class BillingRabbitProperties {

    private String exchange = "oficina.events";
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Queues getQueues() {
        return queues;
    }

    public void setQueues(Queues queues) {
        this.queues = queues;
    }

    public RoutingKeys getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(RoutingKeys routingKeys) {
        this.routingKeys = routingKeys;
    }

    public static class Queues {
        private String orcamentoGerar = "billing.orcamento.gerar";
        private String pagamentoSolicitar = "billing.pagamento.solicitar";
        private String compensarOrcamento = "billing.compensar.orcamento";
        private String compensarPagamento = "billing.compensar.pagamento";

        public String getOrcamentoGerar() {
            return orcamentoGerar;
        }

        public void setOrcamentoGerar(String orcamentoGerar) {
            this.orcamentoGerar = orcamentoGerar;
        }

        public String getPagamentoSolicitar() {
            return pagamentoSolicitar;
        }

        public void setPagamentoSolicitar(String pagamentoSolicitar) {
            this.pagamentoSolicitar = pagamentoSolicitar;
        }

        public String getCompensarOrcamento() {
            return compensarOrcamento;
        }

        public void setCompensarOrcamento(String compensarOrcamento) {
            this.compensarOrcamento = compensarOrcamento;
        }

        public String getCompensarPagamento() {
            return compensarPagamento;
        }

        public void setCompensarPagamento(String compensarPagamento) {
            this.compensarPagamento = compensarPagamento;
        }
    }

    public static class RoutingKeys {
        private String orcamentoGerar = "orcamento.gerar";
        private String pagamentoSolicitar = "pagamento.solicitar";
        private String compensarOrcamento = "compensar.orcamento";
        private String compensarPagamento = "compensar.pagamento";
        private String orcamentoGerado = "orcamento.gerado";
        private String pagamentoAprovado = "pagamento.aprovado";
        private String pagamentoFalhou = "pagamento.falhou";

        public String getOrcamentoGerar() {
            return orcamentoGerar;
        }

        public void setOrcamentoGerar(String orcamentoGerar) {
            this.orcamentoGerar = orcamentoGerar;
        }

        public String getPagamentoSolicitar() {
            return pagamentoSolicitar;
        }

        public void setPagamentoSolicitar(String pagamentoSolicitar) {
            this.pagamentoSolicitar = pagamentoSolicitar;
        }

        public String getCompensarOrcamento() {
            return compensarOrcamento;
        }

        public void setCompensarOrcamento(String compensarOrcamento) {
            this.compensarOrcamento = compensarOrcamento;
        }

        public String getCompensarPagamento() {
            return compensarPagamento;
        }

        public void setCompensarPagamento(String compensarPagamento) {
            this.compensarPagamento = compensarPagamento;
        }

        public String getOrcamentoGerado() {
            return orcamentoGerado;
        }

        public void setOrcamentoGerado(String orcamentoGerado) {
            this.orcamentoGerado = orcamentoGerado;
        }

        public String getPagamentoAprovado() {
            return pagamentoAprovado;
        }

        public void setPagamentoAprovado(String pagamentoAprovado) {
            this.pagamentoAprovado = pagamentoAprovado;
        }

        public String getPagamentoFalhou() {
            return pagamentoFalhou;
        }

        public void setPagamentoFalhou(String pagamentoFalhou) {
            this.pagamentoFalhou = pagamentoFalhou;
        }
    }
}
