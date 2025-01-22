package com.aiqa.ragapitestgenerator.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.regex.*;

@UtilityClass
public class PatchParser {
    private static final Pattern CHUNK_HEADER_PATTERN = Pattern.compile("@@ -\\d+,\\d+ \\+(\\d+),(\\d+) @@");

    public static List<Integer> parseChangedLines(String patch) {
        List<Integer> changedLines = new ArrayList<>();
        String[] lines = patch.split("\n");
        int currentLine = 0;

        for (String line : lines) {
            Matcher matcher = CHUNK_HEADER_PATTERN.matcher(line);

            if (matcher.find()) {
                int startLine = Integer.parseInt(matcher.group(1));
                currentLine = startLine;
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                changedLines.add(currentLine);
                currentLine++;
            } else if (line.startsWith("-")) {
                changedLines.add(currentLine);
                currentLine++;
            } else {
                currentLine++;
            }
        }
        return changedLines;
    }
}
