package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stamp_history")
public class UserStampHistory extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userStampHistoryId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long prevStampId;

    @Column(nullable = false)
    private Long newStampId;

    private LocalDateTime createdAt;

    // Getter/Setter
    public Long getUserStampHistoryId() { return userStampHistoryId; }
    public void setUserStampHistoryId(Long userStampHistoryId) { this.userStampHistoryId = userStampHistoryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPrevStampId() { return prevStampId; }
    public void setPrevStampId(Long prevStampId) { this.prevStampId = prevStampId; }
    public Long getNewStampId() { return newStampId; }
    public void setNewStampId(Long newStampId) { this.newStampId = newStampId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 