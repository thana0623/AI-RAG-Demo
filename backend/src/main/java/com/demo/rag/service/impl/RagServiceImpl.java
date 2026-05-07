package com.demo.rag.service.impl;

import com.demo.rag.common.BusinessException;
import com.demo.rag.common.ErrorCode;
import com.demo.rag.repository.DocumentRepository;
import com.demo.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
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

/**
 * RAG 服务实现
 * 处理文档向量化和知识问答业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final RedisTemplate<String, String> redisTemplate;
    private final DocumentRepository documentRepository;

    /** 问答缓存过期时间（分钟） */
    private static final long QA_CACHE_TTL_MINUTES = 10;

    @Override
    public void vectorizeText(String content, String docId) {
        try {
            log.info("开始处理文档向量化，docId：{}", docId);
            redisTemplate.opsForValue().set("doc_status:" + docId, "PROCESSING");
            updateDocumentStatus(docId, "PROCESSING", null);

            // 创建文档并切块
            Document document = new Document(content, Map.of("docId", docId));
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.apply(List.of(document));

            // 存入向量库
            vectorStore.add(splitDocs);

            redisTemplate.opsForValue().set("doc_status:" + docId, "SUCCESS");
            updateDocumentStatus(docId, "SUCCESS", splitDocs.size());
            log.info("文档向量化完成，docId：{}，切块数量：{}", docId, splitDocs.size());
        } catch (Exception e) {
            redisTemplate.opsForValue().set("doc_status:" + docId, "FAILED");
            updateDocumentStatus(docId, "FAILED", null);
            log.error("文档向量化失败，docId：{}，错误：{}", docId, e.getMessage(), e);
        }
    }

    /**
     * 更新文档实体的状态和切块数量
     */
    private void updateDocumentStatus(String docId, String status, Integer chunkCount) {
        documentRepository.findByDocId(docId).ifPresent(doc -> {
            doc.setStatus(status);
            if (chunkCount != null) {
                doc.setChunkCount(chunkCount);
            }
            documentRepository.save(doc);
        });
    }

    @Override
    public String askQuestion(String question) {
        String cacheKey = "qa_cache:" + question;

        // 1. 查 Redis 缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("命中问答缓存，问题：{}", question);
            return cached;
        }

        // 2. 从向量库检索相关知识
        log.info("缓存未命中，开始向量检索，问题：{}", question);
        List<Document> documents = vectorStore.similaritySearch(question);
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        if (context.isEmpty()) {
            log.warn("未检索到相关知识，问题：{}", question);
        }

        // 3. 拼接提示词并调用大模型
        String systemPrompt = "你是一个使用 RAG 架构的知识问答助手。请仅根据下列内容回答问题：\n" + context;
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(question)
        ));

        String answer = chatModel.call(prompt).getResult().getOutput().getText();

        // 4. 写入 Redis 缓存
        redisTemplate.opsForValue().set(cacheKey, answer, QA_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("问答生成完成，问题：{}", question);
        return answer;
    }
}
