package com.aiqa.ragapitestgenerator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeneratedTest {
    private String testName;
    private String testContent;
    private String targetClass;

    @Override
    public String toString() {
        return "GeneratedTest{" +
                "testName='" + testName + '\'' +
                ", testContent='" + testContent + '\'' +
                ", targetClass='" + targetClass + '\'' +
                '}';
    }
}
