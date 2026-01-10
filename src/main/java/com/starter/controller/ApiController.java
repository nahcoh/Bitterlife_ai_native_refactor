package com.starter.controller;

import com.starter.entity.User;
import com.starter.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * API 엔드포인트를 제공하는 컨트롤러
 * 회원가입 시 중복 검사, 로그인 상태 확인 등의 API 기능을 처리
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final UserService userService;

    /**
     * 이메일 중복 검사 API
     * 회원가입 폼에서 실시간으로 이메일 중복 여부를 확인
     * 
     * @param email 검사할 이메일 주소
     * @return "true" (중복됨) 또는 "false" (사용 가능)
     */
    @PostMapping("/check-email")
    public String checkEmail(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return String.valueOf(isDuplicate);
    }

    /**
     * 닉네임 중복 검사 API
     * 회원가입 폼에서 실시간으로 닉네임 중복 여부를 확인
     * 
     * @param nickname 검사할 닉네임
     * @return "true" (중복됨) 또는 "false" (사용 가능)
     */
    @PostMapping("/check-nickname")
    public String checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
        return String.valueOf(isDuplicate);
    }

    /**
     * 로그인 상태 확인 API
     * 현재 세션에서 사용자의 로그인 상태를 확인
     * 클라이언트에서 페이지 로드 시 로그인 상태를 체크하는데 사용
     * 
     * @param session HTTP 세션 객체
     * @return "true" (로그인됨) 또는 "false" (로그아웃됨)
     */
    @PostMapping("/check-login-status")
    public String checkLoginStatus(HttpSession session) {
        log.info("로그인 상태 확인 요청 받음");
        try {
            // 세션에서 사용자 정보 확인
            User user = (User) session.getAttribute("user");
            boolean isLoggedIn = user != null && user.isActive();
            log.info("로그인 상태: {}, 사용자: {}", isLoggedIn, user != null ? user.getUserEmail() : "null");
            return String.valueOf(isLoggedIn);
        } catch (Exception e) {
            log.error("로그인 상태 확인 중 오류 발생", e);
            // 세션에서 사용자 정보를 가져올 때 오류가 발생하면 로그아웃 상태로 처리
            session.removeAttribute("user");
            return "false";
        }
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * 세션에서 사용자 정보를 JSON 형태로 반환
     * 
     * @param session HTTP 세션 객체
     * @return 사용자 정보 JSON 또는 null
     */
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        log.info("현재 사용자 정보 조회 요청 받음");
        try {
            User user = (User) session.getAttribute("user");
            if (user != null && user.isActive()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("userEmail", user.getUserEmail());
                userInfo.put("nickname", user.getUserNickname());
                userInfo.put("isActive", user.isActive());
                
                log.info("사용자 정보 반환: {}", userInfo);
                return ResponseEntity.ok(userInfo);
            } else {
                log.info("로그인된 사용자가 없음");
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.ok(null);
        }
    }

    /**
     * 채팅 API - FastAPI 서비스로 프록시 (고급 RAG 기능 사용)
     * 사용자의 채팅 메시지를 FastAPI의 고급 ChatbotService로 전달하고 AI 응답을 받아옴
     * 
     * @param message 사용자가 입력한 채팅 메시지
     * @param session HTTP 세션 객체
     * @return AI 응답 메시지
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestParam String message, HttpSession session) {
        log.info("고급 채팅 요청 받음: {}", message);
        
        try {
            // 사용자 ID 가져오기 (세션에서)
            User user = (User) session.getAttribute("user");
            String userId = (user != null) ? "user_" + user.getUserId() : "web_user_01"; // 문자열로 변경
            
            // FastAPI 고급 채팅 서비스 호출
            RestTemplate restTemplate = new RestTemplate();
            String fastApiUrl = "http://localhost:8000/api/v1/conversation-manager/chat-advanced";
            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("user_id", userId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // FastAPI 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> result = new HashMap<>();
                
                result.put("success", responseBody.get("success"));
                result.put("response", responseBody.get("response"));
                result.put("user_id", responseBody.get("user_id"));
                result.put("intent", responseBody.get("intent")); // 의도 분류 결과
                result.put("rag_used", responseBody.get("rag_used")); // RAG 사용 여부
                
                // RAG 검색 정보 추가
                result.put("search_query", responseBody.get("search_query"));
                result.put("search_filters", responseBody.get("search_filters"));
                result.put("similarity_scores", responseBody.get("similarity_scores"));
                result.put("total_searched", responseBody.get("total_searched"));
                result.put("total_filtered", responseBody.get("total_filtered"));
                result.put("rag_details", responseBody.get("rag_details"));
                
                log.info("고급 AI 응답 생성 완료: {}", responseBody.get("response"));
                log.info("RAG 사용 여부: {}, 검색 쿼리: {}", responseBody.get("rag_used"), responseBody.get("search_query"));
                return ResponseEntity.ok(result);
            } else {
                throw new RuntimeException("FastAPI 고급 채팅 서비스 응답 오류");
            }
            
        } catch (Exception e) {
            log.error("고급 채팅 처리 중 오류 발생", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "채팅 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 대화 요약 API - 현재 대화를 종료하고 요약 제공
     * 
     * @param session HTTP 세션 객체
     * @return 대화 요약 결과
     */
    @PostMapping("/chat/summary")
    public ResponseEntity<Map<String, Object>> getChatSummary(HttpSession session) {
        log.info("대화 요약 요청 받음");
        
        try {
            // 사용자 ID 가져오기 (세션에서)
            User user = (User) session.getAttribute("user");
            String userId = (user != null) ? "user_" + user.getUserId() : "web_user_01";
            
            // FastAPI 대화 요약 서비스 호출
            RestTemplate restTemplate = new RestTemplate();
            String fastApiUrl = "http://localhost:8000/api/v1/conversation-manager/conversation-summary";
            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // FastAPI 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", response.getBody().get("success"));
                result.put("summary", response.getBody().get("response"));
                result.put("user_id", response.getBody().get("user_id"));
                
                log.info("대화 요약 완료");
                return ResponseEntity.ok(result);
            } else {
                throw new RuntimeException("FastAPI 대화 요약 서비스 응답 오류");
            }
            
        } catch (Exception e) {
            log.error("대화 요약 처리 중 오류 발생", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "대화 요약 중 오류가 발생했습니다.");
            return ResponseEntity.ok(errorResult);
        }
    }
} 