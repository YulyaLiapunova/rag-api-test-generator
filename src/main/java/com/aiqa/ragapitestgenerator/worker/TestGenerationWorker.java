package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.RAGClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestGenerationWorker {
    private static final Logger logger = LoggerFactory.getLogger(TestGenerationWorker.class);

    private final GitHubClient gitHubClient;
    private final RAGClient ragClient;

    @Autowired
    public TestGenerationWorker(GitHubClient gitHubClient, RAGClient ragClient) {
        this.gitHubClient = gitHubClient;
        this.ragClient = ragClient;
    }

    public void processTestGeneration(@NotNull QueueEvent event) {
        String changes = gitHubClient.getPullRequestChanges(event.getRepository(), event.getPullRequestId());
        String tests = ragClient.generateTests(changes);
        gitHubClient.commitAndCreatePullRequest(event.getRepository(), tests, "Generated Tests");
    }
}
