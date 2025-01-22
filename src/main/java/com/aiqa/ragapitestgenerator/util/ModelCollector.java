package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelCollector {
    private final String modelsPath;

    public ModelCollector(String modelsPath) {
        this.modelsPath = modelsPath;
    }

    public Map<String, Map<String, Object>> collectModels() {
        Map<String, Map<String, Object>> models = new HashMap<>();
        File modelDir = new File(modelsPath);

        if (!modelDir.exists() || !modelDir.isDirectory()) {
            throw new IllegalStateException("Model directory not found: " + modelsPath);
        }

        // Сортируем файлы для обработки родительских классов до дочерних
        List<File> files = Arrays.asList(modelDir.listFiles((dir, name) -> name.endsWith(".java")));
        files.sort((f1, f2) -> {
            try {
                ClassOrInterfaceDeclaration class1 = StaticJavaParser.parse(f1)
                        .getClassByName(f1.getName().replace(".java", "")).orElse(null);
                ClassOrInterfaceDeclaration class2 = StaticJavaParser.parse(f2)
                        .getClassByName(f2.getName().replace(".java", "")).orElse(null);

                    boolean isParentOfClass2 = class2.getExtendedTypes().stream()
                            .anyMatch(t -> t.getNameAsString().equals(class1.getNameAsString()));
                    return isParentOfClass2 ? -1 : 1;
            } catch (Exception e) {
                System.err.println("Failed to parse file for sorting: " + e.getMessage());
            }
            return 0;
        });

        // Обрабатываем файлы
        for (File file : files) {
            try {
                ClassOrInterfaceDeclaration classDecl = StaticJavaParser.parse(file)
                        .getClassByName(file.getName().replace(".java", ""))
                        .orElse(null);

                if (classDecl != null) {
                    String typeName = classDecl.getNameAsString();
                    models.put(typeName, describeClass(classDecl, models));
                }
            } catch (Exception e) {
                System.err.println("Failed to parse model: " + file.getName() + " - " + e.getMessage());
            }
        }

        return models;
    }

    protected Map<String, Object> describeClass(ClassOrInterfaceDeclaration classDecl, Map<String, Map<String, Object>> models) {
        Map<String, Object> classDetails = new HashMap<>();
        classDetails.put("className", classDecl.getNameAsString());
        classDetails.put("annotations", classDecl.getAnnotations().toString());

        Map<String, Object> fields = new HashMap<>();
        for (FieldDeclaration field : classDecl.getFields()) {
            field.getVariables().forEach(var -> {
                String fieldName = var.getNameAsString();
                String fieldType = var.getType().asString();
                fields.put(fieldName, fieldType);
            });
        }

        if (classDecl.getExtendedTypes().isNonEmpty()) {
            String parentType = classDecl.getExtendedTypes(0).getNameAsString();
            classDetails.put("extends", parentType);

            if (models.containsKey(parentType)) {
                Map<String, Object> parentDetails = models.get(parentType);
                Map<String, Object> parentFields = (Map<String, Object>) parentDetails.get("fields");
                fields.putAll(parentFields);
            }
        }

        classDetails.put("fields", fields);
        return classDetails;
    }
}
