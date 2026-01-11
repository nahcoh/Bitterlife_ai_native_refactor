package com.starter.repository;

import com.starter.entity.Stamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {

    //1. 판매 중인 상품 목록
    List<Stamp> findByStatus(Stamp.SaleStampStatus status);

    //2. 이름으로 검색
    Optional<Stamp> findByNameContaining(String name);

    //3. 신상품순 정렬
    List<Stamp> findAllByOrderByCreatedAtDesc();

    //4. 한정판 조회
    List<Stamp> findBySalesEndAfter(LocalDateTime now);

} 