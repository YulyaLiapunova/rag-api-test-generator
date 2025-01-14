package com.aiqa.ragapitestgenerator.service;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.aiqa.ragapitestgenerator.worker.KnowledgeUpdatingWorker;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    private Map<String, String> retrieveContext(EmbeddingResponse queryEmbedding) throws IOException {
        SearchResp searchResp = this.vectorStorage.searchEmbedding(queryEmbedding, 5);
        Map<String, String> context = new HashMap<>();
        List<SearchResp.SearchResult> searchResult = searchResp.getSearchResults().get(0);
        for (SearchResp.SearchResult item : searchResult) {
            String document = item.getEntity().get("embedding").toString();
            String documentType = item.getEntity().get("document_type").toString();
            if (Objects.equals(documentType, KnowledgeUpdatingWorker.DocumentTypes.TESTS.name())) {
                context.put("endpointDescription", document);
            } else {
                context.put("examples", document);
                context.put("previousTestFileContent", "");
            }
        }

        return context;
    }

    private Prompt buildTestGenerationPrompt(Map<String, String> context, String endpointCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an assistant for generating Java-based API tests. Generate test for endpoint below: \n\n");
        sb.append("Technical description of the new API-endpoint for which you have to generate tests:\n");
        sb.append("[ENDPOINT_DESCRIPTION]").append("\n\n");
        sb.append(context.get("endpointDescription")).append("\n\n");
        sb.append("[/ENDPOINT_DESCRIPTION]").append("\n\n");

        sb.append("Models of endpoints to cover and entities that already exist. Use them if they are needed in the context of describing the endpoint to be covered::\n");
        sb.append("[ENDPOINT_CODE_TO_COVER]").append("\n\n");
        sb.append(endpointCode).append("\n\n");
        sb.append("[/ENDPOINT_CODE_TO_COVER]").append("\n\n");

        if (!context.get("previousTestFileContent").isEmpty()) {
            sb.append("Previous test file content:\n");
            sb.append("[PREVIOUS_TEST_FILE_CONTENT]").append("\n\n");
            sb.append(context.get("previousTestFileContent") + "\n\n");
            sb.append("[/PREVIOUS_TEST_FILE_CONTENT]").append("\n\n");
        } else {
            sb.append("Relevant examples of existing tests that you can use as a template and example when writing new technical documentation tests:\n");
            sb.append("[EXAMPLE_TESTS]").append("\n\n");
            List<String> examples = Collections.singletonList(context.get("examples"));
            for (String ex : examples) {
                sb.append(ex).append("\n\n");
            }
            sb.append("[/EXAMPLE_TESTS]").append("\n\n");
        }

        sb.append("Create a well-structured JUnit test class that covers the positive, negative, and boundary cases for endpoints described in the technical documentation. ");
        sb.append("Use the recommended patterns from the examples, describe existing entity and endpoint models if required, and follow best practices.\n\n");
        sb.append("Return only the Java code placed between [JAVA_CODE] and [/JAVA_CODE] tags.\n");

        return new Prompt(sb.toString());
    }

    public String generateTests(EndpointCodeDetails endpointCodeDetails) {
        try {
            StringBuilder sb = new StringBuilder();
            int filesCount = 0;
            for (MethodDeclaration endpoint : endpointCodeDetails.getEndpointMethods()) {
                String input = "API tests for " + endpoint.getName() + "endpoint in class " + endpointCodeDetails.getClassName();
                EmbeddingResponse queryEmbedding = this.embeddingService.getEmbedding(input);
                Map<String, String> context = this.retrieveContext(queryEmbedding);
                Prompt prompt = this.buildTestGenerationPrompt(context, endpointCodeDetails.serializeResults());
                ChatResponse response = this.chatService.sendRequest(prompt);
                sb.append(response.getResult().getOutput().getContent() + "\n");
                filesCount++;
            }

            String tests;
            if (filesCount > 1) {
                ChatResponse response = this.chatService.sendRequest(new Prompt("Collect tests to one file and placed between [JAVA_CODE] and [/JAVA_CODE] tags: \n" + sb.toString()));
                tests = response.getResult().getOutput().getContent();
            } else {
                tests = sb.toString();
            }

            int startIndex = tests.indexOf("[JAVA_CODE]") + "[JAVA_CODE]".length();
            int endIndex = tests.indexOf("[/JAVA_CODE]", startIndex);

            if (startIndex != -1 && endIndex != -1) {
                return tests.substring(startIndex, endIndex).trim();
            } else {
                return tests;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate tests: " + e.getMessage(), e);
        }
    }
}
