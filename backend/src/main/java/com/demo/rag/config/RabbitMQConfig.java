package com.demo.rag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * 配置文档向量化任务的消息队列、交换机和绑定关系
 */
@Slf4j
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
        log.info("初始化 RabbitMQ 队列：{}", queueName);
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange vectorizeExchange() {
        log.info("初始化 RabbitMQ 交换机：{}", exchangeName);
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding bindingVectorize(Queue vectorizeQueue, DirectExchange vectorizeExchange) {
        log.info("绑定队列 {} 到交换机 {}，路由键：{}", queueName, exchangeName, routingKey);
        return BindingBuilder.bind(vectorizeQueue).to(vectorizeExchange).with(routingKey);
    }
}
