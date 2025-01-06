package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.List;

public class EndpointMethodLocator {
    public static List<MethodDeclaration> findEndpointMethods(CompilationUnit compilationUnit, List<Integer> changedLines) {
        List<MethodDeclaration> affectedMethods = new ArrayList<>();

        compilationUnit.findAll(MethodDeclaration.class).forEach(method -> {
            int startLine = method.getBegin().map(position -> position.line).orElse(-1);
            int endLine = method.getEnd().map(position -> position.line).orElse(-1);

            boolean isLineChanged = changedLines.stream().anyMatch(line -> line >= startLine && line <= endLine);

            if (isLineChanged && hasEndpointAnnotation(method)) {
                affectedMethods.add(method);
            }
        });

        return affectedMethods;
    }

    private static boolean hasEndpointAnnotation(MethodDeclaration method) {
        for (AnnotationExpr annotation : method.getAnnotations()) {
            String name = annotation.getNameAsString();
            if (name.matches("RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping")) {
                return true;
            }
        }
        return false;
    }
}
