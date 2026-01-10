package com.starter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API 응답 표준 포맷을 제공하는 DTO 클래스
 * @param <T> 응답 데이터 타입
 */
public class ApiResponse<T> {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;

    public ApiResponse() {}
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    @JsonProperty("success")
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    @JsonProperty("message")
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    @JsonProperty("data")
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
} 