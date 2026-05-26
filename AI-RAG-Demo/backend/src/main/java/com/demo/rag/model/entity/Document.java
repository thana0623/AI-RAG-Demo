package com.demo.rag.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文档实体
 * 记录上传文档的元数据及向量化状态
 */
@Data
@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档唯一标识（UUID） */
    @Column(nullable = false, unique = true, length = 36)
    private String docId;

    /** 文档原始内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 向量化状态：PENDING / PROCESSING / SUCCESS / FAILED */
    @Column(nullable = false, length = 20)
    private String status;

    /** 切块数量（向量化完成后更新） */
    private Integer chunkCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
