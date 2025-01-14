package com.aiqa.ragapitestgenerator.queue;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.worker.KnowledgeUpdatingWorker;
import com.aiqa.ragapitestgenerator.worker.TestGenerationWorker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.*;

@Component
public class QueueListener {
    private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);
    private final TestGenerationWorker testGenerationWorker;
    private final KnowledgeUpdatingWorker knowledgeWorker;

    public QueueListener(TestGenerationWorker testGenerationWorker, KnowledgeUpdatingWorker knowledgeWorker) {
        this.testGenerationWorker = testGenerationWorker;
        this.knowledgeWorker = knowledgeWorker;
    }

    @RabbitListener(queues = TEST_GENERATION_QUEUE)
    public void handleTestGenerationEvent(QueueEvent event) {
        logger.info("Received Test Generation Event: {}", event.toString());
        try {
            validateEvent(event);
            testGenerationWorker.processTestGeneration(event);
        } catch (Exception e) {
            logger.error("Error processing Test Generation Event: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = KNOWLEDGE_BASE_UPDATE_QUEUE)
    public void handleKnowledgeUpdateEvent(Map<String, Object> event) {
        logger.info("Received Knowledge Update Event: {}", event);
        try {
//            validateEvent(event);
            knowledgeWorker.processKnowledgeBaseUpdate(event);
        } catch (Exception e) {
            logger.error("Error processing Knowledge Update Event: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = TEST_GENERATION_DLQ)
    public void handleTestGenerationDLQ(Message message) {
        String rawMessage = new String(message.getBody());
        System.out.println("Failed Test Generation Event: " + rawMessage);
    }

    @RabbitListener(queues = KNOWLEDGE_BASE_UPDATE_DLQ)
    public void handleKnowledgeBaseUpdateDLQ(Message message) {
        String rawMessage = new String(message.getBody());
        System.out.println("Failed Knowledge Update Event: " + rawMessage);
    }

    private void validateEvent(@NotNull QueueEvent event) {
        if (event.getRepositoryUrl() == null || event.getRepositoryUrl().isEmpty()) {
            throw new IllegalArgumentException("Repository is missing in the event");
        }
        if (event.getPullRequestId() <= 0) {
            throw new IllegalArgumentException("Invalid Pull Request ID in the event");
        }
    }
}
