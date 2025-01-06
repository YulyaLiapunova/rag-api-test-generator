package com.aiqa.ragapitestgenerator.util;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DiffParser {
    public static List<Integer> getChangedLines(Repository repository, DiffEntry diff) throws Exception {
        List<Integer> changedLines = new ArrayList<>();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(out)) {
            diffFormatter.setRepository(repository);
            diffFormatter.format(diff);

            String diffOutput = out.toString();
            changedLines.addAll(PatchParser.parseChangedLines(diffOutput));
        }
        return changedLines;
    }
}
