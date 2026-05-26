package com.demo.rag.service;

import com.demo.rag.model.entity.User;
import com.demo.rag.model.request.LoginRequest;
import com.demo.rag.model.request.RegisterRequest;
import com.demo.rag.model.request.ResetPasswordRequest;
import com.demo.rag.model.request.SendCodeRequest;

public interface AuthService {
    void sendVerificationCode(SendCodeRequest request);
    void register(RegisterRequest request);
    String login(LoginRequest request);
    void resetPassword(ResetPasswordRequest request);
    User getUserInfo(String token);
    User getUserInfoById(Long userId);
}