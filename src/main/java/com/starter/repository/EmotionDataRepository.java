package com.starter.repository;

import com.starter.entity.EmotionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmotionDataRepository extends JpaRepository<EmotionData, Long> {
    Optional<EmotionData> findByName(String name);
} 