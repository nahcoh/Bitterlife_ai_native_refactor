package com.starter.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * 사용자의 스탬프 획득/사용 이력 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStampHistoryDto {
    private Long userStampHistoryId;
    private Long userId;
    private Long prevStampId;
    private Long newStampId;
    private LocalDateTime createdAt;
} 