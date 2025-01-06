package com.aiqa.ragapitestgenerator.util;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    private static final String MILVUS_URL = "http://localhost:19530";

    @Bean
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig config = ConnectConfig.builder().uri(MILVUS_URL).build();
        return new MilvusClientV2(config);
    }
}
