package com.starter.controller;

import com.starter.dto.UserRegistrationDto;
import com.starter.entity.User;
import com.starter.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 인증 관련 컨트롤러
 * 회원가입, 로그인, 로그아웃 기능을 처리
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 회원가입을 처리하는 메서드
     * @param registrationDto 회원가입 정보 DTO
     * @param session HTTP 세션
     * @return 성공 시 메인 페이지로 리다이렉트, 실패 시 에러 페이지로 리다이렉트
     */
    @PostMapping("/register")
    public String register(@ModelAttribute UserRegistrationDto registrationDto, HttpSession session) {
        try {
            User registeredUser = userService.registerUser(registrationDto);
            // 회원가입 성공 시 세션에 사용자 정보 저장
            session.setAttribute("user", registeredUser);
            session.setMaxInactiveInterval(3600); // 1시간 세션 유지
            return "redirect:/?registered=true";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }

    /**
     * 사용자 로그인을 처리하는 메서드
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @param session HTTP 세션
     * @return 성공 시 메인 페이지로 리다이렉트, 실패 시 에러 페이지로 리다이렉트
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String password,
                       HttpSession session) {
        try {
            User user = userService.login(email, password);
            // 로그인 성공 시 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(3600); // 1시간 세션 유지
            return "redirect:/?login=true";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }

    /**
     * 사용자 로그아웃을 처리하는 메서드
     * @param session HTTP 세션
     * @return 메인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // 세션에서 사용자 정보 제거
        session.removeAttribute("user");
        session.invalidate();
        return "redirect:/";
    }


}