package com.aiqa.ragapitestgenerator.util;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RAGService {
    private final ChatService chatService;
    private final EmbeddingService embeddingService;
    private final VectorStorageService vectorStorage;

    public RAGService(
            ChatService chatService,
            EmbeddingService embeddingService,
            VectorStorageService vectorStorageService
    ) {
        this.chatService = chatService;
        this.embeddingService = embeddingService;
        this.vectorStorage = vectorStorageService;
    }

    private List<String> retrieveContext(EmbeddingResponse queryEmbedding) {
        SearchResp context = this.vectorStorage.searchEmbedding(queryEmbedding, 5);
        return new ArrayList<>();
    }

    private Prompt buildTestGenerationPrompt(
            List<String> endpointDescription,
            String endpointCode
//            List<String> bestPractices
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an assistant for generating Java-based API tests.\n\n");
        sb.append("Endpoint Description:\n").append(endpointDescription).append("\n\n");

        sb.append("Endpoint code to cover:\n");
        sb.append(endpointCode).append("\n\n");

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

    public ChatResponse generateTests(EndpointCodeDetails endpointCodeDetails) {
        try {
            EmbeddingResponse queryEmbedding = this.embeddingService.getEmbedding(endpointCodeDetails.getClassName());
            List<String> context = this.retrieveContext(queryEmbedding);
            Prompt prompt = this.buildTestGenerationPrompt(context, endpointCodeDetails.serializeResults());
            return this.chatService.sendRequest(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate tests: " + e.getMessage(), e);
        }
    }
}
