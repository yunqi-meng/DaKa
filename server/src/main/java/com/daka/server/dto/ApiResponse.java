package com.daka.server.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code = 0;
    private String message = "success";
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        return r;
    }
}