package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "weekly_feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyFeedback extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean isQualified;

    @Column(columnDefinition = "TEXT")
    private String emotionSummary;


    private int weekOffset;

    private LocalDate feedbackStart;

    private LocalDate feedbackEnd;


    @Builder.Default
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<FeedbackProof> feedbackProofs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<RecommendActivity> recommendActivities = new ArrayList<>();

    //양방향 연관관계 편의 메서드
    public void addProof(FeedbackProof proof) {
        this.feedbackProofs.add(proof);
        proof.setFeedback(this);
    }
}
