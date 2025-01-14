package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.service.GitHubService;
import com.aiqa.ragapitestgenerator.service.RAGService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestGenerationWorker {
    private final GitHubService gitHubService;
    private final RAGService ragService;

    @Autowired
    public TestGenerationWorker(GitHubService gitHubService, RAGService ragClient) {
        this.gitHubService = gitHubService;
        this.ragService = ragClient;
    }

    public void processTestGeneration(@NotNull QueueEvent event) throws Exception {
        List<EndpointCodeDetails> changedEndpoints = gitHubService.analyzePullRequestChanges(
                event.getRepositoryUrl(),
                event.getRepositoryName(),
                event.getPullRequestId()
        );

        Map<String, String> mappedClassToContent = new HashMap<>();
        for (EndpointCodeDetails endpoint : changedEndpoints) {
            String generateTests = ragService.generateTests(endpoint);
            mappedClassToContent.put(endpoint.getClassName(), generateTests);
        }

        gitHubService.createPullRequestWithChanges(event.getRepositoryUrl(), event.getRepositoryName(), event.getPullRequestId(), mappedClassToContent);
    }
}
