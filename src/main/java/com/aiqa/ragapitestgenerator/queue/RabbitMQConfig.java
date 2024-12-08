package com.aiqa.ragapitestgenerator.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TEST_GENERATION_QUEUE = "test_generation_queue";
    public static final String KNOWLEDGE_BASE_UPDATE_QUEUE = "knowledge-base-update-queue";

    @Bean
    public Queue testGenerationQueue() {
        return new Queue(TEST_GENERATION_QUEUE, true);
    }

    @Bean
    public Queue knowledgeBaseUpdateQueue() {
        return new Queue(KNOWLEDGE_BASE_UPDATE_QUEUE, true);
    }
}
