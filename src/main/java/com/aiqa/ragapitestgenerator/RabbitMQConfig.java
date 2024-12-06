package com.aiqa.ragapitestgenerator;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String KB_UPDATE_QUEUE = "kb_update_queue";

    @Bean
    public Queue knowledgeBaseUpdateQueue() {
        return new Queue(KB_UPDATE_QUEUE, true); // Durable queue
    }
}
