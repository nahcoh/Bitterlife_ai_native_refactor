package com.starter.repository;

import com.starter.entity.RecommendActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendActivityRepository extends JpaRepository<RecommendActivity, Long> {
} 