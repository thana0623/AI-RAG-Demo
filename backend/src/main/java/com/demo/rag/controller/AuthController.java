package com.demo.rag.controller;

import com.demo.rag.model.entity.User;
import com.demo.rag.model.request.LoginRequest;
import com.demo.rag.model.request.RegisterRequest;
import com.demo.rag.model.request.ResetPasswordRequest;
import com.demo.rag.model.request.SendCodeRequest;
import com.demo.rag.model.response.Result;
import com.demo.rag.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public Result<Void> sendVerificationCode(@RequestBody SendCodeRequest request) {
        try {
            authService.sendVerificationCode(request);
            return Result.success("Code sent successfully", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return Result.success("Registration successful", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return Result.success("Login successful", token);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return Result.success("Password reset successfully", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/current")
    public Result<User> currentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("No token provided");
            }
            String token = authHeader.substring(7);
            User user = authService.getUserInfo(token);
            user.setPassword(null); // Never return password hash
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}