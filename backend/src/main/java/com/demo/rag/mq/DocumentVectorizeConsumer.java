package com.demo.rag.mq;

import com.demo.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文档向量化消息消费者
 * 监听 RabbitMQ 队列，异步处理文档向量化任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentVectorizeConsumer {

    private final RagService ragService;

    /**
     * 接收并处理文档向量化消息
     */
    @RabbitListener(queues = "${rag.mq.queue}")
    public void receiveMessage(Map<String, String> message) {
        String docId = message.get("docId");
        String content = message.get("content");

        if (docId != null && content != null) {
            log.info("收到文档向量化消息，docId：{}", docId);
            ragService.vectorizeText(content, docId);
        } else {
            log.warn("收到无效消息，缺少 docId 或 content，消息内容：{}", message);
        }
    }
}
