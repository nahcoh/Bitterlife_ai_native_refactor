package com.starter.controller;

import com.starter.dto.ReportResponseDto;
import com.starter.entity.WeeklyFeedback;
import com.starter.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    // 특정 사용자(userId)의 주차 오프셋(weekOffset)에 해당하는 주간 리포트 데이터를 반환
    @GetMapping
    public ResponseEntity<ReportResponseDto> getWeeklyReport(
            @RequestParam Long userId,
            @RequestParam int weekOffset
    ) {
        ReportResponseDto responseDto = weeklyReportService.getWeeklyReport(userId, weekOffset);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 사용자가 작성한 모든 주간 리포트의 weekOffset 목록을 최신 순으로 반환
    @GetMapping("/weeks")
    public ResponseEntity<List<Integer>> getAvailableOffsets(@RequestParam Long userId) {
        List<WeeklyFeedback> feedbacks = weeklyReportService.getAllFeedbacks(userId);
        List<Integer> offsets = feedbacks.stream()
                .map(WeeklyFeedback::getWeekOffset)
                .sorted(Comparator.reverseOrder()) // 최신 주차부터
                .toList();
        return ResponseEntity.ok(offsets);
    }
}