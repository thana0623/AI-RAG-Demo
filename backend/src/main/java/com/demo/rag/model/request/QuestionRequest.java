package com.demo.rag.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 2000, message = "提问内容不能超过 2000 字符")
    private String question;
}
