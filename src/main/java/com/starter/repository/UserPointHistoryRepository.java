package com.starter.repository;

import com.starter.entity.UserPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointHistoryRepository extends JpaRepository<UserPointHistory, Long> {
    UserPointHistory findTopByUserIdOrderByCreatedAtDesc(Long userId);
} 