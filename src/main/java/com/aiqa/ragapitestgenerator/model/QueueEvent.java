package com.aiqa.ragapitestgenerator.model;

import java.util.Map;

public class QueueEvent {
    private String type;
    private String repository;
    private int pullRequestId;
    private Map<String, String> additionalData;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public int getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(int pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "QueueEvent{" +
                "type='" + type + '\'' +
                ", repository='" + repository + '\'' +
                ", pullRequestId=" + pullRequestId +
                ", additionalData=" + additionalData +
                '}';
    }
}
