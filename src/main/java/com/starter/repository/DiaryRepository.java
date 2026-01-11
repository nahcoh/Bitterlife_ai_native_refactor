package com.starter.repository;

import com.starter.entity.Diary;
import com.starter.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 1. 객체 대신 ID로 깔끔하게 조회
    List<Diary> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    //2. 최신 일기 한 건 NPE 방지용 옵셔널
    Optional<Diary> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    //3. 특정 유저의 모든 일기 최신순 조회
    List<Diary> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 4. 출석 체크용 쿼리 (중복 날짜 카운트)
    // SQL의 DATE() 함수는 JPQL에서 지원하지 않을 수 있음
    // 여기서는 데이터가 많지 않다면 LocalTime을 무시하고 처리하는 로직을 고민해야 함.
    // 같은 날짜의 일기를 중복 제거하여 카운트 (연속 기록 계산용)
    @Query(value = "SELECT COUNT(DISTINCT DATE(created_at)) FROM diary WHERE user_id = :userId", nativeQuery = true)
    int countDistinctDatesByUserId(@Param("userId") Long userId);
} 