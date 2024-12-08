package com.aiqa.ragapitestgenerator.model;

import java.util.Map;

public class KnowledgeChunk {
    private String id;
    private String content;
    private Map<String, String> metadata;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "KnowledgeChunk{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
