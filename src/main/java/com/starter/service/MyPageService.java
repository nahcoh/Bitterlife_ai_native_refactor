package com.starter.service;

import com.starter.dto.MyPageSummaryDto;
import com.starter.entity.DailyComment;
import com.starter.entity.Diary;
import com.starter.entity.User;
import com.starter.entity.CommentEmotionMapping;
import com.starter.repository.CommentEmotionMappingRepository;
import com.starter.repository.DailyCommentRepository;
import com.starter.repository.DiaryRepository;
import com.starter.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final DiaryRepository diaryRepository;
    private final DailyCommentRepository dailyCommentRepository;
    private final CommentEmotionMappingRepository commentEmotionMappingRepository;
    private final UserRepository userRepository;
    private final PointshopService pointshopService;

    // 마이페이지 요약 정보 조회
    @Transactional
    public MyPageSummaryDto getMyPageSummary(User user) {
        int totalDiaryCount = diaryRepository.countDistinctDatesByUser(user);
        int consecutiveDiaryDays = calculateConsecutiveDiaryDays(user);
        DailyComment recentComment = dailyCommentRepository.findTopByUserOrderByCreatedAtDesc(user);
        List<String> mainEmotions = List.of();
        String recentCommentContent = null;
        String recentStampImage = null;
        
        if (recentComment != null) {
            // 감정 매핑 조회 및 감정명 추출
            List<CommentEmotionMapping> mappings = commentEmotionMappingRepository.findByDailyCommentIn(List.of(recentComment));
            mainEmotions = mappings.stream()
                .map(m -> m.getEmotionData().getName())
                .toList();
            recentCommentContent = recentComment.getContent();
        }
        
        // 현재 적용된 스탬프 정보 가져오기
        try {
            com.starter.dto.UserStampDto activeStamp = pointshopService.getActiveStamp(user.getUserId());
            if (activeStamp != null) {
                recentStampImage = activeStamp.getStampImage();
            } else {
                recentStampImage = "image/default_stamp.png";
            }
        } catch (Exception e) {
            recentStampImage = "image/default_stamp.png";
        }
        
        // mainEmotions를 '#행복 #피로' 형식의 1개 문자열 리스트로 가공
        String mainEmotionsStr = mainEmotions.stream()
            .map(e -> "#" + e)
            .reduce((a, b) -> a + " " + b)
            .orElse("");
        
        MyPageSummaryDto dto = new MyPageSummaryDto();
        dto.setNickname(user.getUserNickname());
        dto.setEmail(user.getUserEmail());
        dto.setJoinDate(user.getUserCreatedAt().toLocalDate());
        dto.setTotalDiaryCount(totalDiaryCount);
        dto.setConsecutiveDiaryDays(consecutiveDiaryDays);
        dto.setMainEmotions(List.of(mainEmotionsStr));
        dto.setRecentAiComment(recentCommentContent != null ? recentCommentContent : "AI 코멘트가 없습니다.");
        dto.setRecentStampImage(recentStampImage);
        dto.setCommentTime(user.getUserCommentTime());
        return dto;
    }

    // 어제까지 연속 일기 작성 일수 계산
    private int calculateConsecutiveDiaryDays(User user) {
        List<Diary> diaries = diaryRepository.findByUserOrderByCreatedAtDesc(user);
        if (diaries.isEmpty()) {
            return 0;
        }
        java.time.LocalDate yesterday = java.time.LocalDate.now().minusDays(1);
        int streak = 0;
        for (Diary diary : diaries) {
            java.time.LocalDate diaryDate = diary.getCreatedAt().toLocalDate();
            if (diaryDate.equals(yesterday.minusDays(streak))) {
                streak++;
            } else if (diaryDate.isBefore(yesterday.minusDays(streak))) {
                break;
            }
        }
        return streak;
    }

    // 코멘트 받을 시간 업데이트
    @Transactional
    public void updateCommentTime(User user, int commentHour) {
        user.setUserCommentTime(commentHour);
        userRepository.save(user);
    }
}
