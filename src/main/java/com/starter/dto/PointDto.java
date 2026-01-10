package com.starter.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * 포인트 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDto {
    private Long pointId;
    private String pointName;
    private int pointAmount;
    private String reason;
    private LocalDateTime createdAt;
} 