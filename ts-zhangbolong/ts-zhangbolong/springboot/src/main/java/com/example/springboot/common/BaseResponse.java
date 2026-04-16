package com.example.springboot.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用API响应结构
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "操作成功", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(200, message, data);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(500, message, null);
    }
}

