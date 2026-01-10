// Pointshop JavaScript
// API 기반 데이터
let currentPoints = 0;
let myStamps = [];
let activeStampId = null;

// 포인트 불러오기
function loadUserPoints() {
    fetch('/pointshop/api/points')
        .then(res => res.json())
        .then(point => {
            currentPoints = point;
            updatePointsDisplay();
        });
}

// 도장 목록 불러오기 (필터 적용)
function loadStamps(filter = 'all') {
    fetch(`/pointshop/api/stamps?filter=${filter}`)
        .then(res => res.json())
        .then(data => renderShopItems(data));
}

// 내 도장 목록 불러오기
function loadMyStamps() {
    fetch('/pointshop/api/my-stamps')
        .then(res => res.json())
        .then(data => {
            myStamps = data;
            renderOwnedStamps(data);
        });
}

// 포인트 표시 업데이트
function updatePointsDisplay() {
    const currentPointsDisplay = document.getElementById('current-points');
    const popupCurrentPoints = document.getElementById('popup-current-points');
    if (currentPointsDisplay) {
        currentPointsDisplay.innerText = currentPoints.toLocaleString();
    }
    if (popupCurrentPoints) {
        popupCurrentPoints.innerText = currentPoints.toLocaleString();
    }
}

// 포인트 부족 팝업 표시 함수
function showInsufficientPopup() {
    const popup = document.getElementById('point-insufficient-popup');
    if (popup) {
        popup.classList.remove('hidden');
        popup.style.display = 'flex'; // 팝업 표시
    }
}

// 구매 성공 팝업 표시 함수
function showPurchaseSuccessPopup() {
    const popup = document.getElementById('purchase-success-popup');
    if (popup) {
        popup.classList.remove('hidden');
        popup.style.display = 'flex'; // 팝업 표시
    }
}

// 적용 성공 팝업 표시 함수
function showApplySuccessPopup() {
    const popup = document.getElementById('apply-success-popup');
    if (popup) {
        popup.classList.remove('hidden');
        popup.style.display = 'flex'; // 팝업 표시
    }
}

// 도장 상점 렌더링
function renderShopItems(data) {
    const shopGrid = document.querySelector('#shop-items-section .grid');
    shopGrid.innerHTML = '';
    const currentFilter = document.getElementById('stamp-filter').value;
    data.forEach(item => {
        const { stamp, status, userStampId, isActive } = item;
        const card = document.createElement('div');
        card.className = 'shop-item-card';
        card.dataset.stampId = stamp.stampId;
        let buttonHtml = '';
        
        if (status === 'owned') {
            if (isActive === "Y") {
                // 적용중인 스탬프
                buttonHtml = '<button class="btn-buy disabled" disabled>적용중</button>';
            } else {
                // 보유중이지만 적용되지 않은 스탬프
                buttonHtml = `<button class="btn-buy btn-apply-owned" data-user-stamp-id="${userStampId}">적용하기</button>`;
            }
        } else if (status === 'insufficient') {
            buttonHtml = `<button class="btn-buy insufficient" data-stamp-id="${stamp.stampId}" type="button">구매하기</button>`;
        } else {
            buttonHtml = `<button class="btn-buy" data-stamp-id="${stamp.stampId}" type="button">구매하기</button>`;
        }
        
        card.innerHTML = `
            <img src="/${stamp.image}" alt="${stamp.name}" class="stamp-preview-image-shop">
            <h3 class="text-xl font-semibold text-[#90533B] mb-2">${stamp.name}</h3>
            <p class="text-[#495235] mb-4">${stamp.description}</p>
            <p class="text-lg font-bold text-[#DA983C] mb-4">${stamp.price.toLocaleString()} P</p>
            ${buttonHtml}
        `;
        
        // 적용하기 버튼 이벤트 리스너
        const applyButton = card.querySelector('.btn-apply-owned');
        if (applyButton) {
            applyButton.addEventListener('click', function() {
                const userStampId = this.dataset.userStampId;
                onApplyStamp(userStampId);
            });
        }
        
        // 구매하기 버튼 이벤트 리스너
        const buyButton = card.querySelector('.btn-buy:not(.btn-apply-owned):not(.disabled)');
        if (buyButton) {
            buyButton.addEventListener('click', function() {
                const stampId = card.dataset.stampId;
                onBuyButtonClick(stampId);
            });
        }
        
        shopGrid.appendChild(card);
    });
}

// 구매하기 API 연동
function onBuyButtonClick(stampId) {
    fetch('/pointshop/api/buy', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `stampId=${stampId}`
    })
    .then(res => res.json())
    .then(result => {
        if (result) {
            showPurchaseSuccessPopup();
            loadStamps();
            loadUserPoints();
            loadMyStamps();
        } else {
            // 포인트 부족 시 팝업 표시 (alert 대신)
            showInsufficientPopup();
        }
    })
    .catch(error => {
        console.error('구매 오류:', error);
        // 에러 시에도 팝업 표시
        showInsufficientPopup();
    });
}

