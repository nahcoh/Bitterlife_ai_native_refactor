package com.starter.repository;

import com.starter.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 이메일 중복 확인
    boolean existsByEmail(String email);
    
    // 카카오 ID로 사용자 찾기
    Optional<User> findByKakaoId(String kakaoId);
    
    // 활성 사용자만 찾기
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
    
    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
} 