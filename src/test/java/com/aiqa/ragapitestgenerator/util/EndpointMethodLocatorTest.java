package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EndpointMethodLocatorTest {
    @Test
    void testFindEndpointMethods_SingleMethodWithAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("testMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("GetMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("testMethod", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_NoAnnotations() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("testMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindEndpointMethods_NoChangedLines() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("testMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("GetMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Collections.emptyList();

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindEndpointMethods_ChangedLinesOutsideRange() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("testMethod");
        method.setRange(new Range(
                new Position(10, 1), // Начальная строка метода
                new Position(20, 1)  // Конечная строка метода
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("GetMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        // Список изменённых строк вне диапазона метода
        List<Integer> changedLines = Arrays.asList(1, 2, 3, 21, 22, 23);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Проверяем, что метод не найден
    }

    @Test
    void testFindEndpointMethods_MultipleMethods() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method1 = new MethodDeclaration();
        method1.setName("methodOne");
        method1.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));
        AnnotationExpr annotation1 = new NormalAnnotationExpr();
        annotation1.setName(new Name("GetMapping"));
        method1.setAnnotations(new NodeList<>(annotation1));

        MethodDeclaration method2 = new MethodDeclaration();
        method2.setName("methodTwo");
        method2.setRange(new Range(
                new Position(25, 1),
                new Position(35, 1)
        ));
        AnnotationExpr annotation2 = new NormalAnnotationExpr();
        annotation2.setName(new Name("PostMapping"));
        method2.setAnnotations(new NodeList<>(annotation2));

        classDeclaration.addMember(method1);
        classDeclaration.addMember(method2);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15, 30);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(2, result.size());
        assertEquals("methodOne", result.get(0).getNameAsString());
        assertEquals("methodTwo", result.get(1).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_MixedAnnotations() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method1 = new MethodDeclaration();
        method1.setName("methodWithAnnotation");
        method1.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));
        AnnotationExpr annotation1 = new NormalAnnotationExpr();
        annotation1.setName(new Name("GetMapping"));
        method1.setAnnotations(new NodeList<>(annotation1));

        MethodDeclaration method2 = new MethodDeclaration();
        method2.setName("methodWithoutAnnotation");
        method2.setRange(new Range(
                new Position(25, 1),
                new Position(35, 1)
        ));

        classDeclaration.addMember(method1);
        classDeclaration.addMember(method2);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15, 30);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("methodWithAnnotation", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_WithRequestMappingAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("requestMappingMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("RequestMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("requestMappingMethod", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_WithPutMappingAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("putMappingMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("PutMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("putMappingMethod", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_WithDeleteMappingAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("deleteMappingMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("DeleteMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("deleteMappingMethod", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_WithPatchMappingAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("patchMappingMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("PatchMapping"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertEquals(1, result.size());
        assertEquals("patchMappingMethod", result.get(0).getNameAsString());
    }

    @Test
    void testFindEndpointMethods_WithNonMatchingAnnotation() {
        // Arrange
        CompilationUnit compilationUnit = new CompilationUnit();

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName("TestClass");

        MethodDeclaration method = new MethodDeclaration();
        method.setName("nonMatchingAnnotationMethod");
        method.setRange(new Range(
                new Position(10, 1),
                new Position(20, 1)
        ));

        // Создаем аннотацию, которая не соответствует условию
        AnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name("CustomAnnotation"));
        method.setAnnotations(new NodeList<>(annotation));

        classDeclaration.addMember(method);
        compilationUnit.addType(classDeclaration);

        List<Integer> changedLines = Arrays.asList(15);

        // Act
        List<MethodDeclaration> result = EndpointMethodLocator.findEndpointMethods(compilationUnit, changedLines);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Метод с неподходящей аннотацией не должен быть найден");
    }
}
