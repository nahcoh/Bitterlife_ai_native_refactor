package com.starter.repository;

import com.starter.entity.FeedbackProof;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackProofRepository extends JpaRepository<FeedbackProof, Long> {

    //1. 특정 주간에 프드백에 연결된 모든 증거(일기 등)를 가져올 때!
    List<FeedbackProof> findByFeedbackId(Long feedbackId);

    //2. 특정 일기가 이미 피드백 증거로 제출되었는지 확인할 때
    boolean existsByDiaryId(Long diaryId);

    //3. 특정 유저의 모든 증거 자료를 긁어올 때 (조금 복잡한 경로)
    List<FeedbackProof> findByFeedbackUserId(Long userId);
}