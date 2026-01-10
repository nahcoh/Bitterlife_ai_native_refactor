package com.starter.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * 사용자의 포인트 이력 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPointHistoryDto {
    private Long userPointHistoryId;
    private Long userId;
    private int beforePoint;
    private int amount;
    private int afterPoint;
    private String reason;
    private LocalDateTime createdAt;
} 