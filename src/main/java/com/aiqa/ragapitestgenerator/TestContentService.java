package com.aiqa.ragapitestgenerator;

import org.springframework.stereotype.Service;

@Service
public class
TestContentService {
    private final LocalLanguageModel languageModel;

    public TestContentService(LocalLanguageModel languageModel) {
        this.languageModel = languageModel;
    }

    public String generateTestContent(String endpointDetails) {
        String prompt = "Generate a JUnit test for the following endpoint: " + endpointDetails;
        return languageModel.generateText(prompt);
    }
}

