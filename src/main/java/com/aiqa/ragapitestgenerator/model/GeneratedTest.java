package com.aiqa.ragapitestgenerator.model;

public class GeneratedTest {
    private String testName;
    private String testContent;
    private String targetClass;

    // Getters and Setters
    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestContent() {
        return testContent;
    }

    public void setTestContent(String testContent) {
        this.testContent = testContent;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String toString() {
        return "GeneratedTest{" +
                "testName='" + testName + '\'' +
                ", testContent='" + testContent + '\'' +
                ", targetClass='" + targetClass + '\'' +
                '}';
    }
}
