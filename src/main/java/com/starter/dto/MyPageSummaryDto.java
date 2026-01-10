package com.starter.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.*;

/**
 * 마이페이지 요약 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageSummaryDto {
    private String nickname;
    private String email;
    private LocalDate joinDate;
    private int totalDiaryCount;
    private int consecutiveDiaryDays;
    private List<String> mainEmotions;
    private String recentAiComment;
    private String recentStampImage;
    private int commentTime;
    private int point;
} 