// 내 도장 목록 렌더링
function renderOwnedStamps(data) {
    const ownedStampsGrid = document.getElementById('owned-stamps-grid');
    ownedStampsGrid.innerHTML = '';
    if (!data || data.length === 0) {
        ownedStampsGrid.innerHTML = '<p class="col-span-full text-center text-[#8F9562] py-4">아직 보유한 도장이 없어요. 상점에서 새로운 도장을 구매해보세요!</p>';
        return;
    }
    data.forEach(us => {
        const card = document.createElement('div');
        card.className = 'owned-stamp-card';
        // 적용중 뱃지 추가
        const activeBadge = us.isActive === "Y"
            ? '<span class="inline-block bg-[#DA983C] text-white text-xs font-bold px-2 py-1 rounded mb-2">적용중</span>'
            : '';
        card.innerHTML = `
            ${activeBadge}
            <img src="/${us.image}" alt="${us.name}" class="stamp-preview-image-shop">
            <h3 class="text-xl font-semibold text-[#90533B] mb-2">${us.name}</h3>
            <p class="text-[#495235] mb-4">${us.description}</p>
            <button class="btn-apply" data-user-stamp-id="${us.userStampId}">적용하기</button>
        `;
        ownedStampsGrid.appendChild(card);
    });
    // 적용하기 버튼 이벤트 연결
    document.querySelectorAll('.btn-apply').forEach(button => {
        button.addEventListener('click', function() {
            const userStampId = this.dataset.userStampId;
            onApplyStamp(userStampId);
        });
    });
}

// 적용하기 API 연동
function onApplyStamp(userStampId) {
    fetch('/pointshop/api/apply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `userStampId=${userStampId}`
    })
    .then(res => res.json())
    .then(result => {
        if (result) {
            showApplySuccessPopup();
            // 현재 필터에 따라 새로고침
            const currentFilter = document.getElementById('stamp-filter').value;
            loadStamps(currentFilter);
            loadMyStamps();
        } else {
            alert('적용 실패');
        }
    })
    .catch(error => {
        console.error('적용 오류:', error);
        alert('적용 중 오류가 발생했습니다.');
    });
}

// DOM 로드 시 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 팝업 강제 숨김 (최우선)
    const popup = document.getElementById('point-insufficient-popup');
    if (popup) {
        popup.classList.add('hidden');
        popup.style.display = 'none'; // 추가 보안
    }

    const successPopup = document.getElementById('purchase-success-popup');
    if (successPopup) {
        successPopup.classList.add('hidden');
        successPopup.style.display = 'none'; // 추가 보안
    }

    const applySuccessPopup = document.getElementById('apply-success-popup');
    if (applySuccessPopup) {
        applySuccessPopup.classList.add('hidden');
        applySuccessPopup.style.display = 'none'; // 추가 보안
    }

    // 포인트 부족 팝업 닫기 버튼
    const closePopupBtn = document.getElementById('close-popup-btn');
    if (closePopupBtn) {
        closePopupBtn.addEventListener('click', function() {
            const popup = document.getElementById('point-insufficient-popup');
            if (popup) {
                popup.classList.add('hidden');
                popup.style.display = 'none';
            }
        });
    }

    // 구매 성공 팝업 닫기 버튼
    const closeSuccessPopupBtn = document.getElementById('close-success-popup-btn');
    if (closeSuccessPopupBtn) {
        closeSuccessPopupBtn.addEventListener('click', function() {
            const popup = document.getElementById('purchase-success-popup');
            if (popup) {
                popup.classList.add('hidden');
                popup.style.display = 'none';
            }
        });
    }

    // 적용 성공 팝업 닫기 버튼
    const closeApplySuccessPopupBtn = document.getElementById('close-apply-success-popup-btn');
    if (closeApplySuccessPopupBtn) {
        closeApplySuccessPopupBtn.addEventListener('click', function() {
            const popup = document.getElementById('apply-success-popup');
            if (popup) {
                popup.classList.add('hidden');
                popup.style.display = 'none';
            }
        });
    }

    // 포인트 부족 팝업 외부 클릭 시 닫기
    const popupOverlay = document.getElementById('point-insufficient-popup');
    if (popupOverlay) {
        popupOverlay.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.add('hidden');
                this.style.display = 'none';
            }
        });
    }

    // 구매 성공 팝업 외부 클릭 시 닫기
    const successPopupOverlay = document.getElementById('purchase-success-popup');
    if (successPopupOverlay) {
        successPopupOverlay.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.add('hidden');
                this.style.display = 'none';
            }
        });
    }

    // 적용 성공 팝업 외부 클릭 시 닫기
    const applySuccessPopupOverlay = document.getElementById('apply-success-popup');
    if (applySuccessPopupOverlay) {
        applySuccessPopupOverlay.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.add('hidden');
                this.style.display = 'none';
            }
        });
    }

    // 필터 드롭다운 이벤트
    const stampFilter = document.getElementById('stamp-filter');
    if (stampFilter) {
        stampFilter.addEventListener('change', function() {
            loadStamps(this.value);
        });
    }

    // 초기화
    loadUserPoints();
    loadStamps();
    loadMyStamps();
}); 