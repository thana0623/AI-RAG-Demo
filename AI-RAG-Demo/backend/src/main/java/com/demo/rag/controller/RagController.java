package com.demo.rag.controller;

import com.demo.rag.model.entity.Document;
import com.demo.rag.model.request.DocumentUploadRequest;
import com.demo.rag.model.request.QuestionRequest;
import com.demo.rag.model.response.AskResponse;
import com.demo.rag.model.response.DocumentStatusResponse;
import com.demo.rag.model.response.DocumentUploadResponse;
import com.demo.rag.model.response.Result;
import com.demo.rag.repository.DocumentRepository;
import com.demo.rag.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * RAG 控制器
 * 处理文档上传、状态查询、知识问答等 RAG 相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RagService ragService;
    private final DocumentRepository documentRepository;

    @Value("${rag.mq.exchange}")
    private String exchange;

    @Value("${rag.mq.routing-key}")
    private String routingKey;

    /**
     * 上传文档，通过 RabbitMQ 异步向量化
     * 返回 docId 用于查询处理状态
     */
    @PostMapping("/document")
    public Result<DocumentUploadResponse> uploadDocument(@Valid @RequestBody DocumentUploadRequest request) {
        String content = request.getContent();
        String docId = UUID.randomUUID().toString();

        // 持久化文档元数据到 MySQL
        Document document = new Document();
        document.setDocId(docId);
        document.setContent(content);
        document.setStatus("PENDING");
        documentRepository.save(document);

        // 发送到消息队列，由消费者异步完成向量化
        rabbitTemplate.convertAndSend(exchange, routingKey, Map.of(
                "docId", docId,
                "content", content
        ));

        // Redis 初始状态标记为 PENDING（缓存加速查询）
        redisTemplate.opsForValue().set("doc_status:" + docId, "PENDING");

        log.info("文档上传任务已提交，docId：{}", docId);

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .docId(docId)
                .statusUrl("/api/rag/status/" + docId)
                .build();

        return Result.success("文档已提交，正在异步处理中", response);
    }

    /**
     * 查询文档向量化状态
     * 优先查 Redis 缓存，未命中则查 MySQL
     */
    @GetMapping("/status/{docId}")
    public Result<DocumentStatusResponse> getStatus(@PathVariable String docId) {
        String status = redisTemplate.opsForValue().get("doc_status:" + docId);

        if (status == null) {
            // Redis 未命中，从 MySQL 查询
            status = documentRepository.findByDocId(docId)
                    .map(Document::getStatus)
                    .orElse("NOT_FOUND");
        }

        DocumentStatusResponse response = DocumentStatusResponse.builder()
                .docId(docId)
                .status(status)
                .build();

        return Result.success(response);
    }

    /**
     * 知识问答
     * 先查 Redis 缓存，未命中则进行向量检索并调用大模型生成回答
     */
    @PostMapping("/ask")
    public Result<AskResponse> askQuestion(@Valid @RequestBody QuestionRequest request) {
        String question = request.getQuestion();
        log.info("收到问答请求：{}", question);
        String answer = ragService.askQuestion(question);

        AskResponse response = AskResponse.builder()
                .question(question)
                .answer(answer)
                .build();

        return Result.success(response);
    }
}
