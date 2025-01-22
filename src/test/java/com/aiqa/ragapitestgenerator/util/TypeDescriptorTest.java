package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TypeDescriptorTest {
    @Test
    void testDescribeMethod_NoAnnotationsNoParameters() {
        // Arrange
        MethodDeclaration method = new MethodDeclaration();
        method.setName("testMethod");
        method.setType(new VoidType());

        // Act
        Map<String, Object> result = TypeDescriptor.describeMethod(method);

        // Assert
        assertEquals("testMethod", result.get("name"), "Имя метода должно совпадать");
        assertEquals("[]", result.get("annotations"), "Метод не должен иметь аннотаций");
        assertEquals("void", result.get("returnType"), "Возвращаемый тип должен быть void");
        assertTrue(((Map<?, ?>) result.get("parameters")).isEmpty(), "Параметры должны быть пустыми");
    }

    @Test
    void testDescribeMethod_WithParameters() {
        // Arrange
        MethodDeclaration method = new MethodDeclaration();
        method.setName("methodWithParameters");
        method.setType(new ClassOrInterfaceType(null, "String"));
        method.addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.INT), "param1"));
        method.addParameter(new Parameter(new ClassOrInterfaceType(null, "List<String>"), "param2"));

        // Act
        Map<String, Object> result = TypeDescriptor.describeMethod(method);

        // Assert
        assertEquals("methodWithParameters", result.get("name"), "Имя метода должно совпадать");
        assertEquals("String", result.get("returnType"), "Возвращаемый тип должен быть String");

        Map<String, Object> parameters = (Map<String, Object>) result.get("parameters");
        assertEquals(2, parameters.size(), "Метод должен иметь 2 параметра");
        assertEquals("int", parameters.get("param1"), "Тип параметра param1 должен быть int");
        assertEquals("List<String>", parameters.get("param2"), "Тип параметра param2 должен быть List<String>");
    }
}
