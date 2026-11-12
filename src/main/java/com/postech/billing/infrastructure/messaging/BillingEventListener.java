package com.postech.billing.infrastructure.messaging;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.service.CompensationService;
import com.postech.billing.application.service.OrcamentoService;
import com.postech.billing.application.service.PagamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BillingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BillingEventListener.class);

    private final OrcamentoService orcamentoService;
    private final PagamentoService pagamentoService;
    private final CompensationService compensationService;

    public BillingEventListener(
            OrcamentoService orcamentoService,
            PagamentoService pagamentoService,
            CompensationService compensationService
    ) {
        this.orcamentoService = orcamentoService;
        this.pagamentoService = pagamentoService;
        this.compensationService = compensationService;
    }

    @RabbitListener(queues = "${billing.rabbitmq.queues.orcamento-gerar}")
    public void onOrcamentoGerar(DomainEvent event) {
        log.info("Received orcamento.gerar eventId={} ordemServicoId={}", event.eventId(), event.ordemServicoId());
        orcamentoService.gerarOrcamento(event);
    }

    @RabbitListener(queues = "${billing.rabbitmq.queues.pagamento-solicitar}")
    public void onPagamentoSolicitar(DomainEvent event) {
        log.info("Received pagamento.solicitar eventId={} ordemServicoId={}", event.eventId(), event.ordemServicoId());
        pagamentoService.solicitarPagamento(event);
    }

    @RabbitListener(queues = "${billing.rabbitmq.queues.compensar-orcamento}")
    public void onCompensarOrcamento(DomainEvent event) {
        log.info("Received compensar.orcamento eventId={} ordemServicoId={}", event.eventId(), event.ordemServicoId());
        compensationService.compensarOrcamento(event);
    }

    @RabbitListener(queues = "${billing.rabbitmq.queues.compensar-pagamento}")
    public void onCompensarPagamento(DomainEvent event) {
        log.info("Received compensar.pagamento eventId={} ordemServicoId={}", event.eventId(), event.ordemServicoId());
        compensationService.compensarPagamento(event);
    }
}
