package com.aiqa.ragapitestgenerator.model;

public class PullRequestEvent {
    private String repository;
    private String action;
    private int pullRequestId;
    private String branch;

    // Getters and Setters
    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(int pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "PullRequestEvent{" +
                "repository='" + repository + '\'' +
                ", action='" + action + '\'' +
                ", pullRequestId=" + pullRequestId +
                ", branch='" + branch + '\'' +
                '}';
    }
}
