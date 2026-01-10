package com.starter.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "daily_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Column(nullable = false)
    private LocalDateTime diaryDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    // ===================== UPDATED ENTITY FIELD =====================
    // 2025-01-XX: 코멘트 작성 시 적용된 스탬프 정보를 UserStamp로 변경
    // 각 코멘트별로 다른 스탬프가 적용될 수 있도록 user_stamp의 user_stamp_id를 FK로 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_stamp_id")
    private UserStamp userStamp;

    //연관관계 편의 메서드
    public void setUser(User user) {
        this.user = user;
    }

    public void setDiary(Diary diary) {
        this.diary = diary;
    }
}
    // ===================== END UPDATED ENTITY FIELD =====================
