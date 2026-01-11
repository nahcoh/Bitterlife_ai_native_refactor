package com.starter.repository;

import com.starter.entity.CommentEmotionMapping;
import com.starter.entity.CommentEmotionId;
import com.starter.entity.DailyComment;
import com.starter.entity.EmotionData;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CommentEmotionMappingRepository extends JpaRepository<CommentEmotionMapping, CommentEmotionId> {

    //@EntitiyGraph: "야, 가져올 때 emotionData랑 JOIN해서 한방에 가져와! (N+1 방지)
    @EntityGraph(attributePaths = {"emotionData"})
    List<CommentEmotionMapping> findByDailyCommentIn(List<DailyComment> comments);


    @EntityGraph(attributePaths = {"emotionData"})
    List<CommentEmotionMapping> findByDailyComment(DailyComment dailyComment);
}
