package com.starter.controller;

import com.starter.dto.ApiResponse;
import com.starter.entity.Diary;
import com.starter.entity.User;
import com.starter.repository.StampRepository;
import com.starter.repository.UserRepository;
import com.starter.service.DiaryService;
import com.starter.service.PointshopService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
public class DiaryController {
    
    private final DiaryService diaryService;
    private final UserRepository userRepository;
    private final PointshopService pointshopService;
    private final StampRepository stampRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_SERVICE_URL = (System.getenv("AI_SERVICE_URL") != null ? System.getenv("AI_SERVICE_URL") : "http://localhost:8000") + "/api/v1/diary-analyzer/analyze";

    // ===================== REST API =====================

    // ===================== UPDATED API ENDPOINT =====================
    // 2025-01-XX: 감정 표현 기능 추가를 위한 API 엔드포인트 수정
    // 기존 일기 저장 API에 emotion 파라미터 추가 (선택사항)
    // 일기 저장 (REST) - 감정 포함
    @PostMapping("/api/diaries")
    @ResponseBody
    public ResponseEntity<ApiResponse<Diary>> saveDiary(@RequestParam Long userId,
                                           @RequestParam String content,
                                           @RequestParam(required = false) String emotion) {
        System.out.println("=== Diary Save API Called ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);
        System.out.println("emotion: " + emotion);
        
        try {
            Diary saved = diaryService.saveDiary(userId, content, emotion);
            System.out.println("Diary saved successfully with ID: " + saved.getDiaryId());
            
            // 간단한 응답 형태로 변경 (디버깅용)
            ApiResponse<Diary> response = new ApiResponse<>(true, "일기 저장 성공", saved);
            System.out.println("API Response: " + response.isSuccess() + ", " + response.getMessage());
            
            // 응답을 Map으로 변환하여 디버깅
            java.util.Map<String, Object> debugResponse = new java.util.HashMap<>();
            debugResponse.put("success", true);
            debugResponse.put("message", "일기 저장 성공");
            debugResponse.put("data", saved);
            debugResponse.put("diaryId", saved.getDiaryId());
            
            System.out.println("Debug Response: " + debugResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error saving diary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "일기 저장 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END UPDATED API ENDPOINT =====================

    // 유저별, 월별 일기 목록 조회 (REST)
    @GetMapping("/api/diaries")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Diary>>> getDiariesByUserAndMonth(@RequestParam Long userId,
                                                                @RequestParam int year,
                                                                @RequestParam int month) {
        List<Diary> diaries = diaryService.getDiariesByUserAndMonth(userId, year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "일기 목록 조회 성공", diaries));
    }

    // ===================== NEW API ENDPOINT =====================
    // 2025-01-XX: 월별 감정 통계 기능 추가
    // 유저별, 월별 감정 통계 조회 (REST)
    @GetMapping("/api/diaries/emotions")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmotionStatsByUserAndMonth(@RequestParam Long userId,
                                                                                          @RequestParam int year,
                                                                                          @RequestParam int month) {
        Map<String, Object> emotionStats = diaryService.getEmotionStatsByUserAndMonth(userId, year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "감정 통계 조회 성공", emotionStats));
    }
    // ===================== END NEW API ENDPOINT =====================

    // ===================== NEW DAILY COMMENT API =====================
    // 2025-01-XX: 일별 코멘트 저장 기능 추가
    // 코멘트 저장 시 현재 적용중인 스탬프 정보도 함께 저장
    // 감정 분석 결과도 함께 반환
    @PostMapping("/api/daily-comments")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveDailyComment(@RequestParam Long userId,
                                                                             @RequestParam String content,
                                                                             @RequestParam String diaryDate,
                                                                             @RequestParam(required = false) String emotionKeywords) {
        System.out.println("=== Daily Comment Save API Called ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);
        System.out.println("diaryDate: " + diaryDate);
        System.out.println("emotionKeywords: " + emotionKeywords);
        
        try {
            // diaryDate 문자열을 LocalDateTime으로 변환
            java.time.LocalDateTime parsedDate = java.time.LocalDateTime.parse(diaryDate);
            System.out.println("Parsed date: " + parsedDate);
            
            // 감정 키워드 파싱
            List<String> emotions = new ArrayList<>();
            if (emotionKeywords != null && !emotionKeywords.trim().isEmpty()) {
                try {
                    // JSON 문자열을 List<String>으로 파싱
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    emotions = mapper.readValue(emotionKeywords, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    System.out.println("Parsed emotion keywords: " + emotions);
                } catch (Exception e) {
                    System.err.println("Error parsing emotion keywords: " + e.getMessage());
                    // 파싱 실패 시 빈 리스트 사용
                    emotions = new ArrayList<>();
                }
            }
            
            // 코멘트 저장 (스탬프 정보 포함)
            System.out.println("Calling diaryService.saveDailyComment...");
            com.starter.entity.DailyComment savedComment = diaryService.saveDailyComment(userId, content, parsedDate);
            System.out.println("Comment saved successfully with ID: " + savedComment.getId());
            
            // 감정 키워드가 있으면 CommentEmotionMapping에 저장
            if (!emotions.isEmpty()) {
                System.out.println("Saving emotion mappings for comment ID: " + savedComment.getId());
                diaryService.saveEmotionMappings(savedComment, emotions);
                System.out.println("Emotion mappings saved successfully");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("commentId", savedComment.getId());
            result.put("content", savedComment.getContent());
            result.put("diaryDate", savedComment.getDiaryDate());
            result.put("createdAt", savedComment.getCreatedAt());
            
            // 스탬프 정보 포함
            if (savedComment.getUserStamp() != null) {
                // UserStamp의 stampId를 통해 Stamp 정보 조회
                com.starter.entity.Stamp stamp = stampRepository.findById(savedComment.getUserStamp().getStampId()).orElse(null);
                if (stamp != null) {
                    System.out.println("Stamp found: " + stamp.getName());
                    result.put("stampName", stamp.getName());
                    result.put("stampImage", stamp.getImage());
                } else {
                    System.out.println("Stamp not found for stampId: " + savedComment.getUserStamp().getStampId());
                }
            } else {
                System.out.println("No UserStamp found for saved comment");
            }
            
            // 저장된 감정 키워드 포함
            result.put("analyzedEmotions", emotions);
            System.out.println("Analyzed emotions: " + emotions);
            
            System.out.println("Returning success response with data: " + result);
            return ResponseEntity.ok(new ApiResponse<>(true, "코멘트 저장 성공", result));
        } catch (Exception e) {
            System.err.println("Error saving daily comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "코멘트 저장 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW DAILY COMMENT API =====================

    // ===================== NEW CALENDAR DATA API =====================
    // 2025-01-XX: 달력 데이터 조회 기능 추가
    // 일기와 코멘트(스탬프 포함) 정보를 함께 조회
    @GetMapping("/api/calendar-data")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCalendarData(@RequestParam Long userId,
                                                                           @RequestParam int year,
                                                                           @RequestParam int month) {
        try {
            Map<String, Object> calendarData = diaryService.getCalendarData(userId, year, month);
            return ResponseEntity.ok(new ApiResponse<>(true, "달력 데이터 조회 성공", calendarData));
        } catch (Exception e) {
            System.err.println("Error getting calendar data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "달력 데이터 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW CALENDAR DATA API =====================

    // ===================== DEBUG API ENDPOINT =====================
    // 2025-01-XX: 디버깅을 위한 모든 코멘트 조회 API 추가
    @GetMapping("/api/debug/comments")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllComments(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> comments = diaryService.getAllCommentsForDebug(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "모든 코멘트 조회 성공", comments));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "모든 코멘트 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END DEBUG API ENDPOINT =====================

    // ===================== EMOTION MAPPING API =====================
    // 2025-01-XX: 감정 매핑 조회 기능 추가
    
    // 특정 코멘트의 감정 분석 결과 조회
    @GetMapping("/api/daily-comments/{commentId}/emotions")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<String>>> getCommentEmotions(@PathVariable Long commentId) {
        try {
            List<com.starter.entity.CommentEmotionMapping> emotionMappings =
                diaryService.getCommentEmotionMappings(commentId);
            
            List<String> emotions = emotionMappings.stream()
                .map(mapping -> mapping.getEmotionData().getName())
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "감정 분석 결과 조회 성공", emotions));
        } catch (Exception e) {
            System.err.println("Error getting comment emotions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "감정 분석 결과 조회 실패: " + e.getMessage(), null));
        }
    }
    
    // 특정 사용자의 모든 감정 분석 결과 조회
    @GetMapping("/api/users/{userId}/emotions")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserEmotions(@PathVariable Long userId) {
        try {
            List<com.starter.entity.CommentEmotionMapping> emotionMappings =
                diaryService.getUserEmotionMappings(userId);
            
            // 감정별 빈도 계산
            Map<String, Long> emotionFrequency = emotionMappings.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    mapping -> mapping.getEmotionData().getName(),
                    java.util.stream.Collectors.counting()
                ));
            
            // 전체 감정 목록
            List<String> allEmotions = emotionMappings.stream()
                .map(mapping -> mapping.getEmotionData().getName())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("emotionFrequency", emotionFrequency);
            result.put("allEmotions", allEmotions);
            result.put("totalEmotionCount", emotionMappings.size());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "사용자 감정 분석 결과 조회 성공", result));
        } catch (Exception e) {
            System.err.println("Error getting user emotions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "사용자 감정 분석 결과 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END EMOTION MAPPING API =====================



    // ===================== NEW API ENDPOINT =====================
    // 2025-01-XX: 현재 적용된 스탬프 조회 기능 추가
    // 사용자가 포인트샵에서 구매한 스탬프 중 현재 적용된 스탬프 정보 조회
    @GetMapping("/api/active-stamp")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveStamp(@RequestParam Long userId) {
        try {
            com.starter.dto.UserStampDto activeStamp = 
                pointshopService.getActiveStamp(userId);
            
            Map<String, Object> result = new HashMap<>();
            if (activeStamp != null) {
                result.put("stampName", activeStamp.getStampName());
                result.put("stampImage", activeStamp.getStampImage());
                result.put("stampDescription", activeStamp.getStampDescription());
                result.put("isActive", activeStamp.getIsActive());
            } else {
                result.put("stampName", "참잘했어요");
                result.put("stampImage", "image/default_stamp.png");
                result.put("stampDescription", "기본 격려 스탬프");
                result.put("isActive", "Y");
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "적용된 스탬프 조회 성공", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "스탬프 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW API ENDPOINT =====================

    // ===================== AI DIARY ANALYSIS API =====================
    // 2025-01-XX: AI 일기 분석 및 코멘트 생성 기능 추가
    @PostMapping("/api/diaries/analyze")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeDiaryWithAI(@RequestParam Long userId,
                                                                              @RequestParam String content) {
        System.out.println("=== AI Diary Analysis API Called ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);
        
        try {
            // AI 서비스에 요청할 데이터 준비
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("user_id", String.valueOf(userId));
            requestData.put("raw_diary", content);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTP 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            
            // AI 서비스 호출
            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(AI_SERVICE_URL, requestEntity, Map.class);
            
            if (aiResponse.getStatusCode().is2xxSuccessful() && aiResponse.getBody() != null) {
                Map<String, Object> aiResult = aiResponse.getBody();
                
                // 응답 데이터 구성
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("processed_diary", aiResult.get("processed_diary"));
                responseData.put("chunks", aiResult.get("chunks"));
                responseData.put("advice", aiResult.get("advice"));
                responseData.put("comment", aiResult.get("comment"));
                responseData.put("quote", aiResult.get("quote"));
                responseData.put("emotion_keywords", aiResult.get("emotion_keywords"));
                responseData.put("similar_past_diaries", aiResult.get("similar_past_diaries"));
                
                System.out.println("AI Analysis completed successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "AI 일기 분석 완료", responseData));
            } else {
                System.err.println("AI service returned error: " + aiResponse.getStatusCode());
                return ResponseEntity.ok(new ApiResponse<>(false, "AI 서비스 오류", null));
            }
            
        } catch (Exception e) {
            System.err.println("Error in AI diary analysis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "AI 일기 분석 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END AI DIARY ANALYSIS API =====================

    // ===================== NEW AI ANALYSIS SAVE API =====================
    // 2025-01-XX: AI 분석 결과를 DB에 저장하는 API 추가
    @PostMapping("/api/diaries/analyze-and-save")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeAndSaveDiaryWithAI(@RequestParam Long userId,
                                                                                     @RequestParam String content) {
        System.out.println("=== AI Diary Analysis and Save API Called ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);
        
        try {
            // AI 서비스에 요청할 데이터 준비
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("user_id", String.valueOf(userId));
            requestData.put("raw_diary", content);
            
            // 디버깅: 요청 데이터 출력
            System.out.println("=== AI Service Request Data ===");
            System.out.println("URL: " + AI_SERVICE_URL);
            System.out.println("user_id: " + requestData.get("user_id"));
            System.out.println("raw_diary: " + requestData.get("raw_diary"));
            System.out.println("Request Data: " + requestData);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTP 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            
            // AI 서비스 호출
            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(AI_SERVICE_URL, requestEntity, Map.class);
            
            if (aiResponse.getStatusCode().is2xxSuccessful() && aiResponse.getBody() != null) {
                Map<String, Object> aiResult = aiResponse.getBody();
                
                // AI 분석 결과를 DB에 저장
                Map<String, Object> savedData = diaryService.saveAIAnalysisResult(userId, aiResult);
                
                // 응답 데이터 구성
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("processed_diary", aiResult.get("processed_diary"));
                responseData.put("chunks", aiResult.get("chunks"));
                responseData.put("advice", aiResult.get("advice"));
                responseData.put("comment", aiResult.get("comment"));
                responseData.put("quote", aiResult.get("quote"));
                responseData.put("emotion_keywords", aiResult.get("emotion_keywords"));
                responseData.put("similar_past_diaries", aiResult.get("similar_past_diaries"));
                responseData.put("saved_feedback_id", savedData.get("feedback_id"));
                responseData.put("saved_proofs", savedData.get("proofs"));
                responseData.put("saved_activities", savedData.get("activities"));
                
                System.out.println("AI Analysis and Save completed successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "AI 일기 분석 및 저장 완료", responseData));
            } else {
                System.err.println("AI service returned error: " + aiResponse.getStatusCode());
                return ResponseEntity.ok(new ApiResponse<>(false, "AI 서비스 오류", null));
            }
            
        } catch (Exception e) {
            System.err.println("Error in AI diary analysis and save: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "AI 일기 분석 및 저장 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW AI ANALYSIS SAVE API =====================

    // ===================== NEW API ENDPOINT =====================
    // 2025-01-XX: 오늘의 모든 기록을 가져오는 API 추가
    // 오늘의 모든 기록 조회 (REST)
    @GetMapping("/api/diaries/today")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Diary>>> getTodayDiaries(@RequestParam Long userId) {
        System.out.println("=== Get Today Diaries API Called ===");
        System.out.println("userId: " + userId);
        
        try {
            List<Diary> todayDiaries = diaryService.getTodayDiaries(userId);
            System.out.println("Today diaries count: " + todayDiaries.size());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "오늘의 기록 조회 성공", todayDiaries));
        } catch (Exception e) {
            System.err.println("Error getting today diaries: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "오늘의 기록 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW API ENDPOINT =====================

    // ===================== NEW DAILY COMMENT BY DATE API =====================
    // 2025-01-XX: 특정 날짜의 DailyComment 조회 기능 추가
    // 과거 날짜의 AI 코멘트를 조회하기 위한 API
    @GetMapping("/api/daily-comments/date")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyCommentByDate(@RequestParam Long userId,
                                                                                  @RequestParam int year,
                                                                                  @RequestParam int month,
                                                                                  @RequestParam int day) {
        try {
            java.util.Optional<com.starter.entity.DailyComment> comment =
                diaryService.getDailyCommentByDate(userId, year, month, day);
            
            Map<String, Object> result = new HashMap<>();
            
            if (comment.isPresent()) {
                com.starter.entity.DailyComment dailyComment = comment.get();
                result.put("success", true);
                result.put("content", dailyComment.getContent());
                result.put("diaryDate", dailyComment.getDiaryDate());
                result.put("createdAt", dailyComment.getCreatedAt());
                
                // 스탬프 정보 포함
                if (dailyComment.getUserStamp() != null) {
                    // UserStamp의 stampId를 통해 Stamp 정보 조회
                    com.starter.entity.Stamp stamp = stampRepository.findById(dailyComment.getUserStamp().getStampId()).orElse(null);
                    if (stamp != null) {
                        result.put("stampName", stamp.getName());
                        result.put("stampImage", stamp.getImage());
                    }
                }
            } else {
                result.put("success", false);
                result.put("message", "해당 날짜의 코멘트가 없습니다.");
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "특정 날짜 코멘트 조회 성공", result));
        } catch (Exception e) {
            System.err.println("Error getting daily comment by date: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "특정 날짜 코멘트 조회 실패: " + e.getMessage(), null));
        }
    }
    // ===================== END NEW DAILY COMMENT BY DATE API =====================

    // 일기 상세 조회 (REST)
    @GetMapping("/api/diaries/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Diary>> getDiaryById(@PathVariable Long id) {
        Optional<Diary> diary = diaryService.getDiaryById(id);
        if (diary.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "일기 조회 성공", diary.get()));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "일기를 찾을 수 없습니다", null));
        }
    }

    // ===================== Thymeleaf =====================

    // 달력/일기 페이지 렌더링 (Thymeleaf)
    /**
     * userId 파라미터로 유저 닉네임을 보여주는 것은 테스트용입니다.
     * 실제 서비스에서는 로그인/세션 기반으로 user 정보를 전달합니다.
     */
    @GetMapping("/diary-calendar")
    public String diaryCalendarPage(@RequestParam(required = false) Long userId, Model model, HttpSession session) {
        User user = null;
        if (userId != null) { // 테스트용: 쿼리 파라미터로 유저 정보 전달
            user = userRepository.findById(userId).orElse(null);
        } else { // 실제 서비스: 세션에서 유저 정보 전달
            user = (User) session.getAttribute("user");
        }
        model.addAttribute("user", user);
        return "diary_calendar";
    }

    // ===================== UPDATED FORM ENDPOINT =====================
    // 2025-01-XX: 감정 표현 기능 추가를 위한 폼 기반 일기 저장 엔드포인트 수정
    // 기존 폼 기반 일기 저장에 emotion 파라미터 추가 (선택사항)
    // (옵션) 폼 기반 일기 저장 (Thymeleaf) - 감정 포함
    @PostMapping("/diary")
    public String saveDiaryForm(@RequestParam Long userId,
                                @RequestParam String content,
                                @RequestParam(required = false) String emotion,
                                Model model) {
        diaryService.saveDiary(userId, content, emotion);
        return "redirect:/diary-calendar";
    }
    // ===================== END UPDATED FORM ENDPOINT =====================
} 