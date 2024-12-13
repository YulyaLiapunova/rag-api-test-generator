package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.RAGClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestGenerationWorker {
    private final GitHubClient gitHubClient;
    private final RAGClient ragClient;

    @Autowired
    public TestGenerationWorker(GitHubClient gitHubClient, RAGClient ragClient) {
        this.gitHubClient = gitHubClient;
        this.ragClient = ragClient;
    }

    public void processTestGeneration(@NotNull QueueEvent event) throws Exception {
        List<String> changes = gitHubClient.getPullRequestChanges(event.getRepository(), event.getPullRequestId());
        String tests = ragClient.generateResponse(changes);
        gitHubClient.commitAndCreatePullRequest(event.getRepository(), tests, "Generated Tests");
    }
}
