package com.demo.rag.model.response;

import com.demo.rag.common.ErrorCode;
import lombok.Data;

/**
 * 统一返回结果封装
 * 所有 Controller 接口均通过此类返回，保证前后端响应格式一致
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 仅带错误消息的失败返回（兼容旧调用，默认错误码 500）
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.INTERNAL_ERROR.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 带错误码和错误消息的失败返回
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 通过 ErrorCode 枚举构建失败返回
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }
}
