package com.starter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 정보를 담는 JPA 엔티티 클래스
 * 사용자의 기본 정보, 인증 정보, 상태 정보를 관리
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stamp_id", nullable = false)
    private Stamp currentStamp;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer failedLogin = 0;


    @Column(nullable = false)
    @Builder.Default
    private Integer commentTime = 6;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer tokenCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 100)
    private String kakaoId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserPolicyAgreement> policyAgreements = new ArrayList<>();

    /**
     * 사용자가 활성 상태인지 확인하는 메서드
     * @return 활성 상태이면 true, 아니면 false
     */
    public boolean isActive() {
        return status.equals(UserStatus.ACTIVE) && deletedAt == null;
    }

    /**
     * 사용자가 정지 상태인지 확인하는 메서드
     * @return 정지 상태이면 true, 아니면 false
     */
    public boolean isSuspended() {
        return status.equals(UserStatus.SUSPENDED);
    }

    /**
     * 로그인 실패 횟수를 증가시키는 메서드
     */
    public void incrementFailedLogin() {
        this.failedLogin++;
    }

    /**
     * 로그인 실패 횟수를 초기화하는 메서드
     */
    public void resetFailedLogin() {
        this.failedLogin = 0;
    }



    /**
     * 사용자를 소프트 삭제하는 메서드
     * 상태를 'deleted'로 변경하고 삭제 시간을 기록
     */
    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 사용자에게 토큰을 추가하는 메서드
     * @param count 추가할 토큰 개수
     */
    public void addTokens(int count) {
        this.tokenCount += count;
    }

    /**
     * 사용자의 토큰을 사용하는 메서드
     * @return 토큰 사용 성공 시 true, 토큰 부족 시 false
     */
    public boolean useToken() {
        if (this.tokenCount > 0) {
            this.tokenCount--;
            return true;
        }
        return false;
    }

    /**
     * 사용자에게 정책 동의 정보를 추가하는 메서드
     * @param agreement 추가할 정책 동의 정보
     */
    public void addPolicyAgreement(UserPolicyAgreement agreement) {
        this.policyAgreements.add(agreement);
        agreement.setUser(this);
    }

    /**
     * 사용자가 특정 정책에 동의했는지 확인하는 메서드
     * @param policyId 확인할 정책 ID
     * @return 동의했으면 true, 아니면 false
     */
    public boolean hasAgreedToPolicy(Long policyId) {
        return policyAgreements.stream()
                .anyMatch(agreement -> agreement.getPolicyId().equals(policyId) && agreement.isAgreed());
    }

    /**
     * 유저의 현재 도장을 교체하는 메서드
     *
     * @param newStamp 새로 장착할 도장
     */
    public void changeStamp(Stamp newStamp) {
        this.currentStamp = newStamp;
    }
}

