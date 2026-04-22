package com.demo.rag.mq;

import com.demo.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentVectorizeConsumer {

    private final RagService ragService;

    @RabbitListener(queues = "${rag.mq.queue}")
    public void receiveMessage(Map<String, String> message) {
        String docId = message.get("docId");
        String content = message.get("content");
        
        if (docId != null && content != null) {
            log.info("Received vectorization message for docId: {}", docId);
            ragService.vectorizeText(content, docId);
        } else {
            log.warn("Invalid message received, missing docId or content. message: {}", message);
        }
    }
}