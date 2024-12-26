package com.aiqa.ragapitestgenerator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PullRequestEvent {
    private String repository;
    private String action;
    private int pullRequestId;
    private String branch;

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
