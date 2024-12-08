package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.MilvusClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class KnowledgeWorker {
    private final GitHubClient gitHubClient;
    private final MilvusClient milvusClient;

    @Autowired
    public KnowledgeWorker(GitHubClient gitHubClient, MilvusClient milvusClient) {
        this.gitHubClient = gitHubClient;
        this.milvusClient = milvusClient;
    }

    public void processKnowledgeBaseUpdate(@NotNull QueueEvent event) {
        String mergedContent = gitHubClient.getMergedChanges(event.getRepository(), event.getPullRequestId());
        List<String> chunks = splitContent(mergedContent);
        for (String chunk : chunks) {
            milvusClient.insertEmbedding(chunk);
        }
    }

    @NotNull
    private List<String> splitContent(@NotNull String content) {
        return Arrays.asList(content.split("\\n\\n"));
    }
}
