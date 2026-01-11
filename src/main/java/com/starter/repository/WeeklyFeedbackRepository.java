package com.starter.repository;

import com.starter.entity.WeeklyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyFeedbackRepository extends JpaRepository<WeeklyFeedback, Long> {


    //1. 단순 조회( 가장 기본 버튼)
    Optional<WeeklyFeedback> findByUserIdAndWeekOffset(Long userId, int weekOffset);

    //2. 증거 자료들까지 한방에 긁어오기 (N+1방지)
    @Query("SELECT wf FROM WeeklyFeedback wf " +
        "LEFT JOIN FETCH wf.feedbackProofs " +
        "WHERE wf.user.id = :userId AND wf.weekOffset = :weekOffset")
    Optional<WeeklyFeedback> findWithFeedbackPrrofsByUserIdAndWeekOffset(
        @Param("userId") Long userId, @Param("weekOffset") int weekOffset);


    //3. 추천 활동들까지 한방에 긁어오기 (N+1방지)
    @Query("SELECT wf FROM WeeklyFeedback wf " +
        "LEFT JOIN FETCH wf.recommendActivities " +
        "WHERE wf.user.id = :userId AND wf.weekOffset = :weekOffset")
    Optional<WeeklyFeedback> findWithRecommendActivitiesByUserIdAndWeekOffset(
        @Param("userId") Long userId, @Param("weekOffset") int weekOffset);


    //4. 특정 유저의 전체 피드백 목록 최신순
    List<WeeklyFeedback> findAllByUserIdOrderByCreatedAtDesc(Long userId);

}
