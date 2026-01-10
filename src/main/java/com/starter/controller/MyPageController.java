package com.starter.controller;

import com.starter.service.MyPageService;
import com.starter.dto.MyPageSummaryDto;
import com.starter.entity.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    // 마이페이지 매핑
    @GetMapping
    public String myPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/?loginRequired=true";
        }
        model.addAttribute("title", "마이페이지");
        model.addAttribute("contentPath", "myPage");
        MyPageSummaryDto summary = myPageService.getMyPageSummary(user);
        model.addAttribute("summary", summary);
        return "layout/base";
    }

    // 코멘트 받을 시간 업데이트
    @PostMapping("/comment-time")
    public String updateCommentTime(@RequestParam int commentHour, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/?loginRequired=true";
        }
        if (user.getUserCommentTime() == commentHour) {
            return "redirect:/mypage?same=true";
        }
        myPageService.updateCommentTime(user, commentHour);
        user.setUserCommentTime(commentHour);
        session.setAttribute("user", user);
        return "redirect:/mypage?saved=true";
    }
}