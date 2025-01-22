package com.aiqa.ragapitestgenerator.service;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GitHubServiceTests {

    private GitHubService gitHubService;

    @Mock
    private GitHub mockGitHub;

    @Mock
    private GHRepository mockRepository;

    @Mock
    private GHPullRequest mockPullRequest;

    @Mock
    private Git mockGit;

    @Mock
    private Repository mockRepositoryGit;

    @Mock
    private RevWalk mockRevWalk;

    @Mock
    private RevCommit mockRevCommit;

    @BeforeEach
    void setUp() throws IOException {
        try (MockedStatic<GitHub> mockedStatic = mockStatic(GitHub.class)) {
            // Настраиваем замокированный GitHub
            when(mockGitHub.isCredentialValid()).thenReturn(true);
            when(mockGitHub.getRepository(anyString())).thenReturn(mockRepository); // Мокаем getRepository
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest); // Мокаем getPullRequest

            mockedStatic.when(() -> GitHub.connectUsingOAuth(anyString())).thenReturn(mockGitHub);

            // Создаем экземпляр GitHubService
            gitHubService = new GitHubService("dummy-token");
        }
    }

    @Test
    void testValidateConnection_Successful() throws IOException {
        assertDoesNotThrow(() -> gitHubService.validateConnection());
    }

    @Test
    void testValidateConnection_InvalidCredentials() throws IOException {
        // Мокаем статический метод GitHub.connectUsingOAuth
        try (MockedStatic<GitHub> mockedStatic = mockStatic(GitHub.class)) {
            GitHub mockGitHub = mock(GitHub.class);

            // Указываем, что учетные данные недействительны
            when(mockGitHub.isCredentialValid()).thenReturn(false);
            mockedStatic.when(() -> GitHub.connectUsingOAuth(anyString())).thenReturn(mockGitHub);

            // Создаем экземпляр GitHubService (validateConnection вызовется в конструкторе)
            IOException exception = assertThrows(IOException.class, () -> new GitHubService("dummy-token"));

            // Проверяем сообщение об ошибке
            assertEquals("Invalid GitHub credentials", exception.getMessage());
        }
    }

    @Test
    void testExtractRepositoryName_ValidUrl() {
        // Arrange
        String repositoryUrl = "https://github.com/owner/repository.git";

        // Act
        String result = invokeExtractRepositoryName(repositoryUrl);

        // Assert
        assertEquals("repository", result);
    }

    @Test
    void testExtractRepositoryName_WithoutGitExtension() {
        // Arrange
        String repositoryUrl = "https://github.com/owner/repository";

        // Act
        String result = invokeExtractRepositoryName(repositoryUrl);

        // Assert
        assertEquals("repository", result);
    }

    @Test
    void testExtractRepositoryName_InvalidUrl() {
        // Arrange
        String repositoryUrl = "repository.git";

        // Act
        String result = invokeExtractRepositoryName(repositoryUrl);

        // Assert
        assertEquals("repository", result);
    }

    // Вспомогательный метод для вызова приватного метода extractRepositoryName
    private String invokeExtractRepositoryName(String repositoryUrl) {
        try {
            Method method = GitHubService.class.getDeclaredMethod("extractRepositoryName", String.class);
            method.setAccessible(true);
            return (String) method.invoke(gitHubService, repositoryUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke extractRepositoryName", e);
        }
    }

    @Test
    void testIsExistingRepository_ValidRepository() throws Exception {
        // Arrange
        File tempRepoDir = Files.createTempDirectory("repo").toFile();
        File gitDir = new File(tempRepoDir, ".git");
        assertTrue(gitDir.mkdir()); // Создаем папку .git

        // Act
        boolean result = invokeIsExistingRepository(tempRepoDir);

        // Assert
        assertTrue(result);

        // Cleanup
        tempRepoDir.deleteOnExit();
    }

    @Test
    void testIsExistingRepository_NoGitFolder() throws Exception {
        // Arrange
        File tempRepoDir = Files.createTempDirectory("repo").toFile();

        // Act
        boolean result = invokeIsExistingRepository(tempRepoDir);

        // Assert
        assertFalse(result);

        // Cleanup
        tempRepoDir.deleteOnExit();
    }

    @Test
    void testIsExistingRepository_DirectoryDoesNotExist() {
        // Arrange
        File nonExistentDir = new File("nonexistent-directory");

        // Act
        boolean result = invokeIsExistingRepository(nonExistentDir);

        // Assert
        assertFalse(result);
    }

    // Вспомогательный метод для вызова приватного метода isExistingRepository
    private boolean invokeIsExistingRepository(File repoDir) {
        try {
            return (boolean) ReflectionTestUtils.invokeMethod(gitHubService, "isExistingRepository", repoDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke isExistingRepository", e);
        }
    }

    // Вспомогательный метод для вызова приватного метода updateExistingRepository
    private Git invokeUpdateExistingRepository(File repoDir, String token) throws Exception {
        GitHubService gitHubService = spy(new GitHubService(token));

        // Создаем объект GitCredentialsProvider
        CredentialsProvider credentialsProvider = new GitHubService.GitCredentialsProvider(token);

        // Используем ReflectionTestUtils для вызова приватного метода
        return (Git) ReflectionTestUtils.invokeMethod(
                gitHubService,
                "updateExistingRepository",
                repoDir
        );
    }

    @Test
    void testGetPullRequest_Successful() throws IOException {
        // Arrange
        String repositoryName = "test-repo";
        int pullRequestId = 123;

        // Настраиваем моки
        when(mockGitHub.getRepository(repositoryName)).thenReturn(mockRepository);
        when(mockRepository.getPullRequest(pullRequestId)).thenReturn(mockPullRequest);

        // Act
        GHPullRequest result = gitHubService.getPullRequest(repositoryName, pullRequestId);

        // Assert
        assertNotNull(result); // Проверяем, что результат не null
        verify(mockGitHub).getRepository(repositoryName); // Проверяем вызов метода getRepository
        verify(mockRepository).getPullRequest(pullRequestId); // Проверяем вызов метода getPullRequest
    }

    @Test
    void testGetPullRequest_RepositoryNotFound() throws IOException {
        // Arrange
        String repositoryName = "nonexistent-repo";
        int pullRequestId = 123;

        when(mockGitHub.getRepository(repositoryName)).thenThrow(new IOException("Repository not found"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> gitHubService.getPullRequest(repositoryName, pullRequestId));
        assertEquals("Repository not found", exception.getMessage());
    }

}
