package com.aiqa.ragapitestgenerator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BasePackageFinder {
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

    private static void collectPackageDirectories(File dir, String currentPkg, List<String> candidates) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // Check if the directory contains further package-like subdirectories
                String packagePath = currentPkg.isEmpty() ? file.getName() : currentPkg + "." + file.getName();
                if (containsJavaFiles(file)) {
                    candidates.add(packagePath);
                } else {
                    collectPackageDirectories(file, packagePath, candidates);
                }
            }
        }
    }

    private static boolean containsJavaFiles(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                return true;
            }
        }
        return false;
    }
}
