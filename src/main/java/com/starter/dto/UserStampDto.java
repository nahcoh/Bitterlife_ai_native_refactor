package com.starter.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * 사용자 스탬프 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStampDto {
    private Long userStampId;
    private Long userId;
    private Long stampId;
    private String isActive; // "Y" or "N"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

     // 스탬프 정보 추가
     private String stampName;
     private String stampImage;
     private String stampDescription;
     private int stampPrice;
} 