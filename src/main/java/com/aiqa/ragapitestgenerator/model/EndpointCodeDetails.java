package com.aiqa.ragapitestgenerator.model;

import com.aiqa.ragapitestgenerator.util.TypeDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class EndpointCodeDetails {
    private String className;
    private List<MethodDeclaration> endpointMethods;
    private Map<String, Map<String, Object>> models;

    public String serializeResults() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("className", className);

        List<Map<String, Object>> methods = new ArrayList<>();
        for (MethodDeclaration method : endpointMethods) {
            methods.add(TypeDescriptor.describeMethod(method));
        }

        result.put("endpointMethods", methods);
        result.put("models", models);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }
}
