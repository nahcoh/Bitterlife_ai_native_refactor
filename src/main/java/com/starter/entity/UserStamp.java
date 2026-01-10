package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stamp")
public class UserStamp extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userStampId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long stampId;

    @Column(length = 1)
    private String isActive; // Y:적용중 / N:비적용중

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getter/Setter
    public Long getUserStampId() { return userStampId; }
    public void setUserStampId(Long userStampId) { this.userStampId = userStampId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getStampId() { return stampId; }
    public void setStampId(Long stampId) { this.stampId = stampId; }
    public String getIsActive() { return isActive; }
    public void setIsActive(String isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 