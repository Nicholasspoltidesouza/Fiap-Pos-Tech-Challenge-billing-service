package com.postech.billing.application.port;

import com.postech.billing.application.dto.DomainEvent;

public interface EventPublisherPort {
    void publish(String routingKey, DomainEvent event);
}
