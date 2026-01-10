package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_point_history")
public class UserPointHistory extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPointHistoryId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int beforePoint;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int afterPoint;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Getter/Setter
    public Long getUserPointHistoryId() { return userPointHistoryId; }
    public void setUserPointHistoryId(Long userPointHistoryId) { this.userPointHistoryId = userPointHistoryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getBeforePoint() { return beforePoint; }
    public void setBeforePoint(int beforePoint) { this.beforePoint = beforePoint; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getAfterPoint() { return afterPoint; }
    public void setAfterPoint(int afterPoint) { this.afterPoint = afterPoint; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 