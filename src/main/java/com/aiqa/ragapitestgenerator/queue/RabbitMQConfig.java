package com.aiqa.ragapitestgenerator.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TEST_GENERATION_QUEUE = "test-generation-queue";
    public static final String KNOWLEDGE_BASE_UPDATE_QUEUE = "knowledge-base-update-queue";

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
    public Queue testGenerationQueue() {
        return new Queue(TEST_GENERATION_QUEUE, true, false, true);
    }

    @Bean
    public Queue knowledgeBaseUpdateQueue() {
        return new Queue(KNOWLEDGE_BASE_UPDATE_QUEUE, true, false, true);
    }
}
