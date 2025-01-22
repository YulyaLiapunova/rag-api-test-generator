package com.aiqa.ragapitestgenerator.service;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.aiqa.ragapitestgenerator.model.EndpointCodeDetails;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.io.IOException;
import java.util.*;

public class RAGServiceTest {

    private RAGService ragService;

    @Mock
    private ChatService chatService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStorageService vectorStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ragService = new RAGService(chatService, embeddingService, vectorStorageService);
    }

    @Test
    void testRetrieveContext_Success() throws IOException {
        // Мокирование данных
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        SearchResp mockSearchResp = mock(SearchResp.class);
        SearchResp.SearchResult searchResult1 = mock(SearchResp.SearchResult.class);
        SearchResp.SearchResult searchResult2 = mock(SearchResp.SearchResult.class);

        Map<String, Object> entity1 = new HashMap<>();
        entity1.put("embedding", "Document 1");
        entity1.put("document_type", "TESTS");
        when(searchResult1.getEntity()).thenReturn(entity1);

        Map<String, Object> entity2 = new HashMap<>();
        entity2.put("embedding", "Document 2");
        entity2.put("document_type", "OTHER");
        when(searchResult2.getEntity()).thenReturn(entity2);

        when(mockSearchResp.getSearchResults()).thenReturn(List.of(List.of(searchResult1, searchResult2)));
        when(vectorStorageService.searchEmbedding(any(), eq(5))).thenReturn(mockSearchResp);

        // Вызов метода
        Map<String, String> context = ragService.retrieveContext(mockEmbeddingResponse);

        // Проверка результата
        assertEquals("Document 1", context.get("endpointDescription"));
        assertEquals("Document 2", context.get("examples"));
        assertEquals("", context.get("previousTestFileContent"));
    }

    @Test
    void testBuildTestGenerationPrompt() {
        // Входные данные
        Map<String, String> context = new HashMap<>();
        context.put("endpointDescription", "Test Endpoint Description");
        context.put("examples", "Test Example");
        context.put("previousTestFileContent", "");

        String endpointCode = "public void testEndpoint() {}";

        // Вызов метода
        Prompt prompt = ragService.buildTestGenerationPrompt(context, endpointCode);

        // Проверка результата
        assertTrue(prompt.getContents().contains("Test Endpoint Description"));
        assertTrue(prompt.getContents().contains("Test Example"));
        assertTrue(prompt.getContents().contains(endpointCode));
    }

    @Test
    void testBuildTestGenerationPrompt_WithPreviousTestFileContent() {
        // Входные данные
        Map<String, String> context = new HashMap<>();
        context.put("endpointDescription", "Test Endpoint Description");
        context.put("examples", "Test Example");
        context.put("previousTestFileContent", "Previous Test Content");

        String endpointCode = "public void testEndpoint() {};";

        // Вызов метода
        Prompt prompt = ragService.buildTestGenerationPrompt(context, endpointCode);

        // Проверка результата
        String promptContent = prompt.getContents();
        assertTrue(promptContent.contains("Test Endpoint Description"));
        assertTrue(promptContent.contains("Previous Test Content"));
        assertTrue(promptContent.contains("public void testEndpoint() {};"));
        assertTrue(promptContent.contains("[PREVIOUS_TEST_FILE_CONTENT]"));
        assertFalse(promptContent.contains("[EXAMPLE_TESTS]"));
    }

    @Test
    void testBuildTestGenerationPrompt_WithoutPreviousTestFileContent() {
        // Входные данные
        Map<String, String> context = new HashMap<>();
        context.put("endpointDescription", "Test Endpoint Description");
        context.put("examples", "Test Example");
        context.put("previousTestFileContent", "");

        String endpointCode = "public void testEndpoint() {};";

        // Вызов метода
        Prompt prompt = ragService.buildTestGenerationPrompt(context, endpointCode);

        // Проверка результата
        String promptContent = prompt.getContents();
        assertTrue(promptContent.contains("Test Endpoint Description"));
        assertTrue(promptContent.contains("Test Example"));
        assertTrue(promptContent.contains("public void testEndpoint() {};"));
        assertTrue(promptContent.contains("[EXAMPLE_TESTS]"));
        assertFalse(promptContent.contains("[PREVIOUS_TEST_FILE_CONTENT]"));
    }

    @SneakyThrows
    @Test
    void testGenerateTests_SingleMethod_Success() {
        // Создание spy для RAGService
        RAGService ragServiceSpy = spy(new RAGService(chatService, embeddingService, vectorStorageService));

        // Мокирование входных данных
        EndpointCodeDetails endpointCodeDetails = mock(EndpointCodeDetails.class);
        MethodDeclaration methodDeclaration = mock(MethodDeclaration.class);
        EmbeddingResponse embeddingResponse = mock(EmbeddingResponse.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation chatResult = mock(Generation.class); // Создаем мок ChatResult
        AssistantMessage chatOutput = mock(AssistantMessage.class); // Создаем мок ChatOutput

        // Настройка моков для EndpointCodeDetails
        when(endpointCodeDetails.getEndpointMethods()).thenReturn(List.of(methodDeclaration));
        when(endpointCodeDetails.getClassName()).thenReturn("TestClass");
        when(endpointCodeDetails.serializeResults()).thenReturn("Serialized Code");

        // Настройка мока для EmbeddingService
        when(embeddingService.getEmbedding(anyString())).thenReturn(embeddingResponse);

        // Настройка мока для метода retrieveContext через spy
        Map<String, String> context = Map.of(
                "endpointDescription", "Description",
                "examples", "Example",
                "previousTestFileContent", ""
        );
        doReturn(context).when(ragServiceSpy).retrieveContext(any(EmbeddingResponse.class));

        // Настройка вложенных моков для ChatService
        when(chatService.sendRequest(any(Prompt.class))).thenReturn(chatResponse); // Возвращаем mock ChatResponse
        when(chatResponse.getResult()).thenReturn(chatResult); // Возвращаем mock ChatResult
        when(chatResult.getOutput()).thenReturn(chatOutput); // Возвращаем mock ChatOutput
        when(chatOutput.getContent()).thenReturn("[JAVA_CODE]public class Test {}[/JAVA_CODE]"); // Возвращаем ожидаемый результат

        // Вызов тестируемого метода
        String result = ragServiceSpy.generateTests(endpointCodeDetails);

        // Проверка результата
        assertEquals("public class Test {}", result);

        // Проверка вызова зависимостей
        verify(embeddingService).getEmbedding(anyString());
        verify(chatService, times(1)).sendRequest(any(Prompt.class));
    }

    @SneakyThrows
    @Test
    void testGenerateTests_MultipleMethods_Success() {
        // Создание spy для RAGService
        RAGService ragServiceSpy = spy(new RAGService(chatService, embeddingService, vectorStorageService));

        // Мокирование данных
        EndpointCodeDetails mockEndpointCodeDetails = mock(EndpointCodeDetails.class);
        MethodDeclaration mockMethodDeclaration1 = mock(MethodDeclaration.class);
        MethodDeclaration mockMethodDeclaration2 = mock(MethodDeclaration.class);
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockChatResult = mock(Generation.class); // Обновление типа
        AssistantMessage mockChatOutput = mock(AssistantMessage.class); // Обновление типа

        // Настройка моков для EndpointCodeDetails
        when(mockEndpointCodeDetails.getEndpointMethods()).thenReturn(List.of(mockMethodDeclaration1, mockMethodDeclaration2));
        when(mockEndpointCodeDetails.getClassName()).thenReturn("TestClass");
        when(mockEndpointCodeDetails.serializeResults()).thenReturn("Serialized Endpoint Code");

        // Настройка моков для методов
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockEmbeddingResponse);

        // Настройка контекста
        Map<String, String> context = new HashMap<>();
        context.put("endpointDescription", "Test Description");
        context.put("examples", "Example Test");
        context.put("previousTestFileContent", "");

        // Замокированный вызов retrieveContext через spy
        doReturn(context).when(ragServiceSpy).retrieveContext(mockEmbeddingResponse);

        // Настройка цепочки вызовов для ChatService
        when(chatService.sendRequest(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockChatResult); // Настройка mockChatResult
        when(mockChatResult.getOutput()).thenReturn(mockChatOutput);   // Настройка mockChatOutput
        when(mockChatOutput.getContent()).thenReturn("[JAVA_CODE]public class Test {}[/JAVA_CODE]");

        // Вызов тестируемого метода
        String generatedTests = ragServiceSpy.generateTests(mockEndpointCodeDetails);

        // Проверка результата
        assertTrue(generatedTests.contains("public class Test {}"));

        // Проверка вызова зависимостей
        verify(embeddingService, times(2)).getEmbedding(anyString()); // Два вызова для двух методов
        verify(chatService, times(3)).sendRequest(any(Prompt.class)); // Два вызова для двух методов
    }

    @SneakyThrows
    @Test
    void testGenerateTests_MissingJavaCodeTags() {
        // Создание spy для RAGService
        RAGService ragServiceSpy = spy(new RAGService(chatService, embeddingService, vectorStorageService));

        // Мокирование данных
        EndpointCodeDetails mockEndpointCodeDetails = mock(EndpointCodeDetails.class);
        MethodDeclaration mockMethodDeclaration = mock(MethodDeclaration.class);
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockChatResult = mock(Generation.class); // Обновление типа
        AssistantMessage mockChatOutput = mock(AssistantMessage.class); // Обновление типа

        // Настройка моков для EndpointCodeDetails
        when(mockEndpointCodeDetails.getEndpointMethods()).thenReturn(List.of(mockMethodDeclaration));
        when(mockEndpointCodeDetails.getClassName()).thenReturn("TestClass");
        when(mockEndpointCodeDetails.serializeResults()).thenReturn("Serialized Endpoint Code");

        // Настройка моков для методов
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockEmbeddingResponse);

        // Настройка контекста
        Map<String, String> context = new HashMap<>();
        context.put("endpointDescription", "Test Description");
        context.put("examples", "Example Test");
        context.put("previousTestFileContent", "");

        // Замокированный вызов retrieveContext через spy
        doReturn(context).when(ragServiceSpy).retrieveContext(mockEmbeddingResponse);

        // Настройка цепочки вызовов для ChatService
        when(chatService.sendRequest(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockChatResult); // Настройка mockChatResult
        when(mockChatResult.getOutput()).thenReturn(mockChatOutput);   // Настройка mockChatOutput
        when(mockChatOutput.getContent()).thenReturn("No Java Code Tags");

        // Вызов тестируемого метода
        String generatedTests = ragServiceSpy.generateTests(mockEndpointCodeDetails);

        // Проверка результата
        assertEquals("No Java Code Tags\n", generatedTests);

        // Проверка вызова зависимостей
        verify(embeddingService).getEmbedding(anyString());
        verify(chatService, times(1)).sendRequest(any(Prompt.class));
    }

    @Test
    void testGenerateTests_HandlesException() {
        // Мокирование данных
        EndpointCodeDetails mockEndpointCodeDetails = mock(EndpointCodeDetails.class);

        // Мокирование исключения
        when(mockEndpointCodeDetails.getEndpointMethods()).thenThrow(new RuntimeException("Simulated failure"));

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                ragService.generateTests(mockEndpointCodeDetails)
        );

        assertTrue(exception.getMessage().contains("Failed to generate tests: Simulated failure"));
    }
}