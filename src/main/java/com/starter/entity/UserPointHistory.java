package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_point_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserPointHistory extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int beforePoint;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int afterPoint;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;


    @Builder    //클래스 위가 아니라 생성자 위에 붙임
    public UserPointHistory(User user, int beforePoint, int amount, int afterPoint, String reason) {
        //정합성 체크: 계산이 맞는지 확인
        if (beforePoint + amount != afterPoint) {
            throw new IllegalArgumentException("포인트 계산 정합성이 맞지 않습니다.! (기존 + 변동 != 이후)");
        }
        this.user = user;
        this.beforePoint = beforePoint;
        this.amount = amount;
        this.afterPoint = afterPoint;
        this.reason = reason;
    }

} 