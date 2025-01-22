package com.aiqa.ragapitestgenerator.util;

import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class CodePreprocessor {
    public static String preprocessCode(String code) {
        return code.replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("//.*", "")
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining(" "));
    }
}
