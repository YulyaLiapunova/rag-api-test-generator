package com.aiqa.ragapitestgenerator;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RAGController {
    private final GitService gitService;
    private final EmbeddingService embeddingService;
    private final TestCaseGeneratorService testCaseGeneratorService;
    private final TestContentService testContentService;
    private final KnowledgeBaseProducer knowledgeBaseProducer;

    public RAGController(GitService gitService,
                         EmbeddingService embeddingService,
                         TestCaseGeneratorService testCaseGeneratorService,
                         TestContentService testContentService,
                         KnowledgeBaseProducer knowledgeBaseProducer) {
        this.gitService = gitService;
        this.embeddingService = embeddingService;
        this.testCaseGeneratorService = testCaseGeneratorService;
        this.testContentService = testContentService;
        this.knowledgeBaseProducer = knowledgeBaseProducer;
    }

    @PostMapping("/generate-tests")
    public String generateTests(@RequestParam String repoPath,
                                @RequestParam String oldCommit,
                                @RequestParam String newCommit) {
        try {
            List<DiffEntry> diffs = gitService.getPullRequestDiffs(repoPath, oldCommit, newCommit);

            for (DiffEntry diff : diffs) {
                String changedCodeSnippet = getChangedCodeSnippet(diff);  // custom logic to extract code

                double[] embedding = embeddingService.getCodeEmbedding(changedCodeSnippet);

                String endpointDetails = getEndpointDetails(diff);
                String testContent = testContentService.generateTestContent(endpointDetails);

                testCaseGeneratorService.generateTestCase("TestEndpoint", endpointDetails, "200");
            }

            return "Tests generated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to generate tests: " + e.getMessage();
        }
    }

    @PostMapping("/process-pr")
    public String processPullRequest(@RequestParam String repoPath, @RequestParam String prDetails) {
        String message = "Processing PR from " + repoPath + " with details: " + prDetails;
        System.out.println(message);

        knowledgeBaseProducer.sendUpdateMessage(message);

        return "Pull request processed and update queued for knowledge base.";
    }
}
