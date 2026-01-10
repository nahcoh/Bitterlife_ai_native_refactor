package com.starter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Diary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public void setUser(User user) {
        this.user = user;
    }

    // ===================== NEW ENTITY FIELD ADDED =====================
    // 2025-01-XX: 감정 표현 기능 추가를 위한 emotion 필드 추가
    // 사용자가 일기 작성 시 선택한 감정 이모지를 저장
    // 예시: 😊(행복), 😢(슬픔), 😡(화남), 😌(평온), 🤔(고민), 😴(피곤), 😍(사랑), 😤(스트레스)
    @Column(length = 10)
    private String emotion; // 감정 이모지 저장 (예: 😊, 😢, 😡 등)
    // ===================== END NEW ENTITY FIELD =====================


    // ===================== TEMPORARY FIELD FOR DATABASE COMPATIBILITY =====================
    // 2025-01-XX: 데이터베이스 스키마 호환성을 위한 임시 필드
    // 실제로는 사용하지 않지만, 데이터베이스 스키마와 맞추기 위해 유지
    @Column(nullable = true)
    private String appliedStamp;
    // ===================== END TEMPORARY FIELD =====================
}