package com.starter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "weekly_feedback")
public class WeeklyFeedback extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_qualified")
    private String isQualified;

    @Column(name = "emotion_summary", columnDefinition = "TEXT")
    private String emotionSummary;

    private int weekOffset;

    @Column(name = "feedback_start")
    private String feedbackStart;

    @Column(name = "feedback_end")
    private String feedbackEnd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<FeedbackProof> feedbackProofs = new ArrayList<>();

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<RecommendActivity> recommendActivities = new ArrayList<>();
}
