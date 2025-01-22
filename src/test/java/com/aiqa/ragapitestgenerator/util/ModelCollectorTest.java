package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModelCollectorTest {

    @TempDir
    Path tempDir;

    private ModelCollector modelCollector;

    @BeforeEach
    void setUp() {
        modelCollector = new ModelCollector(tempDir.toString());
    }

    @Test
    void testCollectModels_NoModelsDirectory() {
        // Arrange
        Path nonExistentPath = tempDir.resolve("nonExistentModels");
        String modelsPath = nonExistentPath.toString();

        ModelCollector modelCollector = new ModelCollector(modelsPath);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, modelCollector::collectModels);
        assertEquals("Model directory not found: " + modelsPath, exception.getMessage());
    }

    @Test
    void testCollectModels_EmptyModelsDirectory() {
        // Arrange
        File modelDir = tempDir.toFile();
        assertTrue(modelDir.exists() && modelDir.isDirectory());

        // Act
        Map<String, Map<String, Object>> result = modelCollector.collectModels();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCollectModels_WithSingleModel() throws IOException {
        // Arrange
        File modelFile = new File(tempDir.toFile(), "User.java");
        Files.writeString(modelFile.toPath(), """
                public class User {
                    private String name;
                    private int age;
                }
                """);

        // Act
        Map<String, Map<String, Object>> result = modelCollector.collectModels();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("User"));
        Map<String, Object> userDetails = result.get("User");
        assertEquals("User", userDetails.get("className"));
        Map<String, Object> fields = (Map<String, Object>) userDetails.get("fields");
        assertNotNull(fields);
        assertEquals(2, fields.size());
        assertEquals("String", fields.get("name"));
        assertEquals("int", fields.get("age"));
    }

    @Test
    void testCollectModels_WithInheritance() throws IOException {
        // Arrange
        Path modelsDir = tempDir.resolve("models");
        Files.createDirectories(modelsDir);

        Path parentClassFile = modelsDir.resolve("ParentModel.java");
        Files.writeString(parentClassFile, """
        public class ParentModel {
            private String parentField;
        }
    """);

        Path childClassFile = modelsDir.resolve("ChildModel.java");
        Files.writeString(childClassFile, """
        public class ChildModel extends ParentModel {
            private int childField;
        }
    """);

        ModelCollector modelCollector = new ModelCollector(modelsDir.toString());

        // Act
        Map<String, Map<String, Object>> models = modelCollector.collectModels();

        // Assert
        assertEquals(2, models.size(), "Expected two models to be collected.");

        Map<String, Object> parentModel = models.get("ParentModel");
        assertNotNull(parentModel, "ParentModel should be collected.");
        Map<String, Object> parentFields = (Map<String, Object>) parentModel.get("fields");
        assertEquals(1, parentFields.size(), "ParentModel should have one field.");
        assertEquals("String", parentFields.get("parentField"));

        Map<String, Object> childModel = models.get("ChildModel");
        assertNotNull(childModel, "ChildModel should be collected.");
        Map<String, Object> childFields = (Map<String, Object>) childModel.get("fields");
        assertEquals(2, childFields.size(), "ChildModel should inherit fields from ParentModel.");
        assertEquals("String", childFields.get("parentField"));
        assertEquals("int", childFields.get("childField"));
    }

    @Test
    void testCollectModels_ParseError() throws IOException {
        // Arrange
        File invalidFile = new File(tempDir.toFile(), "Invalid.java");
        Files.writeString(invalidFile.toPath(), """
                public class Invalid {
                    // Missing closing brace
                """);

        // Act
        Map<String, Map<String, Object>> result = modelCollector.collectModels();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Invalid file should be skipped
    }

    @Test
    void testCollectModels_EmptyDirectoryWithSubdirectories() throws IOException {
        // Arrange
        File subDir = new File(tempDir.toFile(), "subdir");
        assertTrue(subDir.mkdir());

        // Act
        Map<String, Map<String, Object>> result = modelCollector.collectModels();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Поддиректории не должны обрабатываться
    }

    @Test
    void testCollectModels_BaseClassWithoutFields() throws IOException {
        // Arrange
        File baseClassFile = new File(tempDir.toFile(), "BaseClass.java");
        Files.writeString(baseClassFile.toPath(), """
            public class BaseClass { }
        """);

        File derivedClassFile = new File(tempDir.toFile(), "DerivedClass.java");
        Files.writeString(derivedClassFile.toPath(), """
            public class DerivedClass extends BaseClass {
                private int derivedField;
            }
        """);

        // Act
        Map<String, Map<String, Object>> result = modelCollector.collectModels();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        Map<String, Object> derivedFields = (Map<String, Object>) result.get("DerivedClass").get("fields");
        assertEquals(1, derivedFields.size()); // Только одно поле
        assertEquals("int", derivedFields.get("derivedField"));
    }

    @Test
    void testCollectModels_PathIsNotDirectory() throws IOException {
        // Arrange
        Path filePath = tempDir.resolve("notADirectory.java");
        Files.createFile(filePath); // Создаем файл вместо директории
        ModelCollector collector = new ModelCollector(filePath.toString());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, collector::collectModels);
        assertEquals("Model directory not found: " + filePath.toString(), exception.getMessage());
    }

    @Test
    void testCollectModels_NullClassesDuringSorting() throws IOException {
        // Arrange
        File validModelFile = new File(tempDir.toFile(), "ValidModel.java");
        Files.writeString(validModelFile.toPath(), """
            public class ValidModel {}
            """);

        File invalidModelFile = new File(tempDir.toFile(), "InvalidModel.java");
        Files.writeString(invalidModelFile.toPath(), """
            public class InvalidModel { // Missing closing brace
            """);

        ModelCollector collector = new ModelCollector(tempDir.toString());

        // Act
        Map<String, Map<String, Object>> result = collector.collectModels();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "Only the valid model should be processed.");
        assertTrue(result.containsKey("ValidModel"));
    }

    @Test
    void testCollectModels_NullClassDuringSortingSimulation() throws IOException {
        // Arrange
        File modelFile1 = new File(tempDir.toFile(), "Model1.java");
        Files.writeString(modelFile1.toPath(), """
            public class Model1 {}
            """);

        File modelFile2 = new File(tempDir.toFile(), "Model2.java");
        Files.writeString(modelFile2.toPath(), """
            // This class is intentionally empty to simulate a parsing failure
            """);

        ModelCollector collector = new ModelCollector(tempDir.toString());

        // Act
        Map<String, Map<String, Object>> result = collector.collectModels();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "Only one valid model should be processed.");
        assertTrue(result.containsKey("Model1"), "Model1 should be processed correctly.");
    }

    @Test
    void testCollectModels_BothClassesNotNull() throws IOException {
        // Arrange
        File modelFile1 = new File(tempDir.toFile(), "Model1.java");
        Files.writeString(modelFile1.toPath(), """
            public class Model1 {}
            """);

        File modelFile2 = new File(tempDir.toFile(), "Model2.java");
        Files.writeString(modelFile2.toPath(), """
            public class Model2 {}
            """);

        ModelCollector collector = new ModelCollector(tempDir.toString());

        // Act
        Map<String, Map<String, Object>> result = collector.collectModels();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Expected two models to be collected.");
        assertTrue(result.containsKey("Model1"));
        assertTrue(result.containsKey("Model2"));
    }

    @Test
    void testDescribeClass_WithoutAnnotationsOrInheritance() throws IOException {
        // Arrange
        String modelCode = """
        public class SimpleModel {
            private String field1;
        }
    """;
        ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(modelCode).getClassByName("SimpleModel").orElseThrow();

        Map<String, Map<String, Object>> models = new HashMap<>();

        // Act
        Map<String, Object> result = new ModelCollector("dummyPath").describeClass(classDecl, models);

        // Assert
        assertEquals("SimpleModel", result.get("className"));
        assertEquals("[]", result.get("annotations")); // No annotations
        Map<String, Object> fields = (Map<String, Object>) result.get("fields");
        assertNotNull(fields);
        assertEquals(1, fields.size());
        assertEquals("String", fields.get("field1"));
    }

    @Test
    void testDescribeClass_WithAnnotations() throws IOException {
        // Arrange
        String modelCode = """
        @MyAnnotation
        public class AnnotatedModel {
            private int field1;
        }
    """;
        ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(modelCode).getClassByName("AnnotatedModel").orElseThrow();

        Map<String, Map<String, Object>> models = new HashMap<>();

        // Act
        Map<String, Object> result = new ModelCollector("dummyPath").describeClass(classDecl, models);

        // Assert
        assertEquals("AnnotatedModel", result.get("className"));
        assertTrue(result.get("annotations").toString().contains("MyAnnotation"));
        Map<String, Object> fields = (Map<String, Object>) result.get("fields");
        assertNotNull(fields);
        assertEquals(1, fields.size());
        assertEquals("int", fields.get("field1"));
    }

    @Test
    void testDescribeClass_WithInheritanceAndParentInModels() throws IOException {
        // Arrange
        String parentCode = """
        public class ParentModel {
            private String parentField;
        }
    """;
        String childCode = """
        public class ChildModel extends ParentModel {
            private int childField;
        }
    """;

        ClassOrInterfaceDeclaration parentClass = StaticJavaParser.parse(parentCode).getClassByName("ParentModel").orElseThrow();
        ClassOrInterfaceDeclaration childClass = StaticJavaParser.parse(childCode).getClassByName("ChildModel").orElseThrow();

        Map<String, Map<String, Object>> models = new HashMap<>();
        Map<String, Object> parentDetails = new ModelCollector("dummyPath").describeClass(parentClass, new HashMap<>());
        models.put("ParentModel", parentDetails);

        // Act
        Map<String, Object> result = new ModelCollector("dummyPath").describeClass(childClass, models);

        // Assert
        assertEquals("ChildModel", result.get("className"));
        assertEquals("ParentModel", result.get("extends"));
        Map<String, Object> fields = (Map<String, Object>) result.get("fields");
        assertNotNull(fields);
        assertEquals(2, fields.size());
        assertEquals("String", fields.get("parentField"));
        assertEquals("int", fields.get("childField"));
    }

    @Test
    void testDescribeClass_WithInheritanceAndParentNotInModels() throws IOException {
        // Arrange
        String childCode = """
        public class ChildModel extends ParentModel {
            private int childField;
        }
    """;

        ClassOrInterfaceDeclaration childClass = StaticJavaParser.parse(childCode).getClassByName("ChildModel").orElseThrow();

        Map<String, Map<String, Object>> models = new HashMap<>(); // ParentModel not included

        // Act
        Map<String, Object> result = new ModelCollector("dummyPath").describeClass(childClass, models);

        // Assert
        assertEquals("ChildModel", result.get("className"));
        assertEquals("ParentModel", result.get("extends"));
        Map<String, Object> fields = (Map<String, Object>) result.get("fields");
        assertNotNull(fields);
        assertEquals(1, fields.size());
        assertEquals("int", fields.get("childField"));
    }

    @Test
    void testDescribeClass_ClassWithoutFields() throws IOException {
        // Arrange
        String modelCode = """
        public class EmptyModel {}
    """;
        ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(modelCode).getClassByName("EmptyModel").orElseThrow();

        Map<String, Map<String, Object>> models = new HashMap<>();

        // Act
        Map<String, Object> result = new ModelCollector("dummyPath").describeClass(classDecl, models);

        // Assert
        assertEquals("EmptyModel", result.get("className"));
        assertTrue(((Map<String, Object>) result.get("fields")).isEmpty());
    }

}
