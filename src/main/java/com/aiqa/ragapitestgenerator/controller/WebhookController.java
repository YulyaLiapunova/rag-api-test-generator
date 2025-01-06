package com.aiqa.ragapitestgenerator.controller;

import com.aiqa.ragapitestgenerator.model.PullRequestEvent;
import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.model.WebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.KNOWLEDGE_BASE_UPDATE_QUEUE;
import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.TEST_GENERATION_QUEUE;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public WebhookController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/generate-tests")
    public ResponseEntity<String> generateTests(@RequestBody WebhookPayload payload) {
        logger.info("Processing PR {} with code from {}", payload.getNumber(), payload.getRepository().getCloneUrl());
        QueueEvent queueEvent = new QueueEvent();
        queueEvent.setType(payload.getAction());
        queueEvent.setPullRequestId(payload.getNumber());
        queueEvent.setRepositoryUrl(payload.getRepository().getCloneUrl());
        queueEvent.setRepositoryName(payload.getRepository().getFullName());
        rabbitTemplate.convertAndSend(TEST_GENERATION_QUEUE, queueEvent);
        return ResponseEntity.ok("Event added to queue");
    }

    @PostMapping("/update-knowledge-base")
    public ResponseEntity<String> updateKnowledgeBase(@RequestBody PullRequestEvent event) {
        logger.info("Processing PR {} with tests from {}", event.getPullRequestId(), event.getRepository());
        rabbitTemplate.convertAndSend(KNOWLEDGE_BASE_UPDATE_QUEUE, event);
        return ResponseEntity.ok("Event added to queue");
    }
}
