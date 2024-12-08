package com.aiqa.ragapitestgenerator.queue;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.worker.KnowledgeWorker;
import com.aiqa.ragapitestgenerator.worker.TestGenerationWorker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.KNOWLEDGE_BASE_UPDATE_QUEUE;
import static com.aiqa.ragapitestgenerator.queue.RabbitMQConfig.TEST_GENERATION_QUEUE;

@Component
public class QueueListener {
    private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);

    private final TestGenerationWorker testGenerationWorker;
    private final KnowledgeWorker knowledgeWorker;

    public QueueListener(TestGenerationWorker testGenerationWorker, KnowledgeWorker knowledgeWorker) {
        this.testGenerationWorker = testGenerationWorker;
        this.knowledgeWorker = knowledgeWorker;
    }

    @RabbitListener(queues = TEST_GENERATION_QUEUE)
    public void handleTestGenerationEvent(QueueEvent event) {
        logger.info("Received Test Generation Event: {}", event);
        validateEvent(event);
        try {
            testGenerationWorker.processTestGeneration(event);
        } catch (Exception e) {
            logger.error("Error processing Test Generation Event: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = KNOWLEDGE_BASE_UPDATE_QUEUE)
    public void handleKnowledgeUpdateEvent(QueueEvent event) {
        logger.info("Received Knowledge Update Event: {}", event);
        validateEvent(event);
        try {
            knowledgeWorker.processKnowledgeBaseUpdate(event);
        } catch (Exception e) {
            logger.error("Error processing Knowledge Update Event: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    private void validateEvent(@NotNull QueueEvent event) {
        if (event.getRepository() == null || event.getRepository().isEmpty()) {
            throw new IllegalArgumentException("Repository is missing in the event");
        }
        if (event.getPullRequestId() <= 0) {
            throw new IllegalArgumentException("Invalid Pull Request ID in the event");
        }
    }
}
