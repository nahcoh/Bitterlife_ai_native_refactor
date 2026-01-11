package com.starter.repository;

import com.starter.entity.UserStamp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    Optional<UserStamp> findByUserIdAndStampId(Long userId, Long stampId);

    List<UserStamp> findAllByUserId(Long userId);

    Optional<UserStamp> findByUserIdAndStatus(Long userId, UserStamp.StampStatus status);
} 