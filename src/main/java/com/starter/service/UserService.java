package com.starter.service;

import com.starter.dto.UserRegistrationDto;
import com.starter.entity.User;
import com.starter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 회원가입, 로그인, 사용자 정보 관리 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본값을 읽기 전용으로 설정
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * SHA-256 알고리즘을 사용하여 비밀번호를 암호화하는 메서드
     * @param password 암호화할 원본 비밀번호
     * @return 암호화된 비밀번호 (Base64 인코딩)
     */
    private String encodePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("비밀번호 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 입력된 비밀번호와 저장된 암호화된 비밀번호가 일치하는지 확인하는 메서드
     * @param rawPassword 사용자가 입력한 원본 비밀번호
     * @param encodedPassword DB에 저장된 암호화된 비밀번호
     * @return 비밀번호가 일치하면 true, 아니면 false
     */
    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        String encodedRawPassword = encodePassword(rawPassword);
        return encodedRawPassword.equals(encodedPassword);
    }

    /**
     * 새로운 사용자 회원가입을 처리하는 메서드
     * 중복 검사, 비밀번호 확인, 약관 동의 확인 후 사용자를 등록
     * @param registrationDto 회원가입 정보 DTO
     * @return 등록된 사용자 엔티티
     * @throws IllegalArgumentException 중복된 이메일/닉네임, 비밀번호 불일치, 약관 미동의 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 오버라이드
    public User registerUser(UserRegistrationDto registrationDto) {
        // 이메일 중복 확인
        if (userRepository.existsByUserEmail(registrationDto.getUserEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByUserNickname(registrationDto.getUserNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 확인
        if (!registrationDto.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 약관 동의 확인
        if (!registrationDto.isTermsAgreed()) {
            throw new IllegalArgumentException("약관에 동의해야 합니다.");
        }

        // User 엔티티 생성
        String encodedPassword = encodePassword(registrationDto.getUserPassword());

        User user = User.builder()
                .userEmail(registrationDto.getUserEmail())
                .userPassword(encodedPassword)
                .userNickname(registrationDto.getUserNickname())
                .userPhone(registrationDto.getUserPhone())
                .userStatus("active")
                .userFailedLogin(0)
                .userCommentTime(6)
                .userTokenCount(0)
                .build();

        // DB에 저장
        return userRepository.save(user);
    }

    /**
     * 이메일로 활성 상태의 사용자를 조회하는 메서드
     * @param email 조회할 사용자의 이메일
     * @return 사용자 정보 (Optional)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findActiveUserByEmail(email);
    }

    /**
     * 이메일 중복 여부를 확인하는 메서드
     * @param email 확인할 이메일
     * @return 중복되면 true, 아니면 false
     */
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByUserEmail(email);
    }

    /**
     * 닉네임 중복 여부를 확인하는 메서드
     * @param nickname 확인할 닉네임
     * @return 중복되면 true, 아니면 false
     */
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByUserNickname(nickname);
    }

    /**
     * 사용자 로그인을 처리하는 메서드
     * 비밀번호 검증, 로그인 실패 횟수 관리, 마지막 로그인 시간 업데이트
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 로그인 성공한 사용자 정보
     * @throws IllegalArgumentException 존재하지 않는 사용자 또는 잘못된 비밀번호 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 오버라이드
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findActiveUserByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        User user = userOpt.get();

        // 비밀번호 확인
        if (!matchesPassword(password, user.getUserPassword())) {
            user.incrementFailedLogin();
            userRepository.save(user);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 실패 횟수 초기화 및 마지막 로그인 시간 업데이트
        user.resetFailedLogin();
        user.updateLastLogin();

        return userRepository.save(user);
    }

    /**
     * 사용자를 소프트 삭제하는 메서드
     * 실제 데이터는 삭제하지 않고 상태만 변경
     * @param userId 삭제할 사용자 ID
     * @throws IllegalArgumentException 존재하지 않는 사용자 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 오버라이드
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.softDelete();
        userRepository.save(user);
    }

    /**
     * 사용자에게 토큰을 추가하는 메서드
     * @param userId 토큰을 추가할 사용자 ID
     * @param count 추가할 토큰 개수
     * @throws IllegalArgumentException 존재하지 않는 사용자 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 오버라이드
    public void addTokens(Long userId, int count) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.addTokens(count);
        userRepository.save(user);
    }

    /**
     * 사용자의 토큰을 사용하는 메서드
     * @param userId 토큰을 사용할 사용자 ID
     * @return 토큰 사용 성공 시 true, 토큰 부족 시 false
     * @throws IllegalArgumentException 존재하지 않는 사용자 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 오버라이드
    public boolean useToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean used = user.useToken();
        if (used) {
            userRepository.save(user);
        }

        return used;
    }
}