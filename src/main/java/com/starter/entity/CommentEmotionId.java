package com.starter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CommentEmotionId implements Serializable  {
//복합키
// 한 일기(DailyComment)에는 똑같은 감정(EmotionData)이 여러 번 들어갈 필요가 없지?
//복합키의 정의: 그래서 [일기 ID + 감정 ID] 이 두 개를 묶어서 하나의 유일한 키로 쓰는 거야.
//    복합키 클래스 (CommentEmotionId): 자바에서는 PK가 하나여야 관리하기 편한데,
//    키가 두 개니까 이걸 하나로 묶어줄 **전용 가방(클래스)**을 만든 게 바로 그 복합키 클래스야.


    private Long commentId;

    private Long emotionId;
}

