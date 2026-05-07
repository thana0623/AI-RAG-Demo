-- 初始化本地 MySQL 数据库与用户表
-- 执行方式：mysql -u<username> -p < backend/sql/init_mysql.sql

CREATE DATABASE IF NOT EXISTS rag_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE rag_demo;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_email (email),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document (
  id BIGINT NOT NULL AUTO_INCREMENT,
  doc_id VARCHAR(36) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  chunk_count INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_document_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
