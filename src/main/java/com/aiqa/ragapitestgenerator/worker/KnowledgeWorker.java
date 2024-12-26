package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.MilvusClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
        String mergedContent = gitHubClient.getMergedChanges(event.getRepositoryName(), event.getPullRequestId());
        List<KnowledgeChunk> chunks = splitContent(mergedContent);
        this.milvusClient.insertEmbeddings(chunks);
    }

    @NotNull
    private List<KnowledgeChunk> splitContent(@NotNull String _content) {
        return null;
    }
}
