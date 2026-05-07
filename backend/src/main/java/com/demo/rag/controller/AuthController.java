package com.demo.rag.controller;

import com.demo.rag.common.BusinessException;
import com.demo.rag.common.ErrorCode;
import com.demo.rag.model.entity.User;
import com.demo.rag.model.request.LoginRequest;
import com.demo.rag.model.request.RegisterRequest;
import com.demo.rag.model.request.ResetPasswordRequest;
import com.demo.rag.model.request.SendCodeRequest;
import com.demo.rag.model.response.Result;
import com.demo.rag.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册、登录、验证码发送、密码重置等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求，邮箱：{}，类型：{}", request.getEmail(), request.getType());
        authService.sendVerificationCode(request);
        return Result.success("验证码发送成功", null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求，邮箱：{}，用户名：{}", request.getEmail(), request.getUsername());
        authService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求，标识：{}", request.getIdentifier());
        String token = authService.login(request);
        return Result.success("登录成功", token);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到重置密码请求，邮箱：{}", request.getEmail());
        authService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<User> currentUser(jakarta.servlet.http.HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("currentUserId");
        if (userIdAttr == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        User user = authService.getUserInfoById((Long) userIdAttr);
        user.setPassword(null); // 不返回密码哈希
        return Result.success(user);
    }
}
