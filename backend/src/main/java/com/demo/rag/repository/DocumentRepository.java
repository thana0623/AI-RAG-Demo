package com.demo.rag.repository;

import com.demo.rag.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByDocId(String docId);
}
