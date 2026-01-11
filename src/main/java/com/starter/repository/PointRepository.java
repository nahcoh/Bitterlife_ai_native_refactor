package com.starter.repository;

import com.starter.entity.Point;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {

    //1. 잔액 조회(가장 최근 내역 1개)
    Optional<Point> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    //2. 포인트 적립
    boolean existByUserId(Long userId);

    //3. 사용내역(최신것부터)
    List<Point> findByUserIdOrderByCreatedAtDesc(Long userId);
} 