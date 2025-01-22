package com.aiqa.ragapitestgenerator.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteTestFile_CreatesFileWithContent() throws IOException {
        // Arrange
        String fileName = "testFile.txt";
        String content = "This is a test content.";

        // Act
        TestFileWriter.writeTestFile(tempDir.toString(), fileName, content);

        // Assert
        Path filePath = tempDir.resolve(fileName);
        assertTrue(Files.exists(filePath), "File should be created");
        assertEquals(content, Files.readString(filePath), "File content should match");
    }

    @Test
    void testWriteTestFile_OverwritesExistingFile() throws IOException {
        // Arrange
        String fileName = "testFile.txt";
        String initialContent = "Initial content.";
        String newContent = "New content.";

        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, initialContent);

        // Act
        TestFileWriter.writeTestFile(tempDir.toString(), fileName, newContent);

        // Assert
        assertTrue(Files.exists(filePath), "File should exist");
        assertEquals(newContent, Files.readString(filePath), "File content should be overwritten");
    }

    @Test
    void testWriteTestFile_CreatesDirectoriesIfNotExist() throws IOException {
        // Arrange
        String subDirectory = "subdir";
        String fileName = "testFile.txt";
        String content = "Content for nested file.";

        // Act
        TestFileWriter.writeTestFile(tempDir.resolve(subDirectory).toString(), fileName, content);

        // Assert
        Path filePath = tempDir.resolve(subDirectory).resolve(fileName);
        assertTrue(Files.exists(filePath), "File should be created in subdirectory");
        assertEquals(content, Files.readString(filePath), "File content should match");
    }
}
