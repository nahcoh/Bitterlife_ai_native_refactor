package com.starter.repository;

import com.starter.entity.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 지정된 날짜 범위(start ~ end)에 작성된 일일 코멘트를 조회
public interface DailyCommentRepository extends JpaRepository<DailyComment, Long> {

    List<DailyComment> findByUserIdAndDiaryDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    //1건 조회는 NPE대비 옵셔널
    Optional<DailyComment> findByDiary(Diary diary);

    //최신 코멘트 하나 가져오기
    Optional<DailyComment> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    List<DailyComment> findByUserId(Long userId);

    // ===================== UPDATED QUERY METHOD =====================
    // 2025-01-XX: 달력 조회를 위한 월별 코멘트 조회 메서드 수정
    // 특정 월의 코멘트를 조회하여 UserStamp 정보와 함께 반환
    // :userId -> 콜론: 이름기반 파라미터 바인딩
    @Query("SELECT dc FROM DailyComment dc " +
        "LEFT JOIN FETCH dc.userStamp us " +
        "JOIN FETCH us.stamp " +
        "WHERE dc.user.id = :userId " +
        "AND YEAR(dc.diaryDate) = :year " +
        "AND MONTH(dc.diaryDate) = :month")
    List<DailyComment> findByUserAndYearMonthWithStamp(@Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month);
    // ===================== END UPDATED QUERY METHOD =====================
}
