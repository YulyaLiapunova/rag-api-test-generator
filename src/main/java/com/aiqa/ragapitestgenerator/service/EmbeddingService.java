package com.aiqa.ragapitestgenerator.service;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {
    private final OllamaEmbeddingModel embeddingModel;

    public EmbeddingService(OllamaEmbeddingModel ollamaEmbeddingModel) {
        this.embeddingModel = ollamaEmbeddingModel;
    }

    public EmbeddingResponse getEmbedding(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
                List.of(input),
                OllamaOptions.builder()
                        .withModel(OllamaModel.NOMIC_EMBED_TEXT.id())
                        .withTruncate(false)
                        .build()
        );

        try {
            EmbeddingResponse response = this.embeddingModel.call(embeddingRequest);

            if (response == null || response.getResult() == null) {
                throw new RuntimeException("Invalid response from embedding model: null");
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error during embedding model call: " + e.getMessage(), e);
        }
    }
}
