package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.aiqa.ragapitestgenerator.model.QueueEvent;
import com.aiqa.ragapitestgenerator.service.GitHubService;
import com.aiqa.ragapitestgenerator.service.RAGService;
import com.aiqa.ragapitestgenerator.util.TypeDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestGenerationWorkerTest {

    @Mock
    private GitHubService gitHubService;

    @Mock
    private RAGService ragService;

    private TestGenerationWorker testGenerationWorker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testGenerationWorker = new TestGenerationWorker(gitHubService, ragService);
    }

    @Test
    void testProcessTestGeneration() throws Exception {
        // Arrange
        QueueEvent mockEvent = new QueueEvent();
        mockEvent.setRepositoryUrl("https://github.com/test/repo");
        mockEvent.setRepositoryName("repo");
        mockEvent.setPullRequestId(123);

        MethodDeclaration method1 = mock(MethodDeclaration.class);
        MethodDeclaration method2 = mock(MethodDeclaration.class);

        EndpointCodeDetails endpoint1 = new EndpointCodeDetails();
        endpoint1.setClassName("ClassA");
        endpoint1.setEndpointMethods(Arrays.asList(method1, method2));
        endpoint1.setModels(Map.of("ModelA", Map.of("field1", "value1")));

        EndpointCodeDetails endpoint2 = new EndpointCodeDetails();
        endpoint2.setClassName("ClassB");
        endpoint2.setEndpointMethods(Collections.singletonList(method1));
        endpoint2.setModels(Map.of("ModelB", Map.of("field2", "value2")));

        List<EndpointCodeDetails> changedEndpoints = Arrays.asList(endpoint1, endpoint2);

        when(gitHubService.analyzePullRequestChanges("https://github.com/test/repo", "repo", 123))
                .thenReturn(changedEndpoints);

        when(ragService.generateTests(endpoint1)).thenReturn("TestCodeForClassA");
        when(ragService.generateTests(endpoint2)).thenReturn("TestCodeForClassB");

        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        testGenerationWorker.processTestGeneration(mockEvent);

        // Assert
        verify(gitHubService).analyzePullRequestChanges("https://github.com/test/repo", "repo", 123);
        verify(ragService).generateTests(endpoint1);
        verify(ragService).generateTests(endpoint2);

        verify(gitHubService).createPullRequestWithChanges(eq("https://github.com/test/repo"), eq("repo"), eq(123), mapCaptor.capture());

        Map<String, String> capturedMap = mapCaptor.getValue();
        assertEquals(2, capturedMap.size());
        assertEquals("TestCodeForClassA", capturedMap.get("ClassA"));
        assertEquals("TestCodeForClassB", capturedMap.get("ClassB"));
    }

    @Test
    void testProcessTestGeneration_NoEndpoints() throws Exception {
        // Arrange
        QueueEvent mockEvent = new QueueEvent();
        mockEvent.setRepositoryUrl("https://github.com/test/repo");
        mockEvent.setRepositoryName("repo");
        mockEvent.setPullRequestId(123);

        // Возвращаем пустой список
        when(gitHubService.analyzePullRequestChanges("https://github.com/test/repo", "repo", 123))
                .thenReturn(Collections.emptyList());

        // Act
        testGenerationWorker.processTestGeneration(mockEvent);

        // Assert
        verify(gitHubService).analyzePullRequestChanges("https://github.com/test/repo", "repo", 123);
        verifyNoInteractions(ragService); // Убедитесь, что RAGService не вызывается
        verify(gitHubService).createPullRequestWithChanges(
                "https://github.com/test/repo", "repo", 123, new HashMap<>());
    }

}
