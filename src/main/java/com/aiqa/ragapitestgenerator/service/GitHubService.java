package com.aiqa.ragapitestgenerator.service;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.aiqa.ragapitestgenerator.util.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GitHubService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private static final String REMOTE_NAME = "origin";
    private static final String DEFAULT_BRANCH = "main";

    @Value("${github.access.token}")
    private String githubAccessToken;

    @Value("${repository.base.path}")
    private String repositoryBasePath;

    private final GitHub gitHub;
    private final GitCredentialsProvider credentialsProvider;

    public GitHubService(@Value("${github.access.token}") String githubAccessToken) throws IOException {
        this.githubAccessToken = githubAccessToken;
        this.gitHub = GitHub.connectUsingOAuth(githubAccessToken);
        this.credentialsProvider = new GitCredentialsProvider(githubAccessToken);
        validateConnection();
    }

    private void validateConnection() throws IOException {
        if (!gitHub.isCredentialValid()) {
            throw new IOException("Invalid GitHub credentials");
        }
        logger.info("Successfully connected to GitHub");
    }

    public List<EndpointCodeDetails> analyzePullRequestChanges(String repositoryUrl, String repositoryName, int pullRequestId) {
        try {
            GHPullRequest pullRequest = getPullRequest(repositoryName, pullRequestId);
            String baseBranch = pullRequest.getBase().getRef();
            String headBranch = pullRequest.getHead().getRef();

            Git git = getRepository(repositoryUrl, repositoryName);
            return analyzeChanges(git, baseBranch, headBranch);
        } catch (Exception e) {
            logger.error("Failed to analyze pull request changes", e);
            throw new GitHubOperationException("Failed to analyze pull request changes", e);
        }
    }

    private Git getRepository(String repositoryUrl, String repositoryName) throws GitAPIException, IOException {
        Path localPath = getLocalRepositoryPath(repositoryName);
        return cloneOrUpdateRepository(repositoryUrl, localPath.toString());
    }

    private Path getLocalRepositoryPath(String repositoryName) {
        String repoName = extractRepositoryName(repositoryName);
        return Paths.get(repositoryBasePath, repoName);
    }

    private String extractRepositoryName(String repositoryUrl) {
        return repositoryUrl.substring(repositoryUrl.lastIndexOf('/') + 1).replace(".git", "");
    }

    private Git cloneOrUpdateRepository(String repoUrl, String localPath) throws GitAPIException, IOException {
        File repoDir = new File(localPath);

        if (isExistingRepository(repoDir)) {
            return updateExistingRepository(repoDir);
        }
        return cloneRepository(repoUrl, repoDir);
    }

    private boolean isExistingRepository(File repoDir) {
        return repoDir.exists() && new File(repoDir, ".git").exists();
    }

    private Git updateExistingRepository(File repoDir) throws GitAPIException, IOException {
        logger.info("Updating existing repository at {}", repoDir);
        Git git = Git.open(repoDir);
        git.fetch()
                .setCredentialsProvider(credentialsProvider)
                .setRemote(REMOTE_NAME)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                .call();
        git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call();
        return git;
    }

    private Git cloneRepository(String repoUrl, File repoDir) throws GitAPIException {
        logger.info("Cloning repository from {} to {}", repoUrl, repoDir);
        return Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(repoDir)
                .setCredentialsProvider(credentialsProvider)
                .call();
    }

    private List<EndpointCodeDetails> analyzeChanges(Git git, String baseBranch, String headBranch) throws Exception {
        checkoutBranches(git, baseBranch, headBranch);
        RevCommit baseCommit = getLatestCommit(git);
        RevCommit headCommit = switchToHeadBranch(git, headBranch);

        List<DiffEntry> diffs = getDiffs(git, baseCommit, headCommit);
        return processChangedFiles(git, diffs);
    }

    private void checkoutBranches(Git git, String baseBranch, String headBranch) throws GitAPIException, IOException {
        git.checkout().setName(baseBranch).call();
        cleanupExistingBranch(git, headBranch);
    }

    private RevCommit switchToHeadBranch(Git git, String headBranch) throws Exception {
        git.checkout()
                .setCreateBranch(true)
                .setName(headBranch)
                .setStartPoint(REMOTE_NAME + "/" + headBranch)
                .call();
        return getLatestCommit(git);
    }

    private void cleanupExistingBranch(Git git, String branchName) throws GitAPIException, IOException {
        boolean branchExists = git.getRepository().getRefDatabase()
                .getRefsByPrefix("refs/heads/")
                .stream()
                .anyMatch(ref -> ref.getName().endsWith(branchName));

        if (branchExists) {
            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();
            logger.info("Deleted existing branch: {}", branchName);
        }
    }

    private List<DiffEntry> getDiffs(Git git, RevCommit baseCommit, RevCommit headCommit) throws Exception {
        return git.diff()
                .setOldTree(prepareTreeParser(git, baseCommit))
                .setNewTree(prepareTreeParser(git, headCommit))
                .call();
    }

    private List<EndpointCodeDetails> processChangedFiles(Git git, List<DiffEntry> diffs) throws Exception {
        List<EndpointCodeDetails> endpoints = new ArrayList<>();
        File repoDir = git.getRepository().getWorkTree();
        String basePackagePath = BasePackageFinder.findBasePackagePath(repoDir);
        Map<String, Map<String, Object>> models = collectModels(basePackagePath);

        for (DiffEntry diff : diffs) {
            if (isControllerFile(diff.getNewPath())) {
                processControllerFile(diff, git, repoDir, models, endpoints);
            }
        }
        return endpoints;
    }

    private Map<String, Map<String, Object>> collectModels(String basePackagePath) {
        String modelsPath = basePackagePath + "/model";
        ModelCollector modelCollector = new ModelCollector(modelsPath);
        return modelCollector.collectModels();
    }

    private boolean isControllerFile(String path) {
        return path.endsWith("Controller.java");
    }

    private void processControllerFile(DiffEntry diff, Git git, File repoDir,
                                       Map<String, Map<String, Object>> models,
                                       List<EndpointCodeDetails> endpoints) throws Exception {
        List<Integer> changedLines = DiffParser.getChangedLines(git.getRepository(), diff);
        File file = new File(repoDir, diff.getNewPath());
        CompilationUnit compilationUnit = StaticJavaParser.parse(file);

        List<MethodDeclaration> endpointMethods = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        if (!endpointMethods.isEmpty()) {
            String className = extractClassName(compilationUnit, diff.getNewPath());
            endpoints.add(createEndpointDetails(className, endpointMethods, models));
        }
    }

    private String extractClassName(CompilationUnit compilationUnit, String fallbackPath) {
        return compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse(fallbackPath);
    }

    private EndpointCodeDetails createEndpointDetails(String className,
                                                      List<MethodDeclaration> methods,
                                                      Map<String, Map<String, Object>> models) {
        EndpointCodeDetails details = new EndpointCodeDetails();
        details.setClassName(className);
        details.setEndpointMethods(methods);
        details.setModels(models);
        return details;
    }

    public void createPullRequestWithChanges(String repositoryUrl, String repositoryName, int pullRequestId, Map<String, String> content) {
        try {
            String branchName = generateBranchName();
            Git git = prepareRepository(repositoryUrl, branchName);

            GHPullRequest pullRequest = getPullRequest(repositoryName, pullRequestId);
            String headBranch = pullRequest.getHead().getRef();

            File repoDir = git.getRepository().getWorkTree();
            String testDirectory = BasePackageFinder.findBasePackageTestPath(repoDir);

            for (var entry : content.entrySet()) {
                String testFileName = entry.getKey() + "ApiTest.java";
                TestFileWriter.writeTestFile(testDirectory, testFileName, entry.getValue());
            }

            String message = "Generated tests";
            commitAndPushChanges(git, message, branchName);
            createPullRequest(repositoryName, message, branchName, headBranch);
        } catch (Exception e) {
            logger.error("Failed to create pull request", e);
            throw new GitHubOperationException("Failed to create pull request", e);
        }
    }

    private String generateBranchName() {
        return "feature-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Git prepareRepository(String repository, String branchName) throws Exception {
        Path localPath = getLocalRepositoryPath(repository);
        Git git = cloneOrUpdateRepository(repository, localPath.toString());
        git.checkout().setCreateBranch(true).setName(branchName).call();
        return git;
    }

    private void commitAndPushChanges(Git git, String message, String branchName) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).call();
        git.push()
                .setRemote(REMOTE_NAME)
                .setCredentialsProvider(credentialsProvider)
                .setRefSpecs(new RefSpec("refs/heads/" + branchName))
                .call();
        logger.info("Changes pushed to branch: {}", branchName);
    }

    private void createPullRequest(String repository, String message, String branchName, String headBranch) throws IOException {
        GHRepository ghRepository = gitHub.getRepository(repository);
        GHPullRequest pullRequest = ghRepository.createPullRequest(
                "TESTS: " + message,
                branchName,
                headBranch,
                "Automated pull request\n\n" + message
        );
        logger.info("Pull Request created: {}", pullRequest.getHtmlUrl());
    }

    private GHPullRequest getPullRequest(String repository, int pullRequestId) throws IOException {
        return gitHub.getRepository(repository).getPullRequest(pullRequestId);
    }

    private static AbstractTreeIterator prepareTreeParser(Git git, RevCommit commit) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return new CanonicalTreeParser(null, git.getRepository().newObjectReader(), walk.parseTree(commit.getTree()));
        }
    }

    private static RevCommit getLatestCommit(Git git) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return walk.parseCommit(git.getRepository().resolve("HEAD"));
        }
    }

    private static class GitCredentialsProvider extends UsernamePasswordCredentialsProvider {
        public GitCredentialsProvider(String token) {
            super(token, "");
        }
    }

    public static class GitHubOperationException extends RuntimeException {
        public GitHubOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
