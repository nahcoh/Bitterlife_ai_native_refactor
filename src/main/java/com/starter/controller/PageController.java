package com.starter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    // 일기 달력 페이지 매핑 (헤더 링크용)
    @GetMapping("/diary")
    public String diary() {
        return "redirect:/diary-calendar";
    }

    // AI와 채팅하는 페이지 매핑
    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String diaryDate,
                       Model model) {
        // 채팅 페이지에 일기 정보 전달
        model.addAttribute("title", "선생님과 채팅");
        model.addAttribute("contentPath", "chat");
        model.addAttribute("userId", userId);
        model.addAttribute("diaryDate", diaryDate);
        return "layout/base";
    }
}
