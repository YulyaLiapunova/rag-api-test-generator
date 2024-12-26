package com.aiqa.ragapitestgenerator.util;

import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

public class RAGService {
    private ChatService chatSerivce;
    private EmbeddingService embeddingModel;
    private VectorStorageService vectorStorage;

    public RAGService() {
        this.chatSerivce = new ChatService();
        this.embeddingModel = new EmbeddingService();
        this.vectorStorage = new VectorStorageService();
    }

    private List<String> retrieveContext(EmbeddingResponse queryEmbedding) {
        SearchResp context = this.vectorStorage.searchEmbedding(queryEmbedding, 5);
    }

    private Prompt buildTestGenerationPrompt(
            List<String> endpointDescription
//            List<String> examples,
//            List<String> bestPractices
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an assistant for generating Java-based API tests.\n\n");
        sb.append("Endpoint Description:\n").append(endpointDescription).append("\n\n");

//        sb.append("Relevant Example Tests:\n");
//        for (String ex : examples) {
//            sb.append(ex).append("\n\n");
//        }
//
//        sb.append("Best Practices:\n");
//        for (String bp : bestPractices) {
//            sb.append(bp).append("\n\n");
//        }

        sb.append("Generate a well-structured TestNG test class that covers positive, negative, and edge cases. ");
        sb.append("Use recommended patterns from examples and follow best practices.\n\n");
        sb.append("Return only the Java code.\n");

        return new Prompt(sb.toString());
    }

    public ChatResponse generateTests(List<String> input) {
        try {
            EmbeddingResponse queryEmbedding = this.embeddingModel.getEmbedding(input.get(0));
            List<String> context = this.retrieveContext(queryEmbedding);
            Prompt prompt = this.buildTestGenerationPrompt(context);

            return this.chatSerivce.sendRequest(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate tests: " + e.getMessage(), e);
        }
    }
}
