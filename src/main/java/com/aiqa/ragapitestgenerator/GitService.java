package com.aiqa.ragapitestgenerator;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class GitService {

    public Repository openRepository(String repoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(new File(repoPath + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();
    }

    public List<DiffEntry> getPullRequestDiffs(String repoPath, String oldCommit, String newCommit) throws IOException, GitAPIException {
        try (Repository repository = openRepository(repoPath)) {
            Git git = new Git(repository);
            return git.diff()
                    .setOldTree()
                    .setNewTree()
                    .call();
        }
    }
}

