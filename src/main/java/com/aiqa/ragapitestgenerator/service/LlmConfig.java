package com.aiqa.ragapitestgenerator.service;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean
    public OllamaChatModel ollamaChatModel() {
        return new OllamaChatModel(new OllamaApi(),
                OllamaOptions.create()
                        .withModel("codestral")
                        .withTemperature(0.9),
                null,
                null,
                ObservationRegistry.create(),
                ModelManagementOptions.builder().build()
        );
    }

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel() {
        return new OllamaEmbeddingModel(
                new OllamaApi(),
                OllamaOptions.builder()
                        .withModel(OllamaModel.NOMIC_EMBED_TEXT.id())
                        .build(),
                ObservationRegistry.create(),
                ModelManagementOptions.builder().build()
        );
    }
}
