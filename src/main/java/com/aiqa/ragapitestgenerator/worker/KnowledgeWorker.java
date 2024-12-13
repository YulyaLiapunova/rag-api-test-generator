package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.util.GitHubClient;
import com.aiqa.ragapitestgenerator.util.MilvusClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

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
        List<List<Float>> chunks = splitContent(mergedContent);
        for (List<Float> chunk : chunks) {
            milvusClient.insertEmbedding("chunk123", chunk, Map.of("", "", "", ""));
        }
    }

    @NotNull
    private List<List<Float>> splitContent(@NotNull String _content) {
        return null;
    }
}
