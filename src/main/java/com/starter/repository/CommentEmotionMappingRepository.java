package com.starter.repository;

import com.starter.entity.CommentEmotionMapping;
import com.starter.entity.CommentEmotionId;
import com.starter.entity.DailyComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentEmotionMappingRepository extends JpaRepository<CommentEmotionMapping, CommentEmotionId> {
    List<CommentEmotionMapping> findByDailyCommentIn(List<DailyComment> comments);
    List<CommentEmotionMapping> findByDailyComment(DailyComment dailyComment);
}
