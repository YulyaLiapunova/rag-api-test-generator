package com.aiqa.ragapitestgenerator.util;

import com.aiqa.ragapitestgenerator.model.KnowledgeChunk;
import com.alibaba.fastjson.JSONObject;
import io.milvus.v2.client.ConnectConfig;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VectorStorageService {
    private final MilvusClientV2 vectorStorageClient;
    private static final String MILVUS_URL = "http://localhost:19530";
    private static final String COLLECTION_NAME = "knowledge_base";
    private static final String ID_FIELD = "id";
    private static final String VECTOR_FIELD = "embedding";
    private static final Integer VECTOR_DIM = 6;
    private static final String DOCUMENT_ID_FIELD = "document_id";
    private static final String CHUNK_ID_FIELD = "chunk_id";

    public VectorStorageService() {
        ConnectConfig config = ConnectConfig.builder().uri(MILVUS_URL).build();
        this.vectorStorageClient = new MilvusClientV2(config);
        initializeCollection();
    }

    private boolean isCollectionExists() {
        return this.vectorStorageClient.hasCollection(
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
                    .build();
            AddFieldReq embeddingField = AddFieldReq.builder()
                    .fieldName(VECTOR_FIELD)
                    .dataType(DataType.FloatVector)
                    .dimension(VECTOR_DIM)
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
            collectionSchema.addField(docIdField);
            collectionSchema.addField(chunkIdField);

            CreateCollectionReq requestCreate = CreateCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .collectionSchema(collectionSchema)
                    .build();

            this.vectorStorageClient.createCollection(requestCreate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Milvus collection: " + e.getMessage(), e);
        }
    }

    public void createIndex() {
        try {
            IndexParam indexParam = IndexParam.builder()
                    .fieldName(VECTOR_FIELD)
                    .metricType(IndexParam.MetricType.COSINE)
                    .build();

            CreateIndexReq createIndexReq = CreateIndexReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .indexParams(Collections.singletonList(indexParam))
                    .build();

            this.vectorStorageClient.createIndex(createIndexReq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create index in Milvus: " + e.getMessage(), e);
        }
    }

    public void loadCollection() {
        try {
            this.vectorStorageClient.loadCollection(
                    LoadCollectionReq.builder()
                            .collectionName(COLLECTION_NAME)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Error loading collection into memory: " + e.getMessage());
        }
    }

    public void insertEmbeddings(List<KnowledgeChunk> chunks) {
        try {
            List<JSONObject> insertData = new ArrayList<JSONObject>();

            for(int i = 0; i < chunks.size(); ++i) {
                JSONObject jsonObject = new JSONObject();

//                for(int j = 0; j < VECTOR_DIM; ++j) {
//                    vectorList.add((new Random()).nextFloat());
//                }

                jsonObject.put(VECTOR_FIELD, chunks.get(i).getEmbedding());
                jsonObject.put(DOCUMENT_ID_FIELD, chunks.get(i).getDocumentId());
                jsonObject.put(CHUNK_ID_FIELD, chunks.get(i).getChunkId());
                insertData.add(jsonObject);
            }

            InsertReq insertReq = InsertReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(insertData)
                    .build();

            this.vectorStorageClient.insert(insertReq);
            // TimeUnit.SECONDS.sleep(1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert embedding into Milvus: " + e.getMessage(), e);
        }
    }

    public SearchResp searchEmbedding(List<float[]> embedding, int topK) {
        try {
            this.loadCollection();

//            List<List<Float>> result = new ArrayList<>();
//            for (float[] array : embedding) {
//                List<Float> converted = new ArrayList<>();
//                for (float value : array) {
//                    converted.add(value);
//                }
//                result.add(converted);
//            }

            List<Float> queryVector = new ArrayList<>();
            for (float value : embedding.get(0)) { // Use the first vector
                queryVector.add(value);
            }

            System.out.println(queryVector.get(0).getClass().getName()); // Should print `java.lang.Float`

            SearchReq searchReq = SearchReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(queryVector)
                    .outputFields(Collections.singletonList(VECTOR_FIELD))
                    .topK(topK)
                    .build();

            return this.vectorStorageClient.search(searchReq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search embedding in Milvus: " + e.getMessage(), e);
        }
    }
}
