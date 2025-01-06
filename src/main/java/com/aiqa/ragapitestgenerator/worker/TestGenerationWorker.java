package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.RAGService;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestGenerationWorker {
    private final GitHubClient gitHubClient;
    private final RAGService ragService;

    @Autowired
    public TestGenerationWorker(GitHubClient gitHubClient, RAGService ragClient) {
        this.gitHubClient = gitHubClient;
        this.ragService = ragClient;
    }

    public void processTestGeneration(@NotNull QueueEvent event) throws Exception {
        List<EndpointCodeDetails> changedEndpoints = gitHubClient.getPullRequestChanges(
                event.getRepositoryUrl(),
                event.getRepositoryName(),
                event.getPullRequestId()
        );

        for (EndpointCodeDetails endpoint : changedEndpoints) {
            ChatResponse tests = ragService.generateTests(endpoint);
        }

        // gitHubClient.commitAndCreatePullRequest(event.getRepositoryUrl(), tests, "Generated Tests");
    }
}
