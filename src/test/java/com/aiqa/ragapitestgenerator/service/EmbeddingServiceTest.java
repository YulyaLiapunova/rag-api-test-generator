package com.aiqa.ragapitestgenerator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbeddingServiceTest {
    @Mock
    private OllamaEmbeddingModel mockEmbeddingModel;

    @InjectMocks
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEmbedding_ReturnsExpectedEmbedding() {
        // Arrange
        String input = "test input";
        float[] embeddingData = {0.5f, 1.5f, 2.5f};
        Embedding embedding = new Embedding(embeddingData, 0);
        EmbeddingResponse response = new EmbeddingResponse(List.of(embedding));

        when(mockEmbeddingModel.call(any())).thenReturn(response);

        // Act
        EmbeddingResponse result = embeddingService.getEmbedding(input);

        // Assert
        assertNotNull(result);
        assertEquals(embedding, result.getResult());
        assertArrayEquals(embeddingData, result.getResult().getOutput());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForEmptyEmbeddingData() {
        // Arrange
        String input = "test input";
        EmbeddingResponse response = new EmbeddingResponse(List.of());

        when(mockEmbeddingModel.call(any())).thenReturn(response);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Error during embedding model call: No embedding data available.", exception.getMessage());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForNullInput() {
        // Arrange
        String input = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Input string cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForEmptyInput() {
        // Arrange
        String input = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Input string cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForModelCallError() {
        // Arrange
        String input = "test input";

        when(mockEmbeddingModel.call(any())).thenThrow(new RuntimeException("Model call failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Error during embedding model call: Model call failed", exception.getMessage());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForNullResponse() {
        // Arrange
        String input = "test input";

        when(mockEmbeddingModel.call(any())).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Error during embedding model call: Invalid response from embedding model: null", exception.getMessage());
    }

    @Test
    void testGetEmbedding_ThrowsExceptionForNullResultInResponse() {
        // Arrange
        String input = "test input";
        EmbeddingResponse response = mock(EmbeddingResponse.class);

        when(response.getResult()).thenReturn(null);
        when(mockEmbeddingModel.call(any())).thenReturn(response);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> embeddingService.getEmbedding(input)
        );
        assertEquals("Error during embedding model call: Invalid response from embedding model: null", exception.getMessage());
    }
}
