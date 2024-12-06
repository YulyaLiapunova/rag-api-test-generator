package com.aiqa.ragapitestgenerator;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/milvus")
public class MilvusController {

    private final MilvusService milvusService;

    public MilvusController(MilvusService milvusService) {
        this.milvusService = milvusService;
    }

    @PostMapping("/create")
    public boolean createCollection(@RequestBody CollectionRequest request) {
        return milvusService.createCollection(request.getCollectionName(), request.getFields());
    }

    @PostMapping("/insert")
    public String insertVectors(@RequestParam String collectionName, @RequestBody List<List<Float>> vectors) {
        milvusService.insertVectors(collectionName, vectors);
        return "Vectors inserted successfully!";
    }

    @PostMapping("/query")
    public List<List<Float>> queryVectors(@RequestParam String collectionName,
                                          @RequestParam List<Float> queryVector,
                                          @RequestParam int topK) {
        return milvusService.queryVectors(collectionName, queryVector, topK);
    }
}

