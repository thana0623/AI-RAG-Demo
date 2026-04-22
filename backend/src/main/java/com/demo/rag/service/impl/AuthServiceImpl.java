package com.demo.rag.service.impl;

import com.demo.rag.model.entity.User;
import com.demo.rag.model.request.LoginRequest;
import com.demo.rag.model.request.RegisterRequest;
import com.demo.rag.model.request.ResetPasswordRequest;
import com.demo.rag.model.request.SendCodeRequest;
import com.demo.rag.repository.UserRepository;
import com.demo.rag.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "VERIFY_CODE:";
    private static final String TOKEN_PREFIX = "USER_TOKEN:";

    private String encodePassword(String raw) {
        return DigestUtils.sha256Hex(raw + "_rag_salt_here!");
    }

    @Override
    public void sendVerificationCode(SendCodeRequest request) {
        String email = request.getEmail();
        String type = request.getType(); // REGISTER / RESET

        if ("REGISTER".equals(type) && userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }
        if ("RESET".equals(type) && userRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Email not found");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 保存到 Redis，有效期 10 分钟
        redisTemplate.opsForValue().set(CODE_PREFIX + type + ":" + email, code, 10, TimeUnit.MINUTES);

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your Verification Code");
        message.setText("Your verification code for " + type + " is: " + code + "\nIt will expire in 10 minutes.");
        
        javaMailSender.send(message);
    }

    @Override
    public void register(RegisterRequest request) {
        String email = request.getEmail();
        String codeKey = CODE_PREFIX + "REGISTER:" + email;
        String cacheCode = redisTemplate.opsForValue().get(codeKey);

        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(request.getCode())) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(request.getPassword()));
        userRepository.save(user);

        redisTemplate.delete(codeKey);
    }

    @Override
    public String login(LoginRequest request) {
        Optional<User> optUser;
        if (request.getIdentifier().contains("@")) {
            optUser = userRepository.findByEmail(request.getIdentifier());
        } else {
            optUser = userRepository.findByUsername(request.getIdentifier());
        }

        User user = optUser.orElseThrow(() -> new RuntimeException("Invalid username/email or password"));

        if (!user.getPassword().equals(encodePassword(request.getPassword()))) {
            throw new RuntimeException("Invalid username/email or password");
        }

        // 生成随机 Token 并发给前端
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getId()), 7, TimeUnit.DAYS);
        return token;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String codeKey = CODE_PREFIX + "RESET:" + email;
        String cacheCode = redisTemplate.opsForValue().get(codeKey);

        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(request.getCode())) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(encodePassword(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(codeKey);
    }

    @Override
    public User getUserInfo(String token) {
        String userIdStr = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userIdStr == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}