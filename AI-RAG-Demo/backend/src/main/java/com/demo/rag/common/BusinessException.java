package com.demo.rag.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层抛出可被 GlobalExceptionHandler 统一捕获的业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + "：" + detail);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
