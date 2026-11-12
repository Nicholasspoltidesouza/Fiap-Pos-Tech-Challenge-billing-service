package com.postech.billing.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    TopicExchange oficinaEventsExchange(BillingRabbitProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    Queue orcamentoGerarQueue(BillingRabbitProperties properties) {
        return new Queue(properties.getQueues().getOrcamentoGerar(), true);
    }

    @Bean
    Queue pagamentoSolicitarQueue(BillingRabbitProperties properties) {
        return new Queue(properties.getQueues().getPagamentoSolicitar(), true);
    }

    @Bean
    Queue compensarOrcamentoQueue(BillingRabbitProperties properties) {
        return new Queue(properties.getQueues().getCompensarOrcamento(), true);
    }

    @Bean
    Queue compensarPagamentoQueue(BillingRabbitProperties properties) {
        return new Queue(properties.getQueues().getCompensarPagamento(), true);
    }

    @Bean
    Binding orcamentoGerarBinding(Queue orcamentoGerarQueue, TopicExchange oficinaEventsExchange, BillingRabbitProperties properties) {
        return BindingBuilder.bind(orcamentoGerarQueue)
                .to(oficinaEventsExchange)
                .with(properties.getRoutingKeys().getOrcamentoGerar());
    }

    @Bean
    Binding pagamentoSolicitarBinding(Queue pagamentoSolicitarQueue, TopicExchange oficinaEventsExchange, BillingRabbitProperties properties) {
        return BindingBuilder.bind(pagamentoSolicitarQueue)
                .to(oficinaEventsExchange)
                .with(properties.getRoutingKeys().getPagamentoSolicitar());
    }

    @Bean
    Binding compensarOrcamentoBinding(Queue compensarOrcamentoQueue, TopicExchange oficinaEventsExchange, BillingRabbitProperties properties) {
        return BindingBuilder.bind(compensarOrcamentoQueue)
                .to(oficinaEventsExchange)
                .with(properties.getRoutingKeys().getCompensarOrcamento());
    }

    @Bean
    Binding compensarPagamentoBinding(Queue compensarPagamentoQueue, TopicExchange oficinaEventsExchange, BillingRabbitProperties properties) {
        return BindingBuilder.bind(compensarPagamentoQueue)
                .to(oficinaEventsExchange)
                .with(properties.getRoutingKeys().getCompensarPagamento());
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        return factory;
    }
}
