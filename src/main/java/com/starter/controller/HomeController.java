package com.starter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈페이지 관련 컨트롤러
 * 메인 페이지 렌더링을 처리
 */
@Controller
public class HomeController {

    /**
     * 메인 페이지를 렌더링하는 메서드
     * @param model 뷰에 전달할 데이터 모델
     * @return 메인 페이지 템플릿 경로
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "메인 페이지");
        model.addAttribute("contentPath", "index");
        return "layout/base";
    }
}