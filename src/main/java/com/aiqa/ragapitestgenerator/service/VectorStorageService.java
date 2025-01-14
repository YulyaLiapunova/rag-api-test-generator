package com.aiqa.ragapitestgenerator.service;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.alibaba.fastjson.JSONObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VectorStorageService {
    private final MilvusClientV2 vectorClient;
    private static final String COLLECTION_NAME = "knowledge_base";
    private static final String ID_FIELD = "id";
    private static final String VECTOR_FIELD = "embedding";
    private static final Integer VECTOR_DIM = 768;
    private static final String DOCUMENT_ID_FIELD = "document_id";
    private static final String DOCUMENT_TYPE_FIELD = "document_type";
    private static final String CHUNK_ID_FIELD = "chunk_id";

    public VectorStorageService(MilvusClientV2 milvusClientV2) {
        this.vectorClient = milvusClientV2;
        initializeCollection();
    }

    private boolean isCollectionExists() {
        return this.vectorClient.hasCollection(
                HasCollectionReq.builder()
                        .collectionName(COLLECTION_NAME)
                        .build()
        );
    }

    private void initializeCollection() {
        try {
            if (this.isCollectionExists()) return;

            AddFieldReq idField = AddFieldReq.builder()
                    .fieldName(ID_FIELD)
                    .dataType(DataType.Int64)
                    .isPrimaryKey(Boolean.TRUE)
                    .autoID(true)
                    .build();
            AddFieldReq embeddingField = AddFieldReq.builder()
                    .fieldName(VECTOR_FIELD)
                    .dataType(DataType.FloatVector)
                    .dimension(VECTOR_DIM)
                    .build();
            AddFieldReq docTypeField = AddFieldReq.builder()
                    .fieldName(DOCUMENT_TYPE_FIELD)
                    .dataType(DataType.VarChar)
                    .build();
            AddFieldReq docIdField = AddFieldReq.builder()
                    .fieldName(DOCUMENT_ID_FIELD)
                    .dataType(DataType.Int64)
                    .build();
            AddFieldReq chunkIdField = AddFieldReq.builder()
                    .fieldName(CHUNK_ID_FIELD)
                    .dataType(DataType.Int64)
                    .build();

            CreateCollectionReq.CollectionSchema collectionSchema = CreateCollectionReq
                    .CollectionSchema
                    .builder()
                    .build();

            collectionSchema.addField(idField);
            collectionSchema.addField(embeddingField);
            collectionSchema.addField(docTypeField);
            collectionSchema.addField(docIdField);
            collectionSchema.addField(chunkIdField);

            CreateCollectionReq requestCreate = CreateCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .collectionSchema(collectionSchema)
                    .build();

            this.vectorClient.createCollection(requestCreate);
            this.createIndex();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Milvus collection: " + e.getMessage(), e);
        }
    }

    public void createIndex() {
        try {
            IndexParam indexParam = IndexParam.builder()
                    .fieldName(VECTOR_FIELD)
                    .indexType(IndexParam.IndexType.HNSW)
                    .metricType(IndexParam.MetricType.L2)
                    .extraParams(Collections.singletonMap("M", 16))
                    .build();

            CreateIndexReq createIndexReq = CreateIndexReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .indexParams(Collections.singletonList(indexParam))
                    .build();

            this.vectorClient.createIndex(createIndexReq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create index in Milvus: " + e.getMessage(), e);
        }
    }

    public void loadCollection() {
        try {
            this.vectorClient.loadCollection(
                    LoadCollectionReq.builder()
                            .collectionName(COLLECTION_NAME)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Error loading collection into memory: " + e.getMessage());
        }
    }

    public void insertEmbeddings(KnowledgeChunk chunk) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(VECTOR_FIELD, chunk.getEmbedding());
            jsonObject.put(DOCUMENT_TYPE_FIELD, chunk.getDocumentType());
            jsonObject.put(DOCUMENT_ID_FIELD, chunk.getDocumentId());
            jsonObject.put(CHUNK_ID_FIELD, chunk.getChunkId());

            InsertReq insertReq = InsertReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(Collections.singletonList(jsonObject))
                    .build();

            this.vectorClient.insert(insertReq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert embedding into Milvus: " + e.getMessage(), e);
        }
    }

    public SearchResp searchEmbedding(EmbeddingResponse embeddingResponse, int topK) {
        try {
            this.loadCollection();

            Embedding output = embeddingResponse.getResult();
            List<Float> result = new ArrayList<>();
            for (float value : output.getOutput()) {
                result.add(value);
            }

            if (result.isEmpty()) {
                throw new IllegalArgumentException("The embedding vector is empty. Ensure the embedding response contains valid data.");
            }

            SearchReq searchReq = SearchReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(Collections.singletonList(result))
                    .outputFields(List.of(VECTOR_FIELD, DOCUMENT_ID_FIELD, CHUNK_ID_FIELD, DOCUMENT_TYPE_FIELD))
                    .topK(topK)
                    .build();

            return this.vectorClient.search(searchReq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search embedding in Milvus: " + e.getMessage(), e);
        }
    }
}
