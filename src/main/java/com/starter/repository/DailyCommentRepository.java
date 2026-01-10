package com.starter.repository;

import com.starter.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 지정된 날짜 범위(start ~ end)에 작성된 일일 코멘트를 조회
public interface DailyCommentRepository extends JpaRepository<DailyComment, Long> {
    List<DailyComment> findByUser_UserIdAndDiaryDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    DailyComment findByDiary(Diary diary);
    DailyComment findTopByUserOrderByCreatedAtDesc(User user);
    List<DailyComment> findByUser(User user);
    
    // ===================== UPDATED QUERY METHOD =====================
    // 2025-01-XX: 달력 조회를 위한 월별 코멘트 조회 메서드 수정
    // 특정 월의 코멘트를 조회하여 UserStamp 정보와 함께 반환
    @Query("SELECT dc FROM DailyComment dc " +
           "LEFT JOIN FETCH dc.userStamp us " +
           "WHERE dc.user.userId = :userId " +
           "AND YEAR(dc.diaryDate) = :year " +
           "AND MONTH(dc.diaryDate) = :month")
    List<DailyComment> findByUserAndYearMonthWithStamp(@Param("userId") Long userId, 
                                                      @Param("year") int year, 
                                                      @Param("month") int month);
    // ===================== END UPDATED QUERY METHOD =====================
}
