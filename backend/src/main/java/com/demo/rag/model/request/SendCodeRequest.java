package com.demo.rag.model.request;

import lombok.Data;

@Data
public class SendCodeRequest {
    private String email;
    // REGISTER 或者 RESET
    private String type; 
}