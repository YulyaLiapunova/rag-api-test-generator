package com.aiqa.ragapitestgenerator;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public KnowledgeBaseProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUpdateMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.KB_UPDATE_QUEUE, message);
    }
}
