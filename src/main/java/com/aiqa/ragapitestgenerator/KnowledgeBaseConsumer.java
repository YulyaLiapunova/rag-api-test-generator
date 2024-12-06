package com.aiqa.ragapitestgenerator;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseConsumer {

    private final MilvusService milvusService;

    public KnowledgeBaseConsumer(MilvusService milvusService) {
        this.milvusService = milvusService;
    }

    @RabbitListener(queues = RabbitMQConfig.KB_UPDATE_QUEUE)
    public void receiveUpdateMessage(String message) {

        System.out.println("Received update message: " + message);

        try {
            String collectionName = "code_knowledge_base";
            List<Float> newVector = generateEmbedding(message); // Convert message into embedding

            milvusService.insertVectors(collectionName, List.of(newVector));
            System.out.println("Knowledge base updated successfully for: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Float> generateEmbedding(String message) {
        return List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
    }
}
