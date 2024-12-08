package com.aiqa.ragapitestgenerator.controller;

import com.aiqa.ragapitestgenerator.model.PullRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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

    @PostMapping("/pr-created")
    public ResponseEntity<String> handlePullRequest(@RequestBody PullRequestEvent event,
                                                    @RequestHeader("X-Hub-Signature-256") String signature,
                                                    @RequestBody String payload) {
        if (isSignatureValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Processing PR {} with code from {}", event.getPullRequestId(), event.getRepository());

        rabbitTemplate.convertAndSend(TEST_GENERATION_QUEUE, event);
        return ResponseEntity.ok("Event added to queue");
    }

    @PostMapping("/tests-merged")
    public ResponseEntity<String> handlePullRequestMerged(@RequestBody PullRequestEvent event,
                                                          @RequestHeader("X-Hub-Signature-256") String signature,
                                                          @RequestBody String payload) {
        if (isSignatureValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Processing PR {} with tests from {}", event.getPullRequestId(), event.getRepository());

        rabbitTemplate.convertAndSend(KNOWLEDGE_BASE_UPDATE_QUEUE, event);
        return ResponseEntity.ok("Event added to queue");
    }

    private boolean isSignatureValid(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            String secret = "github-webhook-secret";
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            String computedSignature = "sha256=" + Base64.getEncoder().encodeToString(hash);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Error validation request signature: {}", e.getMessage(), e);
            return false;
        }
    }
}
