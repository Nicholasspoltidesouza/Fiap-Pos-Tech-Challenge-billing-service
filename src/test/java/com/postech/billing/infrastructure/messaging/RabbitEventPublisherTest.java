package com.postech.billing.infrastructure.messaging;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishToExchange() {
        BillingRabbitProperties properties = new BillingRabbitProperties();
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate, properties);
        DomainEvent event = DomainEvent.of("OrcamentoGerado", "os", "cli", Map.of("orcamentoId", "1"));

        publisher.publish("orcamento.gerado", event);

        verify(rabbitTemplate).convertAndSend("oficina.events", "orcamento.gerado", event);
    }
}
