package com.starter.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * 스탬프 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StampDto {
    private Long stampId;
    private String name;
    private String image;
    private String qualification;
    private int price;
    private String description;
    private String status;
    private LocalDateTime salesAt;
    private LocalDateTime salesEnd;
} 