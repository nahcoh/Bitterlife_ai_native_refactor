package com.starter.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEmotionMapping extends BaseTimeEntity{

    @EmbeddedId
    private CommentEmotionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")    //복합키 안의 commentId와 매핑
    @JoinColumn(name = "daily_comment_id")
    private DailyComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("emotionId")    //복합키 안의 commentId와 매핑
    @JoinColumn(name = "emotion_id")
    private EmotionData emotion; // 👈 이 필드가 있어야 getEmotion()가능
}
