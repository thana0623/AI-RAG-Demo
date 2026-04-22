package com.demo.rag.service.impl;

import com.demo.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void vectorizeText(String content, String docId) {
        try {
            log.info("Processing Document ID: {}", docId);
            redisTemplate.opsForValue().set("doc_status:" + docId, "PROCESSING");
            
            Document document = new Document(content, Map.of("docId", docId));
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.apply(List.of(document));
            
            vectorStore.add(splitDocs);
            
            redisTemplate.opsForValue().set("doc_status:" + docId, "SUCCESS");
            log.info("Document vectorization completed: {}", docId);
        } catch (Exception e) {
            redisTemplate.opsForValue().set("doc_status:" + docId, "FAILED");
            log.error("Vectorization failed for Document ID {}: {}", docId, e.getMessage());
        }
    }

    @Override
    public String askQuestion(String question) {
        String cacheKey = "qa_cache:" + question;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            log.info("Hit Redis Cache for question: {}", question);
            return cached;
        }

        // 1. 从向量库查召回知识
        List<Document> documents = vectorStore.similaritySearch(question);
        String context = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));

        // 2. 拼接提示词
        String systemPrompt = "你是一个使用 RAG 架构的知识问答助手。请仅根据下列内容回答问题：\n" + context;
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(question)
        ));

        // 3. 生成回答
        String answer = chatClient.call(prompt).getResult().getOutput().getContent();
        
        // 存入 Redis 缓存10分钟
        redisTemplate.opsForValue().set(cacheKey, answer, 10, TimeUnit.MINUTES);
        
        return answer;
    }
}