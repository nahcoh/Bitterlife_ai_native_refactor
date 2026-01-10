package com.starter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "recommend_activity")
public class RecommendActivity extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    private WeeklyFeedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Column(name = "activity_title")
    private String title;

    @Column(name = "activity_category")
    private String category;

    @Column(name = "activity_detail")
    private String detail;

    @Column(name = "activity_order")
    private Long order;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
