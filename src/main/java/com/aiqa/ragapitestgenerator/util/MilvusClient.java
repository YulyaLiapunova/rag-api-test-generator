package com.aiqa.ragapitestgenerator.util;

import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.ListPartitionsReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class MilvusClient {
    private final MilvusClientV2 milvusClient;

    private static final String COLLECTION_NAME = "knowledge_base";
    private static final String ID_FIELD = "embeddingId";
    private static final String VECTOR_FIELD = "embedding";
    private static final Integer VECTOR_DIM = 64;
    private static final String META_FIELD = "meta";

    public MilvusClient() {
        ConnectConfig config = ConnectConfig.builder().uri("http://localhost:19530").build();
        this.milvusClient = new MilvusClientV2(config);

        initializeCollection();
    }

    private void initializeCollection() {
        try {
            boolean exists = milvusClient.hasCollection(
                    HasCollectionReq.builder().collectionName(COLLECTION_NAME).build());

            if (!exists) {
                CreateCollectionReq.CollectionSchema collectionSchema =
                        CreateCollectionReq.CollectionSchema.builder().build();
                collectionSchema.addField(AddFieldReq.builder().fieldName(ID_FIELD)
                        .dataType(DataType.VarChar).isPrimaryKey(Boolean.TRUE).autoID(Boolean.TRUE)
                        .maxLength(100).build());
                collectionSchema.addField(AddFieldReq.builder().fieldName(VECTOR_FIELD)
                        .dataType(io.milvus.v2.common.DataType.FloatVector).dimension(VECTOR_DIM)
                        .build());
                collectionSchema.addField(
                        AddFieldReq.builder().fieldName(META_FIELD).dataType(DataType.Int8).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Milvus collection: " + e.getMessage(), e);
        }
    }

    public void insertEmbedding(String id, List<Float> embedding, Map<String, String> metadata) {
        try {
            List<JsonObject> rows = new ArrayList<>();
            Random ran = new Random();
            Gson gson = new Gson();
            for (long i = 0; i < embedding.size(); i++) {
                JsonObject row = new JsonObject();
                row.add(VECTOR_FIELD, gson.toJsonTree(CommonUtils.generateFloatVector(VECTOR_DIM)));
                row.addProperty(META_FIELD, ran.nextInt(99));
                rows.add(row);
            }

            InsertResp resp =
                    milvusClient.insert(InsertReq.builder().collectionName(COLLECTION_NAME).data(rows).build());

            List<Object> ids = resp.getPrimaryKeys();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert embedding into Milvus: " + e.getMessage(), e);
        }
    }

    public List<List<SearchResult>> searchEmbedding(List<Float> embedding, int topK) {
        try {
            List<String> outputFields = Collections.singletonList(META_FIELD);
            List<BaseVector> vectors = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                vectors.add(new FloatVec(CommonUtils.generateFloatVector(VECTOR_DIM)));
            }

            long begin = System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put("nprobe", 10);
            SearchResp resp = milvusClient.search(SearchReq.builder().collectionName(COLLECTION_NAME)
                    .topK(topK).data(vectors).annsField(VECTOR_FIELD).filter(embedding)
                    .searchParams(params).consistencyLevel(ConsistencyLevel.EVENTUALLY)
                    .outputFields(outputFields).build());

            long end = System.currentTimeMillis();
            long cost = (end - begin);

            List<List<SearchResp.SearchResult>> searchResults = resp.getSearchResults();
            
            return searchResults;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search embedding in Milvus: " + e.getMessage(), e);
        }
    }
}
