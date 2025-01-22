package com.aiqa.ragapitestgenerator.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@UtilityClass
public class TestFileWriter {
    public static void writeTestFile(String directory, String fileName, String content) throws IOException {
        Path path = Path.of(directory, fileName);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
