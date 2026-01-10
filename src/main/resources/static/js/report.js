// 📌 상태 변수
let validOffsets = [];
let currentIndex = 0;
let emotionChartInstance = null;

// 📌 세션에서 userId를 Thymeleaf로 안전하게 받음
let reportUserId = window.reportUserId;

// 📌 DOM 요소
const currentWeekDisplay = document.getElementById('current-week-display');
const prevWeekBtn = document.getElementById('prev-week-btn');
const nextWeekBtn = document.getElementById('next-week-btn');
const reportEmotionSummary = document.getElementById('report-emotion-summary');
const reportMainKeywords = document.getElementById('report-main-keywords');
const diagnosisBasisBubbles = document.getElementById('diagnosis-basis-bubbles');
const recommendationList = document.getElementById('recommendation-list');

// 📌 API 호출 함수
async function loadWeeklyReport(weekOffset) {
    try {
        console.log(`🔍 API 호출: /api/report?userId=${reportUserId}&weekOffset=${weekOffset}`);
        console.log(`📅 요청한 주차 정보: weekOffset=${weekOffset} (0=이번주, 양수=과거, 음수=미래)`);
        
        const response = await fetch(`/api/report?userId=${reportUserId}&weekOffset=${weekOffset}`);
        console.log(`📡 API 응답 상태:`, response.status);
        
        if (!response.ok) {
            console.error(`❌ API 응답 실패: ${response.status}`);
            throw new Error('리포트 데이터를 불러오지 못했습니다.');
        }
        
        const data = await response.json();
        console.log(`📊 API 응답 데이터:`, data);
        console.log(`📊 백엔드에서 받은 week 정보:`, data.week);
        return data;
    } catch (error) {
        console.error(`❌ API 호출 오류:`, error);
        return null;
    }
}

// 📌 리포트 렌더링
async function updateReportContent(weekOffset) {
    const report = await loadWeeklyReport(weekOffset);
    console.log('Report data:', report);
    
    const isEmptyReport = !report
        || (report.emotionSummary === "이번 주 감정 분석이 준비되지 않았습니다." 
            && report.evidenceSentences.length === 0 
            && report.recommendations.length === 0);

    if (isEmptyReport) {
        // 백엔드에서 제공하는 week 정보를 사용, 없으면 기본 메시지
        const displayText = report?.week ? `${report.week} (리포트 없음)` : '리포트가 준비되지 않았습니다.';
        currentWeekDisplay.innerText = displayText;
        reportEmotionSummary.innerText = "선생님의 감정 진단 (현상)";
        reportMainKeywords.innerText = "";
        diagnosisBasisBubbles.innerHTML = '<p class="text-[#8F9562] text-center py-4">해당 주차의 리포트가 아직 준비되지 않았습니다.</p>';
        recommendationList.innerHTML = '';

        if (emotionChartInstance) {
            emotionChartInstance.destroy();
            emotionChartInstance = null;
        }
        document.querySelector('.chart-container').innerHTML = '<canvas id="emotionTrendChart"></canvas>';
        return;
    }

    // 백엔드에서 제공하는 week 정보를 그대로 사용
    currentWeekDisplay.innerText = report.week;
    reportEmotionSummary.innerText = report.emotionSummary;

    // 진단 근거 버블 렌더링
    diagnosisBasisBubbles.innerHTML = '';
    report.evidenceSentences.forEach(text => {
        const span = document.createElement('span');
        span.className = 'keyword-bubble';
        span.innerText = text;
        diagnosisBasisBubbles.appendChild(span);
    });

    // 추천 활동 렌더링
    recommendationList.innerHTML = '';
    report.recommendations.forEach(rec => {
        const li = document.createElement('li');
        li.className = 'recommendation-item';
        li.innerHTML = `
            <h3 class="text-lg font-semibold text-[#90533B] mb-1">${rec.title}</h3>
            <p class="text-[#495235] text-sm">${rec.description}</p>`;
        recommendationList.appendChild(li);
    });

    // 감정 차트 렌더링
    if (emotionChartInstance) emotionChartInstance.destroy();
    
    console.log(`📈 감정 차트 데이터 확인:`, report.emotionCharts);
    console.log(`📈 감정 차트 개수:`, report.emotionCharts ? report.emotionCharts.length : 0);
    
    if (report.emotionCharts && report.emotionCharts.length > 0) {
        console.log(`✅ 감정 차트 데이터가 있습니다. 차트를 생성합니다.`);
        const ctx = document.getElementById('emotionTrendChart').getContext('2d');
        
        emotionChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: report.dayLabels || ['월', '화', '수', '목', '금', '토', '일'],
                datasets: report.emotionCharts.map(e => ({
                    label: e.emotionLabel,
                    data: e.emotionData,
                    borderColor: e.borderColor,
                    backgroundColor: e.backgroundColor,
                    fill: true,
                    tension: 0.3,
                    borderWidth: 2
                }))
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        position: 'top', 
                        labels: { 
                            color: '#495235', 
                            font: { family: 'Noto Sans KR', size: 14 },
                            usePointStyle: true,
                            padding: 20
                        } 
                    },
                    title: { display: false }
                },
                scales: {
                    x: { 
                        grid: { color: '#E0E0E0' }, 
                        ticks: { color: '#495235', font: { family: 'Noto Sans KR' } } 
                    },
                    y: { 
                        beginAtZero: true, 
                        max: 10, 
                        grid: { color: '#E0E0E0' }, 
                        ticks: { color: '#495235', font: { family: 'Noto Sans KR' } } 
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                }
            }
        });
    } else {
        // 감정 차트 데이터가 없을 때 빈 차트 표시
        const ctx = document.getElementById('emotionTrendChart').getContext('2d');
        emotionChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['월', '화', '수', '목', '금', '토', '일'],
                datasets: []
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    title: { 
                        display: true, 
                        text: '감정 데이터가 없습니다',
                        color: '#8F9562',
                        font: { family: 'Noto Sans KR', size: 16 }
                    }
                },
                scales: {
                    x: { grid: { color: '#E0E0E0' }, ticks: { color: '#495235', font: { family: 'Noto Sans KR' } } },
                    y: { beginAtZero: true, max: 10, grid: { color: '#E0E0E0' }, ticks: { color: '#495235', font: { family: 'Noto Sans KR' } } }
                }
            }
        });
    }

    // 버튼 상태 업데이트
    prevWeekBtn.disabled = currentIndex >= validOffsets.length - 1;
    nextWeekBtn.disabled = currentIndex <= 0;

    prevWeekBtn.classList.toggle('opacity-50', prevWeekBtn.disabled);
    prevWeekBtn.classList.toggle('cursor-not-allowed', prevWeekBtn.disabled);
    nextWeekBtn.classList.toggle('opacity-50', nextWeekBtn.disabled);
    nextWeekBtn.classList.toggle('cursor-not-allowed', nextWeekBtn.disabled);
}

