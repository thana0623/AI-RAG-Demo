package com.demo.rag.model.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String code; // 邮箱验证码
}