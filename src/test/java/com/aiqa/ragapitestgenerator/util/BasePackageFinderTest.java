package com.aiqa.ragapitestgenerator.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasePackageFinderTest {

    @TempDir
    Path tempDir;

    @Test
    void testFindBasePackageTestPath_SourceDirNotFound() {
        // Arrange
        File repoDir = tempDir.toFile();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            BasePackageFinder.findBasePackageTestPath(repoDir);
        });
        assertTrue(exception.getMessage().contains("Source directory not found"));
    }

    @Test
    void testFindBasePackagePath_SourceDirNotFound() {
        // Arrange
        File repoDir = tempDir.toFile();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            BasePackageFinder.findBasePackagePath(repoDir);
        });
        assertTrue(exception.getMessage().contains("Source directory not found"));
    }

    @SneakyThrows
    @Test
    void testFindBasePackageTestPath_NoBasePackageFound() {
        // Arrange
        Path srcTestJava = tempDir.resolve("src/test/java");
        Files.createDirectories(srcTestJava);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            BasePackageFinder.findBasePackageTestPath(tempDir.toFile());
        });
        assertTrue(exception.getMessage().contains("No base package found"));
    }

    @Test
    void testFindBasePackagePath_NoBasePackageFound() throws IOException {
        // Arrange
        Path srcMainJava = tempDir.resolve("src/main/java");
        Files.createDirectories(srcMainJava);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            BasePackageFinder.findBasePackagePath(tempDir.toFile());
        });
        assertTrue(exception.getMessage().contains("No base package found"));
    }

    @SneakyThrows
    @Test
    void testFindBasePackageTestPath_FindsBasePackage() {
        // Arrange
        Path srcTestJava = tempDir.resolve("src/test/java");
        Path packageDir = srcTestJava.resolve("com/example/test");
        Files.createDirectories(packageDir);
        Files.createFile(packageDir.resolve("TestFile.java"));

        // Act
        String basePackage = BasePackageFinder.findBasePackageTestPath(tempDir.toFile());
        System.out.println("Base package found: " + basePackage);

        // Assert
        assertTrue(basePackage.replace("\\", "/").endsWith("src/test/java/com/example/test"));
    }

    @SneakyThrows
    @Test
    void testFindBasePackagePath_FindsBasePackage() {
        // Arrange
        Path srcMainJava = tempDir.resolve("src/main/java");
        Path packageDir = srcMainJava.resolve("com/example/main");
        Files.createDirectories(packageDir);
        Files.createFile(packageDir.resolve("MainFile.java"));

        // Act
        String basePackage = BasePackageFinder.findBasePackagePath(tempDir.toFile());

        // Assert
        assertTrue(basePackage.replace("\\", "/").endsWith("src/main/java/com/example/main"));
    }

    @SneakyThrows
    @Test
    void testFindBasePackageTestPath_NestedPackages() {
        // Arrange
        Path srcTestJava = tempDir.resolve("src/test/java");
        Path packageDir = srcTestJava.resolve("com/example/test/nested");
        Files.createDirectories(packageDir);
        Files.createFile(packageDir.resolve("NestedTestFile.java"));

        // Act
        String basePackage = BasePackageFinder.findBasePackageTestPath(tempDir.toFile());
        System.out.println("Base package found: " + basePackage);

        // Assert
        assertTrue(basePackage.replace("\\", "/").endsWith("src/test/java/com/example/test/nested"));
    }

    @SneakyThrows
    @Test
    void testFindBasePackageTestPath_SinglePackageWithJavaFiles() {
        // Arrange
        Path srcTestJava = tempDir.resolve("src/test/java/com/example");
        Files.createDirectories(srcTestJava); // Создаем структуру пакета
        Files.createFile(srcTestJava.resolve("TestFile.java")); // Создаем .java файл

        // Act
        String basePackage = BasePackageFinder.findBasePackageTestPath(tempDir.toFile());

        // Assert
        assertTrue(basePackage.replace("\\", "/").endsWith("src/test/java/com/example"));
    }

    @Test
    void testContainsJavaFiles() throws IOException {
        // Arrange
        File testDir = tempDir.resolve("testDir").toFile();
        assertTrue(testDir.mkdir());

        File javaFile = new File(testDir, "TestFile.java");
        assertTrue(javaFile.createNewFile());

        // Act
        boolean containsJava = BasePackageFinder.containsJavaFiles(testDir);

        // Assert
        assertTrue(containsJava);
    }

    @Test
    void testContainsJavaFiles_NoJavaFiles() throws IOException {
        // Arrange
        File testDir = tempDir.resolve("testDir").toFile();
        assertTrue(testDir.mkdir());

        File nonJavaFile = new File(testDir, "TestFile.txt");
        assertTrue(nonJavaFile.createNewFile());

        // Act
        boolean containsJava = BasePackageFinder.containsJavaFiles(testDir);

        // Assert
        assertFalse(containsJava);
    }

    @Test
    void testCollectPackageDirectories() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File srcDir = new File(rootDir, "src");
        File pkgDir = new File(srcDir, "com/example");
        assertTrue(pkgDir.mkdirs());
        File javaFile = new File(pkgDir, "Example.java");
        assertTrue(javaFile.createNewFile());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(srcDir, "", candidates);

        // Assert
        assertEquals(1, candidates.size());
        assertEquals("com.example", candidates.get(0));
    }

    @Test
    void testCollectPackageDirectories_EmptyDirectory() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File emptyDir = new File(rootDir, "emptyDir");
        assertTrue(emptyDir.mkdir());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(emptyDir, "", candidates);

        // Assert
        assertTrue(candidates.isEmpty());
    }

    @Test
    void testCollectPackageDirectories_NoJavaFiles() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File srcDir = new File(rootDir, "src");
        File pkgDir = new File(srcDir, "com/example");
        assertTrue(pkgDir.mkdirs());
        File nonJavaFile = new File(pkgDir, "TestFile.txt");
        assertTrue(nonJavaFile.createNewFile());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(srcDir, "", candidates);

        // Assert
        assertTrue(candidates.isEmpty());
    }

    @Test
    void testCollectPackageDirectories_NestedDirectoriesWithJavaFiles() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File srcDir = new File(rootDir, "src");
        File pkgDir1 = new File(srcDir, "com/example");
        File pkgDir2 = new File(pkgDir1, "nested");
        assertTrue(pkgDir2.mkdirs());
        File javaFile = new File(pkgDir2, "NestedFile.java");
        assertTrue(javaFile.createNewFile());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(srcDir, "", candidates);

        // Assert
        assertEquals(1, candidates.size());
        assertEquals("com.example.nested", candidates.get(0));
    }

    @Test
    void testCollectPackageDirectories_RootWithJavaFiles() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File javaFile = new File(rootDir, "RootFile.java");
        assertTrue(javaFile.createNewFile());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(rootDir, "", candidates);

        // Assert
        assertEquals(1, candidates.size());
        assertEquals("", candidates.get(0)); // Корневой пакет
    }

    @Test
    void testCollectPackageDirectories_MultiplePackages() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile();
        File pkgDir1 = new File(rootDir, "com/example");
        File pkgDir2 = new File(rootDir, "org/test");
        assertTrue(pkgDir1.mkdirs());
        assertTrue(pkgDir2.mkdirs());
        File javaFile1 = new File(pkgDir1, "ExampleFile.java");
        File javaFile2 = new File(pkgDir2, "TestFile.java");
        assertTrue(javaFile1.createNewFile());
        assertTrue(javaFile2.createNewFile());

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(rootDir, "", candidates);

        // Assert
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("com.example"));
        assertTrue(candidates.contains("org.test"));
    }

    @Test
    void testCollectPackageDirectories_RootDirectoryWithJavaFilesAndEmptyCurrentPkg() throws IOException {
        // Arrange
        File rootDir = tempDir.toFile(); // Корневая директория
        File javaFile = new File(rootDir, "RootFile.java");
        assertTrue(javaFile.createNewFile()); // Создаём .java файл в корне директории

        List<String> candidates = new ArrayList<>();

        // Act
        BasePackageFinder.collectPackageDirectories(rootDir, "", candidates);

        // Assert
        System.out.println("Candidates: " + candidates);
        assertEquals(1, candidates.size());
        assertEquals("", candidates.get(0)); // Проверяем, что добавлен пустой пакет
    }
}