// 📌 주차 목록 로딩 및 초기화
async function initReportPage() {
    // URL 파라미터에서 weekOffset 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const weekOffset = urlParams.get('weekOffset');

    // userId가 없으면 로그인 페이지로 리다이렉트
    if (!reportUserId) {
        console.error('사용자 ID가 없습니다. 로그인이 필요합니다.');
        window.location.href = '/?loginRequired=true';
        return;
    }

    // 사용 가능한 주차 목록 가져오기
    const res = await fetch(`/api/report/weeks?userId=${reportUserId}`);
    validOffsets = await res.json();
    console.log('사용 가능한 weekOffsets:', validOffsets);

    if (validOffsets.length === 0) {
        currentWeekDisplay.innerText = '리포트 없음';
        return;
    }

    // URL에서 전달받은 weekOffset이 있으면 해당 주차로 설정
    if (weekOffset !== null) {
        const targetWeekOffset = parseInt(weekOffset);
        console.log(`URL에서 받은 weekOffset: ${targetWeekOffset}`);
        
        // 해당 weekOffset이 validOffsets에 있는지 확인
        const weekIndex = validOffsets.indexOf(targetWeekOffset);
        if (weekIndex !== -1) {
            currentIndex = weekIndex;
            console.log(`✅ 해당 주차를 찾았습니다. 인덱스: ${currentIndex}`);
        } else {
            // 해당 주차가 없으면 가장 가까운 주차로 설정
            console.log(`⚠️ 해당 주차(${targetWeekOffset})를 찾을 수 없습니다. 가장 가까운 주차로 설정합니다.`);
            
            let closestIndex = 0;
            let minDiff = Math.abs(validOffsets[0] - targetWeekOffset);
            
            for (let i = 1; i < validOffsets.length; i++) {
                const diff = Math.abs(validOffsets[i] - targetWeekOffset);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            }
            
            currentIndex = closestIndex;
            console.log(`✅ 가장 가까운 주차로 설정: ${validOffsets[currentIndex]} (인덱스: ${currentIndex})`);
        }
    } else {
        currentIndex = 0;
        console.log(`URL에 weekOffset이 없어서 첫 번째 주차로 설정: ${validOffsets[currentIndex]}`);
    }
    
    // 현재 주차의 데이터를 즉시 로드
    const currentWeekOffset = validOffsets[currentIndex];
    console.log(`🔍 현재 주차 데이터 로드: weekOffset=${currentWeekOffset}`);
    await updateReportContent(currentWeekOffset);
}

// 📌 버튼 이벤트 핸들러
prevWeekBtn.addEventListener('click', () => {
    if (currentIndex < validOffsets.length - 1) {
        currentIndex++;
        const weekOffset = validOffsets[currentIndex];
        
        // URL 업데이트
        const newUrl = new URL(window.location);
        newUrl.searchParams.set('weekOffset', weekOffset);
        window.history.pushState({}, '', newUrl);
        
        // 새로운 주차 데이터 로드
        updateReportContent(weekOffset);
    }
});

nextWeekBtn.addEventListener('click', () => {
    if (currentIndex > 0) {
        currentIndex--;
        const weekOffset = validOffsets[currentIndex];
        
        // URL 업데이트
        const newUrl = new URL(window.location);
        newUrl.searchParams.set('weekOffset', weekOffset);
        window.history.pushState({}, '', newUrl);
        
        // 새로운 주차 데이터 로드
        updateReportContent(weekOffset);
    }
});

document.getElementById('go-chat').addEventListener('click', () => {
    // 채팅 페이지로 이동할 때 현재 사용자 정보도 함께 전달
    const chatUrl = reportUserId ? `/chat?userId=${reportUserId}` : '/chat';
    window.location.href = chatUrl;
});

// 📌 페이지 로드 시 초기화
window.addEventListener('load', () => {
    initReportPage();
});