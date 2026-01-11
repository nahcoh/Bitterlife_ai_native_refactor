package com.starter.service;

import com.starter.dto.StampDto;
import com.starter.dto.UserStampDto;
import com.starter.entity.Stamp;
import com.starter.entity.UserStamp;
import com.starter.entity.UserPointHistory;
import com.starter.repository.StampRepository;
import com.starter.repository.UserPointHistoryRepository;
import com.starter.repository.UserStampRepository;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PointshopService {
    private final UserPointHistoryRepository userPointHistoryRepository;
    private final StampRepository stampRepository;
    private final UserStampRepository userStampRepository;

    // 1. 사용자 포인트 조회
    public int getUserPoint(Long userId) {
        var history = userPointHistoryRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        int points = history != null ? history.getAfterPoint() : 0;
        System.out.println("=== 포인트 조회 ===");
        System.out.println("사용자 ID: " + userId);
        System.out.println("최신 기록: " + (history != null ? history.getReason() : "없음"));
        System.out.println("기록 시간: " + (history != null ? history.getCreatedAt() : "없음"));
        System.out.println("현재 포인트: " + points);
        return points;
    }

    // 2. 구매 가능한 도장 목록 조회
    public List<StampDto> getAvailableStamps(Long userId) {
        List<Stamp> stamps = stampRepository.findAll();
        List<StampDto> result = new ArrayList<>();
        for (Stamp stamp : stamps) {
            StampDto dto = new StampDto();
            dto.setStampId(stamp.getId());
            dto.setName(stamp.getName());
            dto.setImage(stamp.getImage());
            dto.setQualification(stamp.getQualification());
            dto.setPrice(stamp.getPrice());
            dto.setDescription(stamp.getDescription());
            dto.setStatus(String.valueOf(stamp.getStatus()));
            dto.setSalesAt(stamp.getSalesAt());
            dto.setSalesEnd(stamp.getSalesEnd());
            result.add(dto);
        }
        return result;
    }

    // 3. 도장 상세 정보 조회
    public StampDto getStampDetail(Long stampId, Long userId) {
        // TODO: 도장 상세 정보 반환
        return null;
    }

    // 4. 카테고리별 도장 목록 조회
    public List<StampDto> getStampsByCategory(String category, Long userId) {
        // TODO: 카테고리별 도장 목록 반환
        return null;
    }

    @Transactional
    public boolean purchaseStamp(Long userId, Long stampId) {
        try {
            System.out.println("=== 구매 시도 ===");
            System.out.println("사용자 ID: " + userId);
            System.out.println("도장 ID: " + stampId);

            // 1. 이미 보유한 도장인지 확인
            UserStamp owned = userStampRepository.findByUserIdAndStampId(userId, stampId);
            if (owned != null) {
                System.out.println("이미 보유한 도장입니다.");
                return false;
            }

            // 2. 포인트 및 도장 가격 조회
            int currentPoint = getUserPoint(userId);
            Stamp stamp = stampRepository.findById(stampId).orElse(null);
            if (stamp == null) {
                System.out.println("도장을 찾을 수 없습니다: " + stampId);
                return false;
            }
            int price = stamp.getPrice();

            System.out.println("현재 포인트: " + currentPoint);
            System.out.println("도장 가격: " + price);

            // 3. 포인트 부족 시
            if (currentPoint < price) {
                System.out.println("포인트가 부족합니다.");
                return false;
            }

            // 4. user_stamp에 추가
            UserStamp userStamp = new stamp();
            userStamp.setUserId(userId);
            userStamp.setStampId(stampId);
            userStamp.setIsActive("N");
            userStamp.setCreatedAt(LocalDateTime.now());
            UserStamp savedUserStamp = userStampRepository.save(userStamp);
            System.out.println("UserStamp 저장 완료: " + savedUserStamp.getUserStampId());

            // 5. 포인트 차감 기록
            UserPointHistory history = new UserPointHistory();
            history.setUserId(userId);
            history.setBeforePoint(currentPoint);
            history.setAmount(-price);
            history.setAfterPoint(currentPoint - price);
            history.setReason("도장 구매: " + stamp.getName());
            history.setCreatedAt(LocalDateTime.now().plusSeconds(1)); // 1초 추가하여 최신 기록 보장

            UserPointHistory savedHistory = userPointHistoryRepository.save(history);

            System.out.println("=== 구매 완료 ===");
            System.out.println("차감 후 포인트: " + (currentPoint - price));
            System.out.println("기록 저장됨: " + savedHistory.getReason());
            System.out.println("저장된 기록 ID: " + savedHistory.getUserPointHistoryId());

            // 저장 후 포인트 재확인
            int updatedPoint = getUserPoint(userId);
            System.out.println("저장 후 조회된 포인트: " + updatedPoint);

            // 6. UserStampHistory에 구매 기록 추가 (임시로 제거)
            /*
            UserStampHistory stampHistory = new UserStampHistory();
            stampHistory.setUserId(userId);
            stampHistory.setPrevStampId(null); // 구매 시에는 이전 스탬프가 없음
            stampHistory.setNewStampId(stampId);
            stampHistory.setCreatedAt(LocalDateTime.now());
            userStampHistoryRepository.save(stampHistory);

            System.out.println("=== UserStampHistory 기록 추가 ===");
            System.out.println("새로운 스탬프 ID: " + stampId);
            System.out.println("스탬프 이름: " + stamp.getName());
            */
            System.out.println("UserStampHistory 기록 추가 건너뜀 (임시)");

            return true;
        } catch (Exception e) {
            System.out.println("=== 구매 중 오류 발생 ===");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean applyStamp(Long userId, Long userStampId) {
        try {
            System.out.println("=== 스탬프 적용 시작 ===");
            System.out.println("사용자 ID: " + userId);
            System.out.println("적용할 UserStamp ID: " + userStampId);

            // 1. 적용할 스탬프가 존재하는지 먼저 확인
            UserStamp toActivate = userStampRepository.findById(userStampId).orElse(null);
            if (toActivate == null) {
                System.out.println("적용할 스탬프를 찾을 수 없음: " + userStampId);
                return false;
            }

            if (!toActivate.getUserId().equals(userId)) {
                System.out.println("스탬프 소유자가 일치하지 않음");
                return false;
            }

            // 2. 모든 도장 isActive="N"으로
            List<UserStamp> userStamps = userStampRepository.findByUserId(userId);
            System.out.println("사용자 보유 스탬프 수: " + userStamps.size());
            for (UserStamp us : userStamps) {
                us.setIsActive("N");
            }
            userStampRepository.saveAll(userStamps);
            System.out.println("모든 스탬프 비활성화 완료");

            // 3. 선택한 도장만 isActive="Y"로
            toActivate.setIsActive("Y");
            userStampRepository.save(toActivate);
            System.out.println("스탬프 활성화 완료: " + toActivate.getStampId());

            // 4. UserStampHistory에 기록 추가 (임시로 제거)
            /*
            UserStampHistory history = new UserStampHistory();
            history.setUserId(userId);

            // 이전 스탬프 ID 찾기 (현재 활성화된 스탬프가 있다면)
            UserStamp currentActive = userStampRepository.findByUserIdAndIsActive(userId, "Y");
            if (currentActive != null && !currentActive.getUserStampId().equals(userStampId)) {
                history.setPrevStampId(currentActive.getStampId());
            } else {
                history.setPrevStampId(null); // 첫 번째 스탬프인 경우
            }

            history.setNewStampId(toActivate.getStampId());
            history.setCreatedAt(LocalDateTime.now());
            userStampHistoryRepository.save(history);
            System.out.println("UserStampHistory 기록 추가 완료");
            */
            System.out.println("UserStampHistory 기록 추가 건너뜀 (임시)");

            System.out.println("=== 스탬프 적용 완료 ===");
            return true;
        } catch (Exception e) {
            System.out.println("=== 스탬프 적용 중 오류 발생 ===");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 7. 현재 적용중인 도장 조회
    public UserStampDto getActiveStamp(Long userId) {
        // 기존 코드 (주석처리)
        // TODO: 현재 적용중인 도장 반환
        // return null;

        // 새로운 구현: 스탬프 상세 정보 포함
        List<UserStamp> userStamps = userStampRepository.findByUserId(userId);
        for (UserStamp us : userStamps) {
            if ("Y".equals(us.getIsActive())) {
                // 스탬프 정보 조회
                Stamp stamp = stampRepository.findById(us.getStampId()).orElse(null);
                if (stamp != null) {
                    UserStampDto dto = new UserStampDto();
                    dto.setUserStampId(us.getUserStampId());
                    dto.setUserId(us.getUserId());
                    dto.setStampId(us.getStampId());
                    dto.setIsActive(us.getIsActive());
                    dto.setCreatedAt(us.getCreatedAt());
                    dto.setUpdatedAt(us.getUpdatedAt());

                    // 스탬프 정보 설정
                    dto.setStampName(stamp.getName());
                    dto.setStampImage(stamp.getImage());
                    dto.setStampDescription(stamp.getDescription());
                    dto.setStampPrice(stamp.getPrice());

                    return dto;
                }
            }
        }

        // 활성 스탬프가 없을 경우 기본 스탬프 반환
        UserStampDto defaultDto = new UserStampDto();
        defaultDto.setUserStampId(null);
        defaultDto.setUserId(userId);
        defaultDto.setStampId(null);
        defaultDto.setIsActive("N");
        defaultDto.setCreatedAt(LocalDateTime.now());
        defaultDto.setUpdatedAt(LocalDateTime.now());

        // 기본 스탬프 정보 설정
        defaultDto.setStampName("기본 스탬프");
        defaultDto.setStampImage("default_stamp.png");
        defaultDto.setStampDescription("기본 스탬프입니다.");
        defaultDto.setStampPrice(0);

        return defaultDto;
    }

    // 8. 내가 보유한 도장 목록 조회
    public List<UserStampDto> getMyStamps(Long userId) {
        List<UserStamp> userStamps = userStampRepository.findByUserId(userId);
        List<UserStampDto> result = new ArrayList<>();
        for (UserStamp us : userStamps) {
            // 기존 코드 (주석처리)
            // UserStampDto dto = new UserStampDto();
            // dto.setUserStampId(us.getUserStampId());
            // dto.setUserId(us.getUserId());
            // dto.setStampId(us.getStampId());
            // dto.setIsActive(us.getIsActive());
            // dto.setCreatedAt(us.getCreatedAt());
            // dto.setUpdatedAt(us.getUpdatedAt());
            // result.add(dto);

            // 새로운 구현: 스탬프 상세 정보 포함
            Stamp stamp = stampRepository.findById(us.getStampId()).orElse(null);
            if (stamp != null) {
                UserStampDto dto = new UserStampDto();
                dto.setUserStampId(us.getUserStampId());
                dto.setUserId(us.getUserId());
                dto.setStampId(us.getStampId());
                dto.setIsActive(us.getIsActive());
                dto.setCreatedAt(us.getCreatedAt());
                dto.setUpdatedAt(us.getUpdatedAt());

                // 스탬프 정보 설정
                dto.setStampName(stamp.getName());
                dto.setStampImage(stamp.getImage());
                dto.setStampDescription(stamp.getDescription());
                dto.setStampPrice(stamp.getPrice());

                result.add(dto);
            }
        }
        return result;
    }
}