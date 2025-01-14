package com.aiqa.ragapitestgenerator.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final OllamaChatModel chatModel;

    public ChatService(OllamaChatModel ollamaChatModel) {
        this.chatModel = ollamaChatModel;
    }

    public ChatResponse sendRequest(Prompt prompt) {
        return this.chatModel.call(prompt);
    }
}

