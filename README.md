# Backend - 일기 기록 및 달력 서비스

## 구현 완료된 기능

### 1. 데이터베이스 스키마
- **User 엔티티**: 사용자 정보 관리
- **Diary 엔티티**: 일기 기록 (감정 필드 포함)

### 2. API 엔드포인트
- `POST /api/diaries`: 일기 저장 (감정 포함)
- `GET /api/diaries`: 유저별, 월별 일기 목록 조회
- `GET /api/diaries/{id}`: 일기 상세 조회
- `GET /diary-calendar`: 달력/일기 페이지 렌더링

### 3. 주요 기능
- ✅ 일기 작성 및 저장 (감정 선택 포함)
- ✅ 달력에서 일기 작성일 표시
- ✅ 월별 일기 목록 조회
- ✅ 감정별 일기 필터링
- ✅ 실시간 UI 업데이트

## 설정 및 실행

### 1. 데이터베이스 설정
```sql
-- 데이터베이스 스키마 업데이트
source database_update.sql

-- 테스트 데이터 생성
source test_data.sql
```

### 2. 애플리케이션 실행
```bash
# Spring Boot 애플리케이션 실행
./mvnw spring-boot:run
```

### 3. 테스트
- 브라우저에서 `http://localhost:8080/diary-calendar?userId=1` 접속
- 일기 작성 및 달력 확인

## API 사용법

### 일기 저장
```javascript
const formData = new FormData();
formData.append('userId', 1);
formData.append('content', '오늘의 기록');
formData.append('appliedStamp', '참잘했어요');
formData.append('emotion', '😊'); // 선택사항

fetch('/api/diaries', {
    method: 'POST',
    body: formData
});
```

### 일기 목록 조회
```javascript
fetch('/api/diaries?userId=1&year=2025&month=1')
    .then(response => response.json())
    .then(data => console.log(data));
```

## 다음 단계 구현 예정
- [ ] AI 코멘트 생성 기능
- [ ] 주차별 감정 리포트 상세 페이지
- [ ] 사용자 인증 및 세션 관리
- [ ] 일기 수정/삭제 기능
- [ ] 이미지 업로드 기능 