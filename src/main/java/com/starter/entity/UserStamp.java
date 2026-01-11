package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_stamp")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class UserStamp extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stamp_id", nullable = false)
    private Stamp stamp;

    @Column(nullable = false)
    private StampStatus status = StampStatus.ACTIVE;

    public enum StampStatus {
        ACTIVE, INACTIVE
    }

    public void activate() {
        this.status = StampStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStamp.StampStatus.INACTIVE;
    }

} 