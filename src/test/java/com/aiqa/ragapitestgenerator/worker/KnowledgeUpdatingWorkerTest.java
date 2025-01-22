package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.aiqa.ragapitestgenerator.service.EmbeddingService;
import com.aiqa.ragapitestgenerator.service.VectorStorageService;
import com.aiqa.ragapitestgenerator.util.CodePreprocessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KnowledgeUpdatingWorkerTest {
    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStorageService vectorStorage;

    private KnowledgeUpdatingWorker knowledgeUpdatingWorker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        knowledgeUpdatingWorker = new KnowledgeUpdatingWorker(embeddingService, vectorStorage);
    }

    @Test
    void testProcessKnowledgeBaseUpdate_WithTests() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "TESTS");
        event.put("fileContent", "public class Test {}");
        event.put("fileName", "TestFile.java");

        KnowledgeChunk mockChunk = new KnowledgeChunk();
        mockChunk.setDocumentType("TESTS");
        mockChunk.setDocumentId(123L);
        mockChunk.setEmbedding(List.of(0.1f, 0.2f, 0.3f));
        mockChunk.setChunkId(1L);

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);
        doReturn(List.of(mockChunk)).when(spyWorker).processCodeContent(anyString(), anyString());

        // Act
        spyWorker.processKnowledgeBaseUpdate(event);

        // Assert
        verify(spyWorker).processCodeContent("TestFile.java", "public class Test {}");
        verify(vectorStorage).insertEmbeddings(mockChunk);
    }

    @Test
    void testProcessKnowledgeBaseUpdate_WithDocumentation() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "DOCUMENTATION");
        event.put("fileContent", "This is documentation content.");
        event.put("fileName", "DocFile.txt");

        KnowledgeChunk mockChunk = new KnowledgeChunk();
        mockChunk.setDocumentType("DOCUMENTATION");
        mockChunk.setDocumentId(123L);
        mockChunk.setEmbedding(List.of(0.4f, 0.5f, 0.6f));
        mockChunk.setChunkId(1L);

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);
        doReturn(List.of(mockChunk)).when(spyWorker).processDocContent(anyString(), anyString());

        // Act
        spyWorker.processKnowledgeBaseUpdate(event);

        // Assert
        verify(spyWorker).processDocContent("DocFile.txt", "This is documentation content.");
        verify(vectorStorage).insertEmbeddings(mockChunk);
    }

    @Test
    void testProcessKnowledgeBaseUpdate_WithMultipleChunks() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "TESTS");
        event.put("fileContent", "public class Test {}");
        event.put("fileName", "TestFile.java");

        KnowledgeChunk chunk1 = new KnowledgeChunk();
        chunk1.setDocumentType("TESTS");
        chunk1.setDocumentId(123L);
        chunk1.setEmbedding(List.of(0.1f, 0.2f, 0.3f));
        chunk1.setChunkId(1L);

        KnowledgeChunk chunk2 = new KnowledgeChunk();
        chunk2.setDocumentType("TESTS");
        chunk2.setDocumentId(124L);
        chunk2.setEmbedding(List.of(0.4f, 0.5f, 0.6f));
        chunk2.setChunkId(2L);

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);
        doReturn(List.of(chunk1, chunk2)).when(spyWorker).processCodeContent(anyString(), anyString());

        // Act
        spyWorker.processKnowledgeBaseUpdate(event);

        // Assert
        verify(vectorStorage).insertEmbeddings(chunk1);
        verify(vectorStorage).insertEmbeddings(chunk2);
    }

    @Test
    void testProcessKnowledgeBaseUpdate_ExceptionInInsertEmbeddings() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "TESTS");
        event.put("fileContent", "public class Test {}");
        event.put("fileName", "TestFile.java");

        KnowledgeChunk mockChunk = new KnowledgeChunk();
        mockChunk.setDocumentType("TESTS");
        mockChunk.setDocumentId(123L);
        mockChunk.setEmbedding(List.of(0.1f, 0.2f, 0.3f));
        mockChunk.setChunkId(1L);

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);
        doReturn(List.of(mockChunk)).when(spyWorker).processCodeContent(anyString(), anyString());

        doThrow(new RuntimeException("Test exception")).when(vectorStorage).insertEmbeddings(mockChunk);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spyWorker.processKnowledgeBaseUpdate(event);
        });

        assertTrue(exception.getMessage().contains("Error processing file: Test exception"));
    }

    @Test
    void testProcessKnowledgeBaseUpdate_ExceptionWithMessage() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "TESTS");
        event.put("fileContent", "public class Test {}");
        event.put("fileName", "TestFile.java");

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);

        // Искусственно выбрасываем исключение с сообщением
        doThrow(new RuntimeException("Custom exception message"))
                .when(spyWorker).processCodeContent(anyString(), anyString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spyWorker.processKnowledgeBaseUpdate(event);
        });

        // Проверяем, что условие с `e.getMessage() != null` покрыто
        assertTrue(exception.getMessage().contains("Error processing file: Custom exception message"));
    }

    @Test
    void testProcessKnowledgeBaseUpdate_ExceptionWithNullMessage() {
        // Arrange
        Map<String, Object> event = new HashMap<>();
        event.put("documentType", "TESTS");
        event.put("fileContent", "public class Test {}");
        event.put("fileName", "TestFile.java");

        KnowledgeUpdatingWorker spyWorker = spy(knowledgeUpdatingWorker);

        // Искусственно выбрасываем исключение с null-сообщением
        doThrow(new RuntimeException((String) null))
                .when(spyWorker).processCodeContent(anyString(), anyString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spyWorker.processKnowledgeBaseUpdate(event);
        });

        // Проверяем, что сообщение заменилось на "null"
        assertTrue(exception.getMessage().contains("Error processing file: null"));
    }

    @Test
    void testProcessCodeContent_EmbeddingResponseIsNull() {
        // Arrange
        String fileName = "TestFile.java";
        String fileContent = "public class Test {}";

        try (MockedStatic<CodePreprocessor> mockedPreprocessor = mockStatic(CodePreprocessor.class)) {
            mockedPreprocessor.when(() -> CodePreprocessor.preprocessCode(fileContent))
                    .thenReturn("Preprocessed content");

            when(embeddingService.getEmbedding("Preprocessed content")).thenReturn(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                knowledgeUpdatingWorker.processCodeContent(fileName, fileContent);
            });

            assertEquals("EmbeddingResponse or its result is null", exception.getMessage());
        }
    }

    @Test
    void testProcessCodeContent_EmbeddingResultIsNull() {
        // Arrange
        String fileName = "TestFile.java";
        String fileContent = "public class Test {}";

        try (MockedStatic<CodePreprocessor> mockedPreprocessor = mockStatic(CodePreprocessor.class)) {
            mockedPreprocessor.when(() -> CodePreprocessor.preprocessCode(fileContent))
                    .thenReturn("Preprocessed content");

            EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
            when(embeddingService.getEmbedding("Preprocessed content")).thenReturn(mockResponse);
            when(mockResponse.getResult()).thenReturn(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                knowledgeUpdatingWorker.processCodeContent(fileName, fileContent);
            });

            assertEquals("EmbeddingResponse or its result is null", exception.getMessage());
        }
    }

    @Test
    void testProcessCodeContent_EmptyOutput() {
        // Arrange
        String fileName = "TestFile.java";
        String fileContent = "public class Test {}";

        try (MockedStatic<CodePreprocessor> mockedPreprocessor = mockStatic(CodePreprocessor.class)) {
            mockedPreprocessor.when(() -> CodePreprocessor.preprocessCode(fileContent))
                    .thenReturn("Preprocessed content");

            EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
            Embedding mockEmbedding = mock(Embedding.class);
            when(embeddingService.getEmbedding("Preprocessed content")).thenReturn(mockResponse);
            when(mockResponse.getResult()).thenReturn(mockEmbedding);
            when(mockEmbedding.getOutput()).thenReturn(new float[0]); // Пустой массив

            // Act
            List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processCodeContent(fileName, fileContent);

            // Assert
            assertNotNull(chunks);
            assertEquals(1, chunks.size());
            KnowledgeChunk chunk = chunks.get(0);
            assertNotNull(chunk.getEmbedding());
            assertTrue(chunk.getEmbedding().isEmpty());
            assertEquals("TESTS", chunk.getDocumentType());
            assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
            assertEquals(1L, chunk.getChunkId());
        }
    }

    @Test
    void testProcessCodeContent_FilledOutput() {
        // Arrange
        String fileName = "TestFile.java";
        String fileContent = "public class Test {}";

        try (MockedStatic<CodePreprocessor> mockedPreprocessor = mockStatic(CodePreprocessor.class)) {
            mockedPreprocessor.when(() -> CodePreprocessor.preprocessCode(fileContent))
                    .thenReturn("Preprocessed content");

            EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
            Embedding mockEmbedding = mock(Embedding.class);
            when(embeddingService.getEmbedding("Preprocessed content")).thenReturn(mockResponse);
            when(mockResponse.getResult()).thenReturn(mockEmbedding);
            when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f, 0.2f, 0.3f}); // Заполненный массив

            // Act
            List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processCodeContent(fileName, fileContent);

            // Assert
            assertNotNull(chunks);
            assertEquals(1, chunks.size());
            KnowledgeChunk chunk = chunks.get(0);
            assertNotNull(chunk.getEmbedding());
            assertEquals(List.of(0.1f, 0.2f, 0.3f), chunk.getEmbedding());
            assertEquals("TESTS", chunk.getDocumentType());
            assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
            assertEquals(1L, chunk.getChunkId());
        }
    }

    @Test
    void testProcessCodeContent_ReturnsKnowledgeChunk() {
        // Arrange
        String fileName = "TestFile.java";
        String fileContent = "public class Test {}";

        try (MockedStatic<CodePreprocessor> mockedPreprocessor = mockStatic(CodePreprocessor.class)) {
            mockedPreprocessor.when(() -> CodePreprocessor.preprocessCode(fileContent))
                    .thenReturn("Preprocessed content");

            EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
            Embedding mockEmbedding = mock(Embedding.class);
            when(embeddingService.getEmbedding("Preprocessed content")).thenReturn(mockResponse);
            when(mockResponse.getResult()).thenReturn(mockEmbedding);
            when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f}); // Один элемент

            // Act
            List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processCodeContent(fileName, fileContent);

            // Assert
            assertNotNull(chunks);
            assertEquals(1, chunks.size());
            KnowledgeChunk chunk = chunks.get(0);
            assertEquals(List.of(0.1f), chunk.getEmbedding());
            assertEquals("TESTS", chunk.getDocumentType());
            assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
            assertEquals(1L, chunk.getChunkId());
        }
    }

    @Test
    void testProcessDocContent_EmbeddingResponseIsNull() {
        // Arrange
        String fileName = "DocFile.txt";
        String fileContent = "This is documentation content.";

        when(embeddingService.getEmbedding(fileContent)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            knowledgeUpdatingWorker.processDocContent(fileName, fileContent);
        });

        assertEquals("EmbeddingResponse or its result is null", exception.getMessage());
    }

    @Test
    void testProcessDocContent_EmbeddingResultIsNull() {
        // Arrange
        String fileName = "DocFile.txt";
        String fileContent = "This is documentation content.";

        EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
        when(embeddingService.getEmbedding(fileContent)).thenReturn(mockResponse);
        when(mockResponse.getResult()).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            knowledgeUpdatingWorker.processDocContent(fileName, fileContent);
        });

        assertEquals("EmbeddingResponse or its result is null", exception.getMessage());
    }

    @Test
    void testProcessDocContent_EmptyOutput() {
        // Arrange
        String fileName = "DocFile.txt";
        String fileContent = "This is documentation content.";

        EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(embeddingService.getEmbedding(fileContent)).thenReturn(mockResponse);
        when(mockResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[0]); // Пустой массив

        // Act
        List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processDocContent(fileName, fileContent);

        // Assert
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        KnowledgeChunk chunk = chunks.get(0);
        assertNotNull(chunk.getEmbedding());
        assertTrue(chunk.getEmbedding().isEmpty());
        assertEquals("DOCUMENTATION", chunk.getDocumentType());
        assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
        assertEquals(1L, chunk.getChunkId());
    }

    @Test
    void testProcessDocContent_FilledOutput() {
        // Arrange
        String fileName = "DocFile.txt";
        String fileContent = "This is documentation content.";

        EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(embeddingService.getEmbedding(fileContent)).thenReturn(mockResponse);
        when(mockResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f, 0.2f, 0.3f}); // Заполненный массив

        // Act
        List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processDocContent(fileName, fileContent);

        // Assert
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        KnowledgeChunk chunk = chunks.get(0);
        assertEquals(List.of(0.1f, 0.2f, 0.3f), chunk.getEmbedding());
        assertEquals("DOCUMENTATION", chunk.getDocumentType());
        assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
        assertEquals(1L, chunk.getChunkId());
    }

    @Test
    void testProcessDocContent_ReturnsKnowledgeChunk() {
        // Arrange
        String fileName = "DocFile.txt";
        String fileContent = "This is documentation content.";

        EmbeddingResponse mockResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(embeddingService.getEmbedding(fileContent)).thenReturn(mockResponse);
        when(mockResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f}); // Один элемент

        // Act
        List<KnowledgeChunk> chunks = knowledgeUpdatingWorker.processDocContent(fileName, fileContent);

        // Assert
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        KnowledgeChunk chunk = chunks.get(0);
        assertEquals(List.of(0.1f), chunk.getEmbedding());
        assertEquals("DOCUMENTATION", chunk.getDocumentType());
        assertEquals(KnowledgeUpdatingWorker.generateLongFromString(fileName), chunk.getDocumentId());
        assertEquals(1L, chunk.getChunkId());
    }

    @Test
    void testGenerateLongFromString() {
        // Act
        long result = KnowledgeUpdatingWorker.generateLongFromString("TestFile.java");

        // Assert
        assertEquals(8985125642329676891L, result); // Example expected hash
    }

    @Test
    void testGenerateLongFromString_ThrowsException() {
        // Arrange
        String input = "TestInput";

        try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
            mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("SHA-256 algorithm not found!"));

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                KnowledgeUpdatingWorker.generateLongFromString(input);
            });

            assertEquals("SHA-256 algorithm not found!", exception.getCause().getMessage());
        }
    }

}
