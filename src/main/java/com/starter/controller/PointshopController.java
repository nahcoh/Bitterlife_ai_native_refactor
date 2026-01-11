package com.starter.controller;

import com.starter.dto.StampDto;
import com.starter.dto.UserStampDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.*;
import com.starter.entity.User;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pointshop")
@RequiredArgsConstructor
public class PointshopController {

    private final PointshopService pointshopService;


    // 1. 포인트샵 메인 페이지 렌더링
    @GetMapping
    public String pointshopPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return "redirect:/?loginRequired=true";
        // }
        if (user == null) {
            return "redirect:/?loginRequired=true";
        }

        int userPoint = pointshopService.getUserPoint(user.getId());
        model.addAttribute("title", "포인트샵");
        model.addAttribute("contentPath", "pointshop");
        model.addAttribute("userPoint", userPoint);
        return "layout/base";
    }

    // 2. 사용자 포인트 조회 (AJAX)
    @GetMapping("/api/points")
    @ResponseBody
    public int getUserPoints(HttpSession session) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return 0; // 로그인하지 않은 경우 0 반환
        // }
        return pointshopService.getUserPoint(user.getId());
    }

    // 3. 상점 도장 목록 조회 (AJAX)
    @GetMapping("/api/stamps")
    @ResponseBody
    public List<Map<String, Object>> getAvailableStamps(
        @RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return new ArrayList<>(); // 로그인하지 않은 경우 빈 리스트 반환
        // }

        Long userId = user.getId();
        int userPoint = pointshopService.getUserPoint(userId);
        List<StampDto> stamps = pointshopService.getAvailableStamps(userId);
        List<UserStampDto> myStamps = pointshopService.getMyStamps(userId);

        // 보유 도장 정보 맵핑 (stampId -> UserStampDto)
        Map<Long, UserStampDto> ownedStampMap = new HashMap<>();
        for (UserStampDto us : myStamps) {
            ownedStampMap.put(us.getStampId(), us);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (StampDto stamp : stamps) {
            String status;
            UserStampDto owned = ownedStampMap.get(stamp.getStampId());
            if (owned != null) {
                status = "owned"; // 1순위: 보유중
            } else if (userPoint < stamp.getPrice()) {
                status = "insufficient"; // 2순위: 포인트 부족
            } else {
                status = "buyable"; // 3순위: 구매 가능
            }
            // 필터 적용
            if (filter.equals("all") || filter.equals(status) ||
                (filter.equals("notowned") && owned == null)) {
                Map<String, Object> map = new HashMap<>();
                map.put("stamp", stamp);
                map.put("status", status);
                if (owned != null) {
                    map.put("userStampId", owned.getUserStampId());
                    map.put("isActive", owned.getIsActive());
                }
                result.add(map);
            }
        }
        return result;
    }

    // 4. 내가 보유한 도장 목록 조회 (AJAX)
    @GetMapping("/api/my-stamps")
    @ResponseBody
    public List<UserStampDto> getMyStamps(HttpSession session) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return new ArrayList<>(); // 로그인하지 않은 경우 빈 리스트 반환
        // }
        return pointshopService.getMyStamps(user.getId());
    }

    // 5. 도장 구매
    @PostMapping("/api/buy")
    @ResponseBody
    public boolean buyStamp(@RequestParam Long stampId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return false; // 로그인하지 않은 경우 false 반환
        // }
        return pointshopService.purchaseStamp(user.getId(), stampId);
    }

    // 6. 도장 적용
    @PostMapping("/api/apply")
    @ResponseBody
    public boolean applyStamp(@RequestParam Long userStampId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return false; // 로그인하지 않은 경우 false 반환
        // }
        return pointshopService.applyStamp(user.getId(), userStampId);
    }
}