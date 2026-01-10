package com.starter.controller;

import com.starter.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class WeeklyReportPageController {

    // "/report" 경로로 GET 요청이 오면 감정 리포트 페이지 뷰를 반환
    @GetMapping("/report")
    public String showReportPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/?loginRequired=true";
        }
        model.addAttribute("user", user);
        model.addAttribute("contentPath", "report");
        return "layout/base";
    }
}