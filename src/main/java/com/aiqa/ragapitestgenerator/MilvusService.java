package com.aiqa.ragapitestgenerator;

import io.grpc.StatusRuntimeException;
import io.milvus.grpc.SearchResults;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilvusService {
    private final MilvusClientV2 milvusClient;
    private static final String COLLECTION_NAME = "java_sdk_example_general_v2";

    @Value("${milvus.host}")
    private String milvusHost;

    public MilvusService() {
        this.milvusClient = new MilvusClientV2(ConnectConfig.builder()
                .uri(milvusHost) //"http://localhost:19530"
                .build());;
    }

    public boolean createCollection(String collectionName, List<MilvusField> fields) {
        try {
            this.milvusClient.dropCollection(DropCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .build());

            CreateCollectionReq.CollectionSchema collectionSchema = CreateCollectionReq.CollectionSchema.builder()
                    .build();
            return true;
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertVectors(String collectionName, List<List<Float>> vectors) {
        try {
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields("embedding", vectors)
                    .build();

            milvusClient.insert(insertParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<List<Float>> queryVectors(String collectionName, List<Float> queryVector, int topK) {
        try {
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withTopK(topK)
                    .withVectors(queryVector)
                    .build();

            SearchResults searchResults = milvusClient.search(searchParam);
            return searchResults.getResults();
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }
}
