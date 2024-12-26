package com.aiqa.ragapitestgenerator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebhookPayload {
    private String action;
    private int number;
    private PullRequest pullRequest;
    private Repository repository;

    @Setter
    @Getter
    public static class PullRequest {
        private String url;
        private int id;
        private String title;
    }

    @Setter
    @Getter
    public static class Repository {
        private int id;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("clone_url")
        private String cloneUrl;
    }
}
