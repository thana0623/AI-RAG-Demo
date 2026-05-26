package com.demo.rag.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentUploadRequest {
    @NotBlank(message = "文档内容不能为空")
    @Size(max = 100000, message = "文档内容不能超过 100000 字符")
    private String content;
}
