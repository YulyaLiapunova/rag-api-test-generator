package com.aiqa.ragapitestgenerator.util;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

@Component
public class ChatService {
    private final OllamaApi ollamaApi;
    private final OllamaChatModel chatModel;

    public ChatService() {
        this.ollamaApi = new OllamaApi();
        this.chatModel = new OllamaChatModel(this.ollamaApi,
                OllamaOptions.create()
                        .withModel(OllamaModel.CODELLAMA)
                        .withTemperature(0.9),
                null,
                null,
                ObservationRegistry.create(),
                ModelManagementOptions.builder().build()
        );
    }

    public ChatResponse sendRequest(Prompt prompt) {
        return this.chatModel.call(prompt);
    }
}

