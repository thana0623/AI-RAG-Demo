package com.demo.rag.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rag.mq.queue}")
    private String queueName;

    @Value("${rag.mq.exchange}")
    private String exchangeName;

    @Value("${rag.mq.routing-key}")
    private String routingKey;

    @Bean
    public Queue vectorizeQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange vectorizeExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding bindingVectorize(Queue vectorizeQueue, DirectExchange vectorizeExchange) {
        return BindingBuilder.bind(vectorizeQueue).to(vectorizeExchange).with(routingKey);
    }
}