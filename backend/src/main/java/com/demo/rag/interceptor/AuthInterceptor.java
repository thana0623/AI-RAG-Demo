package com.demo.rag.interceptor;

import com.demo.rag.common.ErrorCode;
import com.demo.rag.model.response.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 校验请求头中的 Bearer Token，从 Redis 解析当前用户 ID
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "USER_TOKEN:";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response);
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);

        if (userId == null) {
            log.warn("Token 无效或已过期：{}", token);
            writeUnauthorized(response);
            return false;
        }

        // 将当前用户 ID 存入 request 属性，供后续 Controller 使用
        request.setAttribute("currentUserId", Long.parseLong(userId));
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(ErrorCode.INVALID_TOKEN);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
