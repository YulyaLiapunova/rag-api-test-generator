package com.aiqa.ragapitestgenerator.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.alibaba.fastjson.JSONObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.request.InsertReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class VectorStorageServiceTest {

    private VectorStorageService vectorStorageService;

    @Mock
    private MilvusClientV2 mockMilvusClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vectorStorageService = new VectorStorageService(mockMilvusClient);
    }

    @Test
    void testInitializeCollection_ThrowsRuntimeException() {
        // Мокирование ошибки при вызове createCollection
        doThrow(new RuntimeException("Simulated create collection failure"))
                .when(mockMilvusClient).createCollection(any());

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vectorStorageService = new VectorStorageService(mockMilvusClient) // Без пропуска инициализации
        );

        // Проверка сообщения исключения
        assertTrue(exception.getMessage().contains("Failed to initialize Milvus collection: Simulated create collection failure"));
    }

    @Test
    void testSearchEmbedding() {
        // Мокирование данных
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(mockEmbeddingResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(mockMilvusClient.search(any())).thenReturn(mock(SearchResp.class));

        // Вызов метода
        SearchResp searchResp = vectorStorageService.searchEmbedding(mockEmbeddingResponse, 5);

        // Проверка вызова методов
        verify(mockMilvusClient).loadCollection(any());
        verify(mockMilvusClient).search(any());

        // Проверка результата
        assertNotNull(searchResp);
    }

    @Test
    void testSearchEmbedding_ThrowsIllegalArgumentExceptionWhenVectorIsEmpty() {
        // Мокирование данных
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(mockEmbeddingResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[0]); // Пустой массив

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vectorStorageService.searchEmbedding(mockEmbeddingResponse, 5)
        );
        assertEquals("Failed to search embedding in Milvus: The embedding vector is empty. Ensure the embedding response contains valid data.", exception.getMessage());

        // Убедиться, что search не вызывается
        verify(mockMilvusClient, never()).search(any());
    }

    @Test
    void testSearchEmbedding_ThrowsRuntimeExceptionWhenSearchFails() {
        // Мокирование данных
        EmbeddingResponse mockEmbeddingResponse = mock(EmbeddingResponse.class);
        Embedding mockEmbedding = mock(Embedding.class);
        when(mockEmbeddingResponse.getResult()).thenReturn(mockEmbedding);
        when(mockEmbedding.getOutput()).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        // Мокирование ошибки в методе search
        when(mockMilvusClient.search(any())).thenThrow(new RuntimeException("Simulated search failure"));

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vectorStorageService.searchEmbedding(mockEmbeddingResponse, 5)
        );
        assertTrue(exception.getMessage().contains("Failed to search embedding in Milvus: Simulated search failure"));

        // Проверить, что loadCollection был вызван
        verify(mockMilvusClient).loadCollection(any());
        // Убедиться, что search был вызван
        verify(mockMilvusClient).search(any());
    }

    @Test
    void testLoadCollection() {
        // Вызов метода
        vectorStorageService.loadCollection();

        // Проверка вызова метода
        verify(mockMilvusClient).loadCollection(any());
    }

    @Test
    void testLoadCollection_HandlesException() {
        // Мокирование ошибки в методе loadCollection
        doThrow(new RuntimeException("Simulated load failure"))
                .when(mockMilvusClient).loadCollection(any());

        // Перехват вывода System.err
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try {
            // Вызов метода, который вызывает ошибку
            vectorStorageService.loadCollection();

            // Проверка, что сообщение об ошибке было выведено
            String errorOutput = errContent.toString();
            assertTrue(errorOutput.contains("Error loading collection into memory: Simulated load failure"));
        } finally {
            // Восстановление оригинального System.err
            System.setErr(originalErr);
        }

        // Проверка, что loadCollection был вызван
        verify(mockMilvusClient).loadCollection(any());
    }

    @Test
    void testCreateIndex_ThrowsRuntimeExceptionWhenCreateIndexFails() {
        // Мокирование ошибки в методе createIndex
        doThrow(new RuntimeException("Simulated index creation failure"))
                .when(mockMilvusClient).createIndex(any());

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vectorStorageService.createIndex()
        );

        // Проверка сообщения исключения
        assertTrue(exception.getMessage().contains("Failed to create index in Milvus: Simulated index creation failure"));

        // Проверить, что createIndex вызывался дважды
        verify(mockMilvusClient, times(2)).createIndex(any());
    }

    @Test
    void testInsertEmbeddings_Success() {
        // Мокирование данных
        KnowledgeChunk chunk = mock(KnowledgeChunk.class);
        when(chunk.getEmbedding()).thenReturn(Collections.singletonList(0.1f));
        when(chunk.getDocumentType()).thenReturn("testType");
        when(chunk.getDocumentId()).thenReturn(1L);
        when(chunk.getChunkId()).thenReturn(2L);

        // Вызов метода
        vectorStorageService.insertEmbeddings(chunk);

        // Захват аргументов
        ArgumentCaptor<InsertReq> insertReqCaptor = ArgumentCaptor.forClass(InsertReq.class);
        verify(mockMilvusClient).insert(insertReqCaptor.capture());

        // Проверка переданных данных
        InsertReq capturedReq = insertReqCaptor.getValue();
        assertEquals("knowledge_base", capturedReq.getCollectionName());

        JSONObject jsonObject = capturedReq.getData().get(0);
        assertEquals(0.1f, jsonObject.getJSONArray("embedding").getFloat(0));
        assertEquals("testType", jsonObject.getString("document_type"));
        assertEquals(1L, jsonObject.getLong("document_id"));
        assertEquals(2L, jsonObject.getLong("chunk_id"));
    }

    @Test
    void testInsertEmbeddings_ThrowsRuntimeException() {
        // Мокирование данных
        KnowledgeChunk chunk = mock(KnowledgeChunk.class);
        when(chunk.getEmbedding()).thenReturn(Collections.singletonList(0.1f));
        when(chunk.getDocumentType()).thenReturn("testType");
        when(chunk.getDocumentId()).thenReturn(1L);
        when(chunk.getChunkId()).thenReturn(2L);

        // Мокирование ошибки
        doThrow(new RuntimeException("Simulated insertion failure"))
                .when(mockMilvusClient).insert(any());

        // Проверка исключения
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vectorStorageService.insertEmbeddings(chunk)
        );

        // Проверка сообщения исключения
        assertTrue(exception.getMessage().contains("Failed to insert embedding into Milvus: Simulated insertion failure"));
    }
}
