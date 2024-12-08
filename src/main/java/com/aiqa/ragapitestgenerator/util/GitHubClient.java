package com.aiqa.ragapitestgenerator.util;

import org.springframework.stereotype.Component;

@Component
public class GitHubClient {
    public String getPullRequestChanges(String repository, int pullRequestId) {
        return "Fetched changes for PR #" + pullRequestId;
    }

    public String getMergedChanges(String repository, int pullRequestId) {
        return "Fetched merged changes for PR #" + pullRequestId;
    }

    public void commitAndCreatePullRequest(String repository, String content, String message) {
        // Use GitHub API to commit changes and create a PR
    }
}
