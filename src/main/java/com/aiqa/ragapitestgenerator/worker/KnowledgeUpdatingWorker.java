package com.aiqa.ragapitestgenerator.worker;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.aiqa.ragapitestgenerator.service.EmbeddingService;
import com.aiqa.ragapitestgenerator.service.VectorStorageService;
import com.aiqa.ragapitestgenerator.util.CodePreprocessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class KnowledgeUpdatingWorker {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeUpdatingWorker.class);
    private final EmbeddingService embeddingService;
    private final VectorStorageService vectorStorage;

    public enum DocumentTypes {
        TESTS,
        DOCUMENTATION
    }

    @Autowired
    public KnowledgeUpdatingWorker(EmbeddingService embeddingService, VectorStorageService vectorStorageService) {
        this.embeddingService = embeddingService;
        this.vectorStorage = vectorStorageService;
    }

    static long generateLongFromString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result <<= 8;
                result |= (hash[i] & 0xFF);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }

    protected List<KnowledgeChunk> processCodeContent(String fileName, String fileContent) {
        String preprocessedCode = CodePreprocessor.preprocessCode(fileContent);
        EmbeddingResponse embeddingResponse = this.embeddingService.getEmbedding(preprocessedCode);

        if (embeddingResponse == null || embeddingResponse.getResult() == null) {
            throw new RuntimeException("EmbeddingResponse or its result is null");
        }

        Embedding output = embeddingResponse.getResult();
        List<Float> embedding = new ArrayList<>();
        for (float value : output.getOutput()) {
            embedding.add(value);
        }

        Long documentId = KnowledgeUpdatingWorker.generateLongFromString(fileName);

        KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
        knowledgeChunk.setEmbedding(embedding);
        knowledgeChunk.setDocumentType(DocumentTypes.TESTS.name());
        knowledgeChunk.setDocumentId(documentId);
        knowledgeChunk.setChunkId(1L);

        return Collections.singletonList(knowledgeChunk);
    }

    protected List<KnowledgeChunk> processDocContent(String fileName, String fileContent) {
        EmbeddingResponse embeddingResponse = this.embeddingService.getEmbedding(fileContent);

        if (embeddingResponse == null || embeddingResponse.getResult() == null) {
            throw new RuntimeException("EmbeddingResponse or its result is null");
        }

        Embedding output = embeddingResponse.getResult();
        List<Float> embedding = new ArrayList<>();
        for (float value : output.getOutput()) {
            embedding.add(value);
        }

        Long documentId = KnowledgeUpdatingWorker.generateLongFromString(fileName);

        KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
        knowledgeChunk.setEmbedding(embedding);
        knowledgeChunk.setDocumentType(DocumentTypes.DOCUMENTATION.name());
        knowledgeChunk.setDocumentId(documentId);
        knowledgeChunk.setChunkId(1L);

        return Collections.singletonList(knowledgeChunk);
    }

    public void processKnowledgeBaseUpdate(@NotNull Map<String, Object> event) {
        try {
            String documentType = (String) event.get("documentType");
            String fileContent = (String) event.get("fileContent");
            String fileName = (String) event.get("fileName");
            List<KnowledgeChunk> knowledgeChunks;

            if (Objects.equals(documentType, DocumentTypes.TESTS.name())) {
                knowledgeChunks = processCodeContent(fileName, fileContent);
            } else {
                knowledgeChunks = processDocContent(fileName, fileContent);
            }

            for (KnowledgeChunk chunk : knowledgeChunks) {
                this.vectorStorage.insertEmbeddings(chunk);
                logger.info(
                        "File type '{}' with id '{}' processed and data inserted into Milvus.",
                        chunk.getDocumentType(),
                        chunk.getDocumentId()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing file: " +
                    (e.getMessage() != null
                    ? e.getMessage()
                    : "null"),
                    e);
        }
    }
}
