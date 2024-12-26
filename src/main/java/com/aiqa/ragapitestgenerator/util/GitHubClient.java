package com.aiqa.ragapitestgenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class GitHubClient {
    private static final String GITHUB_ACCESS_TOKEN = "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";
    private static final String REPO_FOLDER = "repositories";
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private GitHub gitHub;

    public boolean connect(String token) {
        try {
            this.gitHub = GitHub.connectUsingOAuth(token);
            if (gitHub.isCredentialValid()) {
                logger.info("Successfully connected to GitHub!");
                return true;
            } else {
                logger.error("Invalid GitHub credentials!");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to connect to GitHub: {}", e.getMessage(), e);
            return false;
        }
    }

    public Git cloneOrUpdateRepository(String repoUrl, String localPath) {
        File repoDir = new File(localPath);

        try {
            if (repoDir.exists() && new File(repoDir, ".git").exists()) {
                logger.info("Repository already exists. Pulling latest changes...");
                Git git = Git.open(repoDir);
                git.pull()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_ACCESS_TOKEN, ""))
                        .call();
                return git;
            } else {
                logger.info("Cloning repository...");
                return Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(repoDir)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_ACCESS_TOKEN, ""))
                        .call();
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Error handling repository: {}", e.getMessage(), e);
        }
        return null;
    }

    private GHPullRequest getMetaDataOfPullRequest(String repository, int pullRequestId) {
        if(connect(GITHUB_ACCESS_TOKEN)) {
            try {
                return this.gitHub.getRepository(repository).getPullRequest(pullRequestId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get repository: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public List<String> getPullRequestChanges(String repositoryUrl, String repositoryName, int pullRequestId) throws Exception {
        GHPullRequest pullRequest = getMetaDataOfPullRequest(repositoryName, pullRequestId);
        String baseBranch = pullRequest.getBase().getRef();
        String headBranch = pullRequest.getHead().getRef();

        logger.info(baseBranch);
        logger.info(headBranch);

        String repoName = repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1).replace(".git", "");
        String pwd = System.getenv("PWD");
        String localRepositoryPath = pwd + "/" + REPO_FOLDER + "/" + repoName;

        Git git = cloneOrUpdateRepository(repositoryUrl, localRepositoryPath);

        git.checkout().setName(baseBranch).call();
        RevCommit baseCommit = getLatestCommit(git);

        git.checkout().setName(headBranch).call();
        RevCommit headCommit = getLatestCommit(git);

        List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(git, baseCommit))
                .setNewTree(prepareTreeParser(git, headCommit))
                .call();

        List<String> changes = new ArrayList<String>();

        for (DiffEntry entry : diffs) {
            logger.info("File: {}", entry.getNewPath());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(out)) {
                formatter.setRepository(git.getRepository());
                formatter.format(entry);
                changes.add(out.toString());
            }
        }

        return changes;
    }

    private static AbstractTreeIterator prepareTreeParser(Git git, RevCommit commit) throws Exception {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return new CanonicalTreeParser(null, git.getRepository().newObjectReader(), walk.parseTree(commit.getTree()));
        }
    }

    private static RevCommit getLatestCommit(Git git) throws Exception {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return walk.parseCommit(git.getRepository().resolve("HEAD"));
        }
    }

    public String getMergedChanges(String repository, int pullRequestId) {
        return "Fetched merged changes for PR #" + pullRequestId;
    }

    public void commitAndCreatePullRequest(String repository, String content, String message) {
        String repoName = repository.substring(repository.lastIndexOf("/") + 1).replace(".git", "");
        String pwd = System.getenv("PWD");
        String localRepositoryPath = pwd + "/" + REPO_FOLDER + "/" + repoName;
        String branchName = UUID.randomUUID().toString();

        try {
            Git git = cloneOrUpdateRepository(repository, localRepositoryPath);

            git.checkout().setCreateBranch(true).setName(branchName).call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).call();
            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_ACCESS_TOKEN, ""))
                    .setRefSpecs(new RefSpec("refs/heads/" + branchName))
                    .call();

            logger.info("Changes pushed to branch: {}", branchName);

            createPullRequest(repoName, message, branchName);

        } catch (GitAPIException e) {
            logger.error("Error during commit and PR creation: {}", e.getMessage(), e);
        }
    }

    public void createPullRequest(String repoName, String message, String branchName) {
        if(connect(GITHUB_ACCESS_TOKEN)) {
            try {
                GHRepository repository = this.gitHub.getRepository(repoName);

                GHPullRequest pullRequest = repository.createPullRequest(
                        "pullRequestTitle", // pull request name
                        branchName,
                        "main", // default branch
                        "" // description
                );
                logger.info("Pull Request created: {}", pullRequest.getHtmlUrl());
            } catch (Exception e) {
                throw new RuntimeException("Failed to get repository: " + e.getMessage(), e);
            }
        }

    }
}
