package com.postech.billing.infrastructure.messaging;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.port.EventPublisherPort;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final BillingRabbitProperties properties;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate, BillingRabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(String routingKey, DomainEvent event) {
        log.info("Publishing event type={} routingKey={} ordemServicoId={}",
                event.eventType(), routingKey, event.ordemServicoId());
        rabbitTemplate.convertAndSend(properties.getExchange(), routingKey, event);
    }
}
