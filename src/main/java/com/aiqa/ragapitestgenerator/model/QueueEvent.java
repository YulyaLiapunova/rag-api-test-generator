package com.aiqa.ragapitestgenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class QueueEvent {
    private String type;
    private String repositoryUrl;
    private String repositoryName;
    private int pullRequestId;
    private Map<String, String> additionalData;

    @Override
    public String toString() {
        return "QueueEvent{" +
                "type='" + type + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", pullRequestId=" + pullRequestId +
                ", additionalData=" + additionalData +
                '}';
    }
}
