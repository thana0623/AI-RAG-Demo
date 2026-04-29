package com.demo.rag.service.impl;

import com.demo.rag.common.BusinessException;
import com.demo.rag.common.ErrorCode;
import com.demo.rag.model.entity.User;
import com.demo.rag.model.request.LoginRequest;
import com.demo.rag.model.request.RegisterRequest;
import com.demo.rag.model.request.ResetPasswordRequest;
import com.demo.rag.model.request.SendCodeRequest;
import com.demo.rag.repository.UserRepository;
import com.demo.rag.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 * 处理用户注册、登录、验证码、密码重置等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /** Redis 验证码缓存前缀 */
    private static final String CODE_PREFIX = "VERIFY_CODE:";
    /** Redis Token 缓存前缀 */
    private static final String TOKEN_PREFIX = "USER_TOKEN:";
    /** 密码加密盐值 */
    private static final String PASSWORD_SALT = "_rag_salt_here!";

    /**
     * 对密码进行 SHA-256 加盐哈希
     */
    private String encodePassword(String raw) {
        return DigestUtils.sha256Hex(raw + PASSWORD_SALT);
    }

    @Override
    public void sendVerificationCode(SendCodeRequest request) {
        String email = request.getEmail();
        String type = request.getType(); // REGISTER / RESET

        // 校验邮箱是否已被注册
        if ("REGISTER".equals(type) && userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        // 校验邮箱是否存在
        if ("RESET".equals(type) && userRepository.findByEmail(email).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_FOUND);
        }

        // 生成 6 位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 保存到 Redis，有效期 10 分钟
        redisTemplate.opsForValue().set(CODE_PREFIX + type + ":" + email, code, 10, TimeUnit.MINUTES);

        log.info("========================================");
        log.info("验证码发送 - 邮箱：{}，类型：{}，验证码：{}", email, type, code);
        log.info("========================================");

        // 发送邮件（如果 SMTP 未配置会失败，但验证码已存入 Redis）
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("您的验证码");
            message.setText("您在 " + type + " 操作中的验证码是：" + code + "\n验证码有效期为 10 分钟。");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("邮件发送失败（开发环境可忽略）：{}", e.getMessage());
        }
    }

    @Override
    public void register(RegisterRequest request) {
        String email = request.getEmail();
        String codeKey = CODE_PREFIX + "REGISTER:" + email;
        String cacheCode = redisTemplate.opsForValue().get(codeKey);

        // 校验验证码
        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 校验用户名或邮箱是否已存在
        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_OR_EMAIL_EXISTS);
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(request.getPassword()));
        userRepository.save(user);

        // 注册成功后删除验证码
        redisTemplate.delete(codeKey);

        log.info("用户注册成功，邮箱：{}，用户名：{}", email, request.getUsername());
    }

    @Override
    public String login(LoginRequest request) {
        // 根据邮箱或用户名查找用户
        Optional<User> optUser;
        if (request.getIdentifier().contains("@")) {
            optUser = userRepository.findByEmail(request.getIdentifier());
        } else {
            optUser = userRepository.findByUsername(request.getIdentifier());
        }

        User user = optUser.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 校验密码
        if (!user.getPassword().equals(encodePassword(request.getPassword()))) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 生成随机 Token，有效期 7 天
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getId()), 7, TimeUnit.DAYS);

        log.info("用户登录成功，标识：{}", request.getIdentifier());
        return token;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String codeKey = CODE_PREFIX + "RESET:" + email;
        String cacheCode = redisTemplate.opsForValue().get(codeKey);

        // 校验验证码
        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 更新密码
        user.setPassword(encodePassword(request.getNewPassword()));
        userRepository.save(user);

        // 重置成功后删除验证码
        redisTemplate.delete(codeKey);

        log.info("密码重置成功，邮箱：{}", email);
    }

    @Override
    public User getUserInfo(String token) {
        String userIdStr = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
