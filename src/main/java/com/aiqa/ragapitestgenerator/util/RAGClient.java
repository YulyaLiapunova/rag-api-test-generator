package com.aiqa.ragapitestgenerator.util;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class RAGClient {
    private final MilvusClient milvusClient;
    private final OllamaEmbeddingModel embeddingModel;

    public RAGClient(MilvusClient milvusClient) { // new OllamaApi()
        this.milvusClient = milvusClient;

        OllamaApi ollamaApi = new OllamaApi();

        this.embeddingModel = new OllamaEmbeddingModel(ollamaApi,
                OllamaOptions.builder().withModel(OllamaModel.MISTRAL.id()).build(), null, null);
    }

    public List<Map<String, Object>> retrieveContext(List<Float> queryEmbedding, int topK) {
        return this.milvusClient.searchEmbedding(queryEmbedding, topK);
    }

    public String buildPrompt(List<String> input, List<Map<String, Object>> context) {
        StringBuilder prompt =
                new StringBuilder("Use the following context to respond to the input:\n\n");

        for (Map<String, Object> item : context) {
            prompt.append("Context Item:\n");
            prompt.append(item.get("metadata")).append("\n\n");
        }

        prompt.append("User Input:\n").append(input).append("\n\n");
        prompt.append("Generate a comprehensive response based on the context above.");

        return prompt.toString();
    }

    public String generateResponse(List<String> input) {
        try {
            List<Float> queryEmbedding = this.embeddingModel.generateEmbedding(input);
            List<Map<String, Object>> context = retrieveContext(queryEmbedding, 5);
            String prompt = buildPrompt(input, context);

            return this.embeddingModel.call(new EmbeddingRequest(List.of(prompt),
                    OllamaOptions.builder().withModel("chroma/all-minilm-l6-v2-f32"))
                            .withTruncate(false).build());;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate response: " + e.getMessage(), e);
        }
    }
}

