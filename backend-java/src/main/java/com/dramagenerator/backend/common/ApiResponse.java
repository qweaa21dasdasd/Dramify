package com.dramagenerator.backend.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(new ApiError(code, message));
        return response;
    }

    @Data
    public static class ApiError {
        private int code;
        private String message;

        public ApiError(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
