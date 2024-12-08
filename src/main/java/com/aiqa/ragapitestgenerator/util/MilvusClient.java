package com.aiqa.ragapitestgenerator.util;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MilvusClient {
    private static final Logger logger = LoggerFactory.getLogger(MilvusClient.class);
    private static final String COLLECTION_NAME = "knowledge_base";

    private final MilvusClientV2 milvusClient;

    @Value("${milvus.host}")
    private String milvusHost;

    public MilvusClient() {
        this.milvusClient = new MilvusClientV2(ConnectConfig.builder()
                .uri(milvusHost) // "http://localhost:19530"
                .build());;
    }

    public void createCollection(String collectionName, List<String> fields) {
        // Logic to create collection with schema in Milvus
    }

    public void insertEmbedding(String chunk) {
        // Logic to generate embedding and insert into Milvus
    }

    public void queryVectors(List<Float> queryVector, int topK) {
        // Logic to query Milvus
    }
}
