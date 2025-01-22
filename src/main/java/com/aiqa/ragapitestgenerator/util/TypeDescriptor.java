package com.aiqa.ragapitestgenerator.util;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TypeDescriptor {
    public static Map<String, Object> describeMethod(MethodDeclaration method) {
        Map<String, Object> methodDetails = new HashMap<>();

        methodDetails.put("name", method.getNameAsString());
        methodDetails.put("annotations", method.getAnnotations().toString());
        methodDetails.put("returnType", method.getType().asString());

        Map<String, Object> parameters = new HashMap<>();
        for (Parameter param : method.getParameters()) {
            parameters.put(param.getNameAsString(), param.getType().asString());
        }
        methodDetails.put("parameters", parameters);

        return methodDetails;
    }
}
