// 코멘트 받을 시간 선택 후 저장 시 팝업 표시
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('saved') === 'true') {
        alert('시간이 저장되었습니다.');
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (urlParams.get('same') === 'true') {
        alert('이미 저장된 시간입니다.');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}); 