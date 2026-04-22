package com.demo.rag.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; //  could be email or username
    private String password;
}