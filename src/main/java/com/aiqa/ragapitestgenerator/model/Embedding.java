package com.aiqa.ragapitestgenerator.model;

import java.util.List;
import java.util.Map;

public class Embedding {
    private String id;
    private List<Float> vector;
    private Map<String, String> metadata;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Embedding{" +
                "id='" + id + '\'' +
                ", vector=" + vector +
                ", metadata=" + metadata +
                '}';
    }
}
