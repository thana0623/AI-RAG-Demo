package com.demo.rag.common;

/**
 * 统一错误码枚举
 * 定义标准错误码及对应的中文错误消息
 */
public enum ErrorCode {

    // ========== 通用错误码 ==========
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== 认证模块错误码 ==========
    EMAIL_ALREADY_REGISTERED(1001, "该邮箱已被注册"),
    EMAIL_NOT_FOUND(1002, "该邮箱未注册"),
    INVALID_VERIFICATION_CODE(1003, "验证码无效或已过期"),
    USERNAME_OR_EMAIL_EXISTS(1004, "用户名或邮箱已存在"),
    INVALID_CREDENTIALS(1005, "用户名/邮箱或密码错误"),
    USER_NOT_FOUND(1006, "用户不存在"),
    INVALID_TOKEN(1007, "Token 无效或已过期"),

    // ========== RAG 模块错误码 ==========
    CONTENT_REQUIRED(2001, "文档内容不能为空"),
    QUESTION_REQUIRED(2002, "提问内容不能为空"),
    DOCUMENT_NOT_FOUND(2003, "文档不存在"),
    VECTORIZATION_FAILED(2004, "文档向量化处理失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
