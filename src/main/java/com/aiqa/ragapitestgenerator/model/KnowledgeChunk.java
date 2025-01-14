package com.aiqa.ragapitestgenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class KnowledgeChunk {
    private List<Float> embedding;
    private String documentType;
    private Long documentId;
    private Long chunkId;

    @Override
    public String toString() {
        return "KnowledgeChunk{" +
                ", embedding='" + embedding + '\'' +
                ", documentType='" + documentType + '\'' +
                ", documentId='" + documentId + '\'' +
                ", chunkId=" + chunkId +
                '}';
    }
}
