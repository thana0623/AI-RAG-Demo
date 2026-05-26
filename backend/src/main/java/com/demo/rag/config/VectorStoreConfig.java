package com.demo.rag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * 向量库配置
 * 使用 RedisVectorStore 实现向量持久化，重启后数据不丢失
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        log.info("初始化 Redis 向量库（RedisVectorStore）");
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("rag-doc-idx")
                .prefix("rag:doc:")
                .initializeSchema(true)
                .build();
    }
}
