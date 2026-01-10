package com.starter.service;

import com.starter.entity.Diary;
import com.starter.entity.User;
import com.starter.entity.DailyComment;
import com.starter.entity.UserStamp;
import com.starter.entity.Stamp;
import com.starter.entity.CommentEmotionMapping;
import com.starter.entity.CommentEmotionId;
import com.starter.entity.EmotionData;
import com.starter.entity.WeeklyFeedback;
import com.starter.entity.FeedbackProof;
import com.starter.entity.RecommendActivity;
import com.starter.dto.UserStampDto;
import com.starter.repository.DiaryRepository;
import com.starter.repository.UserRepository;
import com.starter.repository.DailyCommentRepository;
import com.starter.repository.UserStampRepository;
import com.starter.repository.StampRepository;
import com.starter.repository.CommentEmotionMappingRepository;
import com.starter.repository.EmotionDataRepository;
import com.starter.repository.WeeklyFeedbackRepository;
import com.starter.repository.FeedbackProofRepository;
import com.starter.repository.RecommendActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DailyCommentRepository dailyCommentRepository;
    private final UserStampRepository userStampRepository;
    private final StampRepository stampRepository;
    private final CommentEmotionMappingRepository commentEmotionMappingRepository;
    private final EmotionDataRepository emotionDataRepository;
    private final WeeklyFeedbackRepository weeklyFeedbackRepository;
    private final FeedbackProofRepository feedbackProofRepository;
    private final RecommendActivityRepository recommendActivityRepository;
    private final PointshopService pointshopService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_SERVICE_URL = "http://localhost:8000/api/v1/diary-analyzer/analyze";

    // ===================== NEW METHOD ADDED =====================
    // 2025-01-XX: 감정 표현 기능 추가를 위한 새로운 일기 저장 메서드
    // 일기 저장 (감정 포함)
    @Transactional
    public Diary saveDiary(Long userId, String content, String emotion) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Diary diary = new Diary();
        diary.setUser(user);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setContent(content);
        diary.setEmotion(emotion); // 새로운 emotion 필드 설정
        return diaryRepository.save(diary);
    }
    // ===================== END NEW METHOD =====================

    // ===================== COMPATIBILITY METHOD =====================
    // 2025-01-XX: 기존 코드 호환성을 위한 오버로드 메서드 추가
    // 기존 saveDiary 메서드 호출 시 emotion은 null로 설정됨
    @Transactional
    public Diary saveDiary(Long userId, String content) {
        return saveDiary(userId, content, null);
    }
    // ===================== END COMPATIBILITY METHOD =====================

    // ===================== UPDATED DAILY COMMENT METHOD =====================
    // 2025-01-XX: 일별 코멘트 저장 기능 수정
    // 코멘트 저장 시 현재 적용중인 스탬프 선호도 정보도 함께 저장
    // 감정 분석 결과를 CommentEmotionMapping 테이블에 저장
    @Transactional
    public DailyComment saveDailyComment(Long userId, String content, LocalDateTime diaryDate) {
        System.out.println("=== DiaryService.saveDailyComment called ===");
        System.out.println("userId: " + userId);
        System.out.println("content: " + content);
        System.out.println("diaryDate: " + diaryDate);
        
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println("User found: " + user.getUserNickname());
        
        // 현재 적용중인 스탬프 정보 가져오기
        System.out.println("Getting current active user stamp...");
        UserStamp activeUserStamp = getCurrentActiveUserStamp(userId);
        System.out.println("Active UserStamp found: " + (activeUserStamp != null ? "yes" : "no"));
        if (activeUserStamp != null) {
            System.out.println("Active stamp ID: " + activeUserStamp.getUserStampId());
        }
        
        System.out.println("Creating DailyComment...");
        DailyComment comment = new DailyComment();
        comment.setUser(user);
        comment.setContent(content);
        comment.setDiaryDate(diaryDate);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUserStamp(activeUserStamp); // 현재 적용중인 스탬프 설정
        
        System.out.println("Saving DailyComment to database...");
        DailyComment savedComment = dailyCommentRepository.save(comment);
        System.out.println("DailyComment saved with ID: " + savedComment.getId());
        System.out.println("UserStamp ID: " + (savedComment.getUserStamp() != null ? savedComment.getUserStamp().getUserStampId() : "null"));
        
        // 감정 분석은 프론트엔드에서 전달받은 감정 키워드를 사용하므로 여기서는 수행하지 않음
        // 감정 키워드는 DiaryController에서 별도로 처리됨
        
        return savedComment;
    }
    // ===================== END UPDATED DAILY COMMENT METHOD =====================

    // ===================== EMOTION ANALYSIS METHODS =====================
    // 2025-01-XX: 감정 분석 및 매핑 저장 기능 추가
    
    // AI 서비스를 통해 감정 분석 수행
    private List<String> analyzeEmotionsFromContent(String content) {
        try {
            System.out.println("Calling AI service for emotion analysis...");
            
            // AI 서비스에 요청할 데이터 준비
            Map<String, Object> requestData = new HashMap<>();
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
                
                // emotion_keywords에서 감정 추출
                @SuppressWarnings("unchecked")
                List<String> emotionKeywords = (List<String>) aiResult.get("emotion_keywords");
                
                if (emotionKeywords != null && !emotionKeywords.isEmpty()) {
                    System.out.println("AI returned emotions: " + emotionKeywords);
                    return emotionKeywords;
                } else {
                    System.out.println("No emotion keywords found in AI response");
                    return new ArrayList<>();
                }
            } else {
                System.err.println("AI service returned error: " + aiResponse.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("Error calling AI service: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // 감정들을 CommentEmotionMapping에 저장
    public void saveEmotionMappings(DailyComment dailyComment, List<String> emotions) {
        System.out.println("Saving emotion mappings for comment ID: " + dailyComment.getId());
        System.out.println("Emotions to save: " + emotions);
        
        for (String emotionName : emotions) {
            try {
                // EmotionData 조회 또는 생성
                EmotionData emotionData = getOrCreateEmotionData(emotionName);
                
                // CommentEmotionMapping 생성
                CommentEmotionMapping mapping = new CommentEmotionMapping();
                
                // 복합키 설정
                CommentEmotionId id = new CommentEmotionId();
                id.setDailyCommentId(dailyComment.getId());
                id.setEmotionId(emotionData.getId());
                mapping.setId(id);
                
                // 연관관계 설정
                mapping.setDailyComment(dailyComment);
                mapping.setEmotionData(emotionData);
                
                // 저장
                commentEmotionMappingRepository.save(mapping);
                System.out.println("Saved emotion mapping: " + emotionName + " for comment ID: " + dailyComment.getId());
                
            } catch (Exception e) {
                System.err.println("Error saving emotion mapping for " + emotionName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // EmotionData 조회 또는 생성
    private EmotionData getOrCreateEmotionData(String emotionName) {
        // 기존 EmotionData 조회
        Optional<EmotionData> existingEmotion = emotionDataRepository.findByName(emotionName);
        
        if (existingEmotion.isPresent()) {
            System.out.println("Found existing emotion: " + emotionName);
            return existingEmotion.get();
        } else {
            // 새로운 EmotionData 생성
            System.out.println("Creating new emotion: " + emotionName);
            EmotionData newEmotion = new EmotionData();
            newEmotion.setName(emotionName);
            newEmotion.setCreatedAt(LocalDateTime.now());
            return emotionDataRepository.save(newEmotion);
        }
    }
    // ===================== END EMOTION ANALYSIS METHODS =====================

    // ===================== EMOTION MAPPING QUERY METHODS =====================
    // 2025-01-XX: 감정 매핑 조회 기능 추가
    
    // 특정 DailyComment의 감정 매핑 조회
    @Transactional(readOnly = true)
    public List<CommentEmotionMapping> getCommentEmotionMappings(Long dailyCommentId) {
        DailyComment dailyComment = dailyCommentRepository.findById(dailyCommentId)
            .orElseThrow(() -> new IllegalArgumentException("DailyComment not found"));
        
        return commentEmotionMappingRepository.findByDailyComment(dailyComment);
    }
    
    // 특정 사용자의 모든 감정 매핑 조회
    @Transactional(readOnly = true)
    public List<CommentEmotionMapping> getUserEmotionMappings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<DailyComment> userComments = dailyCommentRepository.findByUser(user);
        return commentEmotionMappingRepository.findByDailyCommentIn(userComments);
    }
    // ===================== END EMOTION MAPPING QUERY METHODS =====================

    // ===================== UPDATED CALENDAR DATA METHOD =====================
    // 2025-01-XX: 달력 조회를 위한 통합 데이터 조회 메서드 수정
    // 일기와 코멘트(UserStamp 포함) 정보를 함께 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getCalendarData(Long userId, int year, int month) {
        
        // 월별 일기 조회
        List<Diary> diaries = getDiariesByUserAndMonth(userId, year, month);
        
        // 월별 코멘트 조회 (UserStamp 정보 포함)
        List<DailyComment> comments = dailyCommentRepository.findByUserAndYearMonthWithStamp(userId, year, month);
        
        // 코멘트 데이터를 Map으로 변환하여 Stamp 정보 포함
        List<Map<String, Object>> commentsWithStamp = new ArrayList<>();
        for (DailyComment comment : comments) {
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("content", comment.getContent());
            commentData.put("diaryDate", comment.getDiaryDate());
            commentData.put("createdAt", comment.getCreatedAt());
            
            // UserStamp 정보 처리
            if (comment.getUserStamp() != null) {
                // Stamp 정보 조회
                Stamp stamp = stampRepository.findById(comment.getUserStamp().getStampId()).orElse(null);
                if (stamp != null) {
                    Map<String, Object> userStampData = new HashMap<>();
                    userStampData.put("stampId", comment.getUserStamp().getStampId());
                    userStampData.put("stampName", stamp.getName());
                    userStampData.put("stampImage", stamp.getImage());
                    commentData.put("userStamp", userStampData);
                } else {
                    // Stamp 정보가 없는 경우 기본값 설정
                    Map<String, Object> userStampData = new HashMap<>();
                    userStampData.put("stampId", -1L);
                    userStampData.put("stampName", "참잘했어요");
                    userStampData.put("stampImage", "image/default_stamp.png");
                    commentData.put("userStamp", userStampData);
                }
            } else {
                // UserStamp가 없는 경우 기본값 설정
                Map<String, Object> userStampData = new HashMap<>();
                userStampData.put("stampId", -1L);
                userStampData.put("stampName", "참잘했어요");
                userStampData.put("stampImage", "image/default_stamp.png");
                commentData.put("userStamp", userStampData);
            }
            
            commentsWithStamp.add(commentData);
        }
        
        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("diaries", diaries);
        result.put("comments", commentsWithStamp);
        result.put("year", year);
        result.put("month", month);
        
        return result;
    }
    // ===================== END UPDATED CALENDAR DATA METHOD =====================



    // ===================== NEW METHOD =====================
    // 2025-01-XX: 현재 활성화된 UserStamp 조회 메서드 추가
    @Transactional(readOnly = true)
    public UserStamp getCurrentActiveUserStamp(Long userId) {
        // PointshopService를 통해 현재 활성화된 스탬프 조회
        UserStampDto activeStampDto = pointshopService.getActiveStamp(userId);
        if (activeStampDto != null && activeStampDto.getUserStampId() != null) {
            // UserStamp 엔티티 조회
            return userStampRepository.findById(activeStampDto.getUserStampId()).orElse(null);
        }
        return null;
    }
    // ===================== END NEW METHOD =====================

    // ===================== DEBUG METHOD =====================
    // 2025-01-XX: 디버깅을 위한 모든 코멘트 조회 메서드 추가
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllCommentsForDebug(Long userId) {
        List<DailyComment> comments = dailyCommentRepository.findByUser_UserIdAndDiaryDateBetween(
            userId, 
            LocalDateTime.of(2020, 1, 1, 0, 0), 
            LocalDateTime.of(2030, 12, 31, 23, 59)
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (DailyComment comment : comments) {
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("content", comment.getContent());
            commentData.put("diaryDate", comment.getDiaryDate());
            commentData.put("createdAt", comment.getCreatedAt());
            commentData.put("userStamp", comment.getUserStamp());
            result.add(commentData);
        }
        
        return result;
    }
    // ===================== END DEBUG METHOD =====================



    // 유저별, 월별 일기 목록 조회
    @Transactional(readOnly = true)
    public List<Diary> getDiariesByUserAndMonth(Long userId, int year, int month) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23,59,59);
        return diaryRepository.findByUserAndCreatedAtBetween(user, startDateTime, endDateTime);
    }

    // ===================== NEW EMOTION STATS METHOD =====================
    // 2025-01-XX: 월별 감정 통계 기능 추가
    // 유저별, 월별 감정 통계 조회 - 상위 3개 감정 반환
    @Transactional(readOnly = true)
    public Map<String, Object> getEmotionStatsByUserAndMonth(Long userId, int year, int month) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23,59,59);
        
        List<Diary> diaries = diaryRepository.findByUserAndCreatedAtBetween(user, startDateTime, endDateTime);
        
        // 감정별 카운트 계산
        Map<String, Long> emotionCounts = diaries.stream()
                .filter(diary -> diary.getEmotion() != null && !diary.getEmotion().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    Diary::getEmotion,
                    Collectors.counting()
                ));
        
        // 상위 3개 감정 추출
        List<Map<String, Object>> topEmotions = emotionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    Map<String, Object> emotionData = new HashMap<>();
                    emotionData.put("emotion", entry.getKey());
                    emotionData.put("count", entry.getValue());
                    return emotionData;
                })
                .collect(Collectors.toList());
        
        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("topEmotions", topEmotions);
        result.put("totalEmotions", emotionCounts.size());
        result.put("totalDiaries", diaries.size());
        result.put("year", year);
        result.put("month", month);
        
        return result;
    }
    // ===================== END NEW EMOTION STATS METHOD =====================

    // ===================== NEW METHOD ADDED =====================
    // 2025-01-XX: 오늘의 모든 기록을 가져오는 메서드 추가
    // 오늘의 모든 기록 조회
    @Transactional(readOnly = true)
    public List<Diary> getTodayDiaries(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 오늘 날짜의 시작과 끝 시간 설정
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        return diaryRepository.findByUserAndCreatedAtBetween(user, startOfDay, endOfDay);
    }
    // ===================== END NEW METHOD =====================

    // 일기 상세 조회
    @Transactional(readOnly = true)
    public Optional<Diary> getDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId);
    }

    // ===================== NEW DAILY COMMENT BY DATE METHOD =====================
    // 2025-01-XX: 특정 날짜의 DailyComment 조회 기능 추가
    // 과거 날짜의 AI 코멘트를 조회하기 위한 메서드
    @Transactional(readOnly = true)
    public Optional<DailyComment> getDailyCommentByDate(Long userId, int year, int month, int day) {
        
        // 해당 날짜의 시작과 끝 시간 설정
        LocalDateTime startOfDay = LocalDateTime.of(year, month, day, 0, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(year, month, day, 23, 59, 59);
        
        // 해당 날짜의 DailyComment 조회
        List<DailyComment> comments = dailyCommentRepository.findByUser_UserIdAndDiaryDateBetween(
            userId, startOfDay, endOfDay
        );
        
        // 가장 최근 코멘트 반환 (여러 개가 있을 경우)
        return comments.stream()
                .max(Comparator.comparing(DailyComment::getCreatedAt));
    }
    // ===================== END NEW DAILY COMMENT BY DATE METHOD =====================

    // ===================== NEW AI ANALYSIS SAVE METHOD =====================
    // 2025-01-XX: AI 분석 결과를 DB에 저장하는 메서드 추가
    // AI 분석 결과를 WeeklyFeedback, FeedbackProof, RecommendActivity에 저장
    @Transactional
    public Map<String, Object> saveAIAnalysisResult(Long userId, Map<String, Object> aiResult) {
        System.out.println("=== DiaryService.saveAIAnalysisResult called ===");
        System.out.println("userId: " + userId);
        System.out.println("aiResult keys: " + aiResult.keySet());
        
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // WeeklyFeedback 생성
        WeeklyFeedback feedback = new WeeklyFeedback();
        feedback.setUser(user);
        feedback.setCreatedAt(LocalDateTime.now());
        
        // AI 분석 결과에서 데이터 추출
        String comment = (String) aiResult.get("comment");
        String quote = (String) aiResult.get("quote");
        List<String> emotionKeywords = (List<String>) aiResult.get("emotion_keywords");
        List<Map<String, Object>> similarPastDiaries = (List<Map<String, Object>>) aiResult.get("similar_past_diaries");
        List<Map<String, Object>> recommendActivities = (List<Map<String, Object>>) aiResult.get("recommend_activities");
        
        // WeeklyFeedback 설정
        feedback.setEmotionSummary(String.join(", ", emotionKeywords != null ? emotionKeywords : new ArrayList<>()));
        feedback.setIsQualified("Y"); // 기본값으로 자격 부여
        feedback.setWeekOffset(0); // 현재 주차
        
        // WeeklyFeedback 저장
        WeeklyFeedback savedFeedback = weeklyFeedbackRepository.save(feedback);
        System.out.println("WeeklyFeedback saved with ID: " + savedFeedback.getId());
        
        // FeedbackProof 저장 (유사한 과거 일기들)
        List<Map<String, Object>> savedProofs = new ArrayList<>();
        if (similarPastDiaries != null) {
            for (Map<String, Object> similarDiary : similarPastDiaries) {
                FeedbackProof proof = new FeedbackProof();
                proof.setFeedback(savedFeedback);
                proof.setType("similar_diary");
                proof.setDetail(similarDiary.toString());
                proof.setCreatedAt(LocalDateTime.now());
                
                FeedbackProof savedProof = feedbackProofRepository.save(proof);
                Map<String, Object> proofData = new HashMap<>();
                proofData.put("proof_id", savedProof.getId());
                proofData.put("type", savedProof.getType());
                proofData.put("detail", savedProof.getDetail());
                savedProofs.add(proofData);
            }
        }
        
        // RecommendActivity 저장
        List<Map<String, Object>> savedActivities = new ArrayList<>();
        if (recommendActivities != null) {
            for (int i = 0; i < recommendActivities.size(); i++) {
                Map<String, Object> activity = recommendActivities.get(i);
                RecommendActivity recommendActivity = new RecommendActivity();
                recommendActivity.setFeedback(savedFeedback);
                recommendActivity.setTitle((String) activity.get("title"));
                recommendActivity.setCategory((String) activity.get("category"));
                recommendActivity.setDetail((String) activity.get("detail"));
                recommendActivity.setOrder((long) (i + 1));
                recommendActivity.setCreatedAt(LocalDateTime.now());
                
                RecommendActivity savedActivity = recommendActivityRepository.save(recommendActivity);
                Map<String, Object> activityData = new HashMap<>();
                activityData.put("activity_id", savedActivity.getId());
                activityData.put("title", savedActivity.getTitle());
                activityData.put("category", savedActivity.getCategory());
                activityData.put("detail", savedActivity.getDetail());
                activityData.put("order", savedActivity.getOrder());
                savedActivities.add(activityData);
            }
        }
        
        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("feedback_id", savedFeedback.getId());
        result.put("proofs", savedProofs);
        result.put("activities", savedActivities);
        
        System.out.println("AI Analysis Result saved successfully");
        System.out.println("Feedback ID: " + savedFeedback.getId());
        System.out.println("Proofs count: " + savedProofs.size());
        System.out.println("Activities count: " + savedActivities.size());
        
        return result;
    }
    // ===================== END NEW AI ANALYSIS SAVE METHOD =====================
} 