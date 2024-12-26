package com.aiqa.ragapitestgenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class KnowledgeChunk {
    private Integer id;
    private List<Float> embedding;
    private String documentId;
    private String chunkId;

    @Override
    public String toString() {
        return "KnowledgeChunk{" +
                "id='" + id + '\'' +
                ", embedding='" + embedding + '\'' +
                ", documentId='" + documentId + '\'' +
                ", chunkId=" + chunkId +
                '}';
    }
}
