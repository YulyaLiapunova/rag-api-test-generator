package com.aiqa.ragapitestgenerator.controller;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.model.WebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.KNOWLEDGE_BASE_UPDATE_QUEUE;
import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.TEST_GENERATION_QUEUE;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    public RabbitTemplate rabbitTemplate;

    @Autowired
    public WebhookController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private void validatePayload(WebhookPayload payload) {
        if (payload == null || payload.getRepository() == null || payload.getAction() == null) {
            throw new IllegalArgumentException("Invalid payload received");
        }
        logger.info("Processing PR {} from repository URL {}",
                payload.getNumber(), payload.getRepository().getCloneUrl());
    }

    private QueueEvent buildQueueEvent(WebhookPayload payload) {
        QueueEvent queueEvent = new QueueEvent();
        queueEvent.setType(payload.getAction());
        queueEvent.setPullRequestId(payload.getNumber());
        queueEvent.setRepositoryUrl(payload.getRepository().getCloneUrl());
        queueEvent.setRepositoryName(payload.getRepository().getFullName());
        return queueEvent;
    }

    @PostMapping("/generate-tests")
    public ResponseEntity<String> generateTests(@RequestBody WebhookPayload payload) {
        try {
            validatePayload(payload);
            QueueEvent queueEvent = buildQueueEvent(payload);
            rabbitTemplate.convertAndSend(TEST_GENERATION_QUEUE, queueEvent);

            return ResponseEntity.ok("Event added to queue");
        } catch (Exception e) {
            logger.error("Failed to process webhook payload", e);
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body("Invalid payload");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request");
        }
    }

    @PostMapping("/upload-knowledge-file")
    public ResponseEntity<String> uploadKnowledgeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName,
            @RequestParam("documentType") String documentType
    ) {
        try {
            if (file.isEmpty() || fileName.isEmpty() || documentType.isEmpty()) {
                throw new IllegalArgumentException("Invalid payload received");
            }
            String content = new String(file.getBytes());
            Map<String, Object> message = new HashMap<>();
            message.put("documentType", documentType);
            message.put("fileContent", content);
            message.put("fileName", fileName);
            rabbitTemplate.convertAndSend(KNOWLEDGE_BASE_UPDATE_QUEUE, message);

            return ResponseEntity.ok("File uploaded and processing started.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body("Invalid payload");
            }
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }
}
