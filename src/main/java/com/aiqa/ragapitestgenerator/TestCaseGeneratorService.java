package com.aiqa.ragapitestgenerator;

import com.squareup.javapoet.*;

import org.springframework.stereotype.Service;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class TestCaseGeneratorService {
    public void generateTestCase(String methodName, String endpoint, String expectedResponse) throws IOException {
        MethodSpec testMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(org.junit.jupiter.api.Test.class)
                .addStatement("// TODO: Add HTTP request creation logic here")
                .addStatement("assertEquals($L, response.getStatusCode())", expectedResponse)
                .build();

        TypeSpec testClass = TypeSpec.classBuilder(methodName + "Test")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(testMethod)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.generatedtests", testClass)
                .build();

        javaFile.writeTo(Paths.get("./generated-sources"));
    }
}
