package com.demo.rag.service;

public interface RagService {
    
    /**
     * 处理文本并存入向量库。同时更新任务状态。
     * 
     * @param content 文档内容
     * @param docId 文档 ID
     */
    void vectorizeText(String content, String docId);

    /**
     * 根据提问进行 RAG 生成问答。
     * 
     * @param question 提问内容
     * @return 回答内容
     */
    String askQuestion(String question);
}