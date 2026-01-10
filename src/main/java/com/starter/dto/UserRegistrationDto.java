package com.starter.dto;

import lombok.*;

/**
 * 사용자 회원가입 정보를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationDto {
    
    private String userNickname;
    private String userEmail;
    private String userPassword;
    private String confirmPassword;
    private String userPhone;
    private String termsAgreed;
    
    // 비밀번호 확인 검증
    public boolean isPasswordMatching() {
        return userPassword != null && userPassword.equals(confirmPassword);
    }
    
    // 약관 동의 확인
    public boolean isTermsAgreed() {
        return termsAgreed != null && (termsAgreed.equals("true") || termsAgreed.equals("on"));
    }
    
    // 기본 유효성 검사
    public boolean isValid() {
        return userNickname != null && !userNickname.trim().isEmpty() &&
               userEmail != null && !userEmail.trim().isEmpty() &&
               userPassword != null && userPassword.length() >= 8 && userPassword.length() <= 20 &&
               userPhone != null && !userPhone.trim().isEmpty() &&
               isPasswordMatching() && isTermsAgreed();
    }
} 