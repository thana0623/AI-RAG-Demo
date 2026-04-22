package com.demo.rag.controller;

import com.demo.rag.model.request.DocumentUploadRequest;
import com.demo.rag.model.request.QuestionRequest;
import com.demo.rag.model.response.AskResponse;
import com.demo.rag.model.response.DocumentStatusResponse;
import com.demo.rag.model.response.DocumentUploadResponse;
import com.demo.rag.model.response.Result;
import com.demo.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RagService ragService;

    @Value("${rag.mq.exchange}")
    private String exchange;

    @Value("${rag.mq.routing-key}")
    private String routingKey;

    /**
     * 将长文档导入，通过 RabbitMQ 异步向量化。
     * 用 Redis 返回任务状态。
     */
    @PostMapping("/document")
    public Result<DocumentUploadResponse> uploadDocument(@RequestBody DocumentUploadRequest request) {
        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            return Result.error("The 'content' field is required");
        }

        String docId = UUID.randomUUID().toString();
        
        // 压入 MQ 队列，由 MqConsumer 异步完成向量化
        rabbitTemplate.convertAndSend(exchange, routingKey, Map.of(
                "docId", docId,
                "content", content
        ));

        // 用 Redis 初始状态记为 PENDING
        redisTemplate.opsForValue().set("doc_status:" + docId, "PENDING");

        log.info("Document upload task submitted for docId: {}", docId);

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .docId(docId)
                .statusUrl("/api/rag/status/" + docId)
                .build();
                
        return Result.success("Document processing initialized asynchronously", response);
    }

    /**
     * 获取异步任务的状态 (查询 Redis)
     */
    @GetMapping("/status/{docId}")
    public Result<DocumentStatusResponse> getStatus(@PathVariable String docId) {
        String status = redisTemplate.opsForValue().get("doc_status:" + docId);
        
        DocumentStatusResponse response = DocumentStatusResponse.builder()
                .docId(docId)
                .status(status != null ? status : "NOT_FOUND")
                .build();
                
        return Result.success(response);
    }

    /**
     * 实时检索问答对话 (如果结果有 Redis 缓存，由 RagService 控制)
     */
    @PostMapping("/ask")
    public Result<AskResponse> askQuestion(@RequestBody QuestionRequest request) {
        String question = request.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            return Result.error("The 'question' field is required");
        }

        String answer = ragService.askQuestion(question);
        
        AskResponse response = AskResponse.builder()
                .question(question)
                .answer(answer)
                .build();
                
        return Result.success(response);
    }
}