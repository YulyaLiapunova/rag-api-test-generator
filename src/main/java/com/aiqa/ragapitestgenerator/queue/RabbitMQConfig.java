package com.aiqa.ragapitestgenerator.queue;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TEST_GENERATION_QUEUE = "test-generation-queue";
    public static final String KNOWLEDGE_BASE_UPDATE_QUEUE = "knowledge-base-update-queue";
    public static final String TEST_GENERATION_DLQ = "test-generation-dlq";
    public static final String KNOWLEDGE_BASE_UPDATE_DLQ = "knowledge-base-update-dlq";
    public static final String DEAD_LETTER_EXCHANGE = "dead-letter-exchange";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue testGenerationDLQ() {
        return QueueBuilder.durable(TEST_GENERATION_DLQ).build();
    }

    @Bean
    public Queue knowledgeBaseUpdateDLQ() {
        return QueueBuilder.durable(KNOWLEDGE_BASE_UPDATE_DLQ).build();
    }

    @Bean
    public Queue testGenerationQueue() {
        return QueueBuilder.durable(TEST_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TEST_GENERATION_DLQ)
                .build();
    }

    @Bean
    public Queue knowledgeBaseUpdateQueue() {
        return QueueBuilder.durable(KNOWLEDGE_BASE_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", KNOWLEDGE_BASE_UPDATE_DLQ)
                .build();
    }

    @Bean
    public Binding testGenerationDLQBinding(Queue testGenerationDLQ, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(testGenerationDLQ).to(deadLetterExchange).with(TEST_GENERATION_DLQ);
    }

    @Bean
    public Binding knowledgeBaseUpdateDLQBinding(Queue knowledgeBaseUpdateDLQ, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(knowledgeBaseUpdateDLQ).to(deadLetterExchange).with(KNOWLEDGE_BASE_UPDATE_DLQ);
    }
}
