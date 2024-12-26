package com.aiqa.ragapitestgenerator.util;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;

import java.util.List;

public class EmbeddingService {
    private final OllamaApi ollamaApi;
    private final OllamaEmbeddingModel embeddingModel;

    public EmbeddingService() {
        this.ollamaApi = new OllamaApi();
        this.embeddingModel = new OllamaEmbeddingModel(
                this.ollamaApi,
                OllamaOptions.builder()
                        .withModel(OllamaModel.NOMIC_EMBED_TEXT.id())
                        .build(),
                ObservationRegistry.create(),
                ModelManagementOptions.builder().build()
        );
    }

    public EmbeddingResponse getEmbedding(String input) {
        return this.embeddingModel.call(
                new EmbeddingRequest(
                        List.of(input),
                        OllamaOptions.builder()
                                .withModel(OllamaModel.NOMIC_EMBED_TEXT.id())
                                .withTruncate(false)
                                .build()
                )
        );
    }
}
