package com.starter.repository;

import com.starter.entity.RecommendActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendActivityRepository extends JpaRepository<RecommendActivity, Long> {

    //1. 특정 유저의 모든 추천 활동 조회 (Feedback을 거쳐서!)
    List<RecommendActivity> findByFeedbackUserId(Long userId);

    //2. 유저가 추천 활동을 확인(checked)했는지 여부
    // 엔티티에 isChecked 필드가 있다고 가정할때
    boolean existsByFeedbackUserIdAndIsCheckedTrue(Long userId);

    //3. 최신 추천
    List<RecommendActivity> findTop3ByFeedbackUserIdOrderByCreatedAtDesc(Long userId);

} 