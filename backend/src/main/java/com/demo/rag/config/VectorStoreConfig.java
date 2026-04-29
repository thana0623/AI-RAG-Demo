package com.demo.rag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量库配置
 * 使用内存向量库（SimpleVectorStore）代替复杂配置，支持快速演示 RAG 功能
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("初始化内存向量库（SimpleVectorStore）");
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
