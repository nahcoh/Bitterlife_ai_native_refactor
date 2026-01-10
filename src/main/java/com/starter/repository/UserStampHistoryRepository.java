package com.starter.repository;

import com.starter.entity.UserStampHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStampHistoryRepository extends JpaRepository<UserStampHistory, Long> {
    
    // 사용자별 스탬프 히스토리 조회 (최신순)
    List<UserStampHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 사용자의 가장 최근 스탬프 히스토리 조회
    @Query("SELECT ush FROM UserStampHistory ush WHERE ush.userId = :userId ORDER BY ush.createdAt DESC")
    List<UserStampHistory> findLatestByUserId(@Param("userId") Long userId);
    
    // 사용자의 가장 최근 스탬프 히스토리 하나만 조회
    @Query("SELECT ush FROM UserStampHistory ush WHERE ush.userId = :userId ORDER BY ush.createdAt DESC")
    UserStampHistory findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
} 