package com.aiqa.ragapitestgenerator.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BasePackageFinder {
    public static String findBasePackageTestPath(File repoDir) throws Exception {
        File srcDir = new File(repoDir, "src/test/java");
        if (!srcDir.exists()) {
            throw new IllegalStateException("Source directory not found: " + srcDir.getAbsolutePath());
        }

        List<String> candidates = new ArrayList<>();
        collectPackageDirectories(srcDir, "", candidates);

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No base package found in: " + srcDir.getAbsolutePath());
        }

        return srcDir.getAbsolutePath() + "/" + candidates.get(0).replace(".", "/");
    }

    public static String findBasePackagePath(File repoDir) throws Exception {
        File srcDir = new File(repoDir, "src/main/java");
        if (!srcDir.exists()) {
            throw new IllegalStateException("Source directory not found: " + srcDir.getAbsolutePath());
        }

        List<String> candidates = new ArrayList<>();
        collectPackageDirectories(srcDir, "", candidates);

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No base package found in: " + srcDir.getAbsolutePath());
        }

        return srcDir.getAbsolutePath() + "/" + candidates.get(0).replace(".", "/");
    }

    protected static void collectPackageDirectories(File dir, String currentPkg, List<String> candidates) {
        boolean hasJavaFiles = false;

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                String packagePath = currentPkg.isEmpty() ? file.getName() : currentPkg + "." + file.getName();
                if (containsJavaFiles(file)) {
                    candidates.add(packagePath);
                } else {
                    collectPackageDirectories(file, packagePath, candidates);
                }
            } else if (file.getName().endsWith(".java")) {
                hasJavaFiles = true; // Если найден .java файл
            }
        }

        if (hasJavaFiles) { // Условие без проверки currentPkg.isEmpty()
            candidates.add(""); // Добавляем корневую директорию как пакет
        }
    }


    protected static boolean containsJavaFiles(File dir) {
        for (File file : dir.listFiles()) {
            System.out.println("Checking file: " + file.getAbsolutePath());
            if (file.isFile() && file.getName().endsWith(".java")) {
                System.out.println("Java file found: " + file.getName());
                return true;
            }
        }
        return false;
    }
}
