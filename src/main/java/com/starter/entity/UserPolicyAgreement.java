package com.starter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 정책 동의 정보를 담는 JPA 엔티티 클래스
 * 사용자가 특정 정책에 동의했는지 여부를 관리
 */
@Entity
@Table(name = "user_policy_agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPolicyAgreement extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @CreationTimestamp
    @Column(name = "agreement_agreed_at", nullable = false, updatable = false)
    private LocalDateTime agreementAgreedAt;
    
    @Column(name = "agreement_is_agreed", nullable = false, length = 1)
    @Builder.Default
    private String agreementIsAgreed = "N";
    
    /**
     * 사용자가 정책에 동의했는지 확인하는 메서드
     * @return 동의했으면 true, 아니면 false
     */
    public boolean isAgreed() {
        return "Y".equals(agreementIsAgreed);
    }
    
    /**
     * 사용자가 정책에 동의하는 메서드
     * 동의 상태를 'Y'로 변경하고 동의 시간을 현재 시간으로 설정
     */
    public void agree() {
        this.agreementIsAgreed = "Y";
        this.agreementAgreedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자가 정책에 동의하지 않는 메서드
     * 동의 상태를 'N'으로 변경
     */
    public void disagree() {
        this.agreementIsAgreed = "N";
    }
    
    /**
     * 연관된 사용자의 ID를 반환하는 메서드
     * @return 사용자 ID, 연관된 사용자가 없으면 null
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 연관된 정책의 ID를 반환하는 메서드
     * @return 정책 ID, 연관된 정책이 없으면 null
     */
    public Long getPolicyId() {
        return policy != null ? policy.getPolicyId() : null;
    }
} 