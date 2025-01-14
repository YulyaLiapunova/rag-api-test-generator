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
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
                List.of(input),
                OllamaOptions.builder()
                        .withModel(OllamaModel.NOMIC_EMBED_TEXT.id())
                        .withTruncate(false)
                        .build()
        );
        return this.embeddingModel.call(embeddingRequest);
    }
}
