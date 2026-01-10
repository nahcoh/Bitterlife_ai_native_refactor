package com.starter.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "recommend_activity")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendActivity extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    private WeeklyFeedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    private String title;

    private String category;

    private String detail;

    private Integer order;

    // 일대다. 나중에 피드백 저장시
    public void setFeedback(WeeklyFeedback feedback) {
        this.feedback =feedback;
        //피드백 객체 쪽 리스트에도 나를 추가해주는 센스
        if (!feedback.getRecommendActivities().contains(this)) {
            feedback.getRecommendActivities().add(this);
        }
    }
}
