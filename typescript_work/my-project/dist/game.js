const today = new Date();
const oneWeekAgo = new Date(today.setDate(today.getDate() - 7));
const todayStr = new Date().toISOString().split('T')[0];
const oneWeekAgoStr = oneWeekAgo.toISOString().split('T')[0];
// --- 상태 및 상수 관리 ---
/** 필터의 현재 상태를 저장하고 관리하는 전역 변수 */
const filterState = {
    status: 'FINISHED',
    page: 0,
    size: 6,
    sort: 'createdAt,desc',
    keyword: '',
    startDate: oneWeekAgoStr,
    endDate: todayStr,
};
async function fetchGames() {
    const params = new URLSearchParams({
        status: filterState.status,
        page: filterState.page.toString(),
        size: filterState.size.toString(),
        sort: filterState.sort,
    });
    // 필터링 동적 추가
    if (filterState.keyword) {
        params.append('keyword', filterState.keyword);
    }
    if (filterState.startDate && filterState.endDate) {
        params.append('startDate', filterState.startDate);
        params.append('endDate', filterState.endDate);
    }
    const GAME_URL = `http://localhost:8080/v1/api/admin/games?${params.toString()}`;
    const accessToken = localStorage.getItem('accessToken'); // 토큰이 있는 사용자에게만 허용
    if (!accessToken) {
        console.error('인증 토큰이 없습니다.');
        window.location.href = 'login.html';
        return;
    }
    try {
        console.log(`요청 URL: ${GAME_URL}`);
        const response = await fetch(GAME_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                console.error('인증에 실패했거나 권한이 없습니다.');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const responseDate = await response.json();
        const data = responseDate.data;
        // 3. 받아온 데이터로 각 UI 컴포넌트를 업데이트합니다.
        renderTable(data);
        renderPagination(data);
        updateTotalCount(data.totalElements);
    }
    catch (error) {
        console.error("데이터를 불러오는 중 오류 발생:", error);
        const tableBody = document.getElementById('data-table-body');
        if (tableBody) {
            tableBody.innerHTML = '<tr><td colspan="4">데이터를 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
    }
}
// fetchGames 가 받아온 데이터를 html 로 변환하여 화면에 표시하는 역할
function renderTable(pageData) {
    const tableBody = document.getElementById('data-table-body');
    if (!tableBody)
        return;
    const { content: games, totalElements, number: currentPage, size: pageSize } = pageData;
    if (games.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4">데이터가 없습니다.</td></tr>';
        return;
    }
    tableBody.innerHTML = games.map((game, index) => {
        const rowNum = currentPage * pageSize + index + 1;
        return `
        <tr>
            <td>${rowNum}</td>
            <td>${game.name}</td>
            <td>${game.createdAt.split('T')[0]}</td>
            <td>${game.modifiedAt.split('T')[0]}</td>
        </tr>
    `;
    }).join('');
}
/**
 * 전체 데이터 개수를 받아와 '총 n건' 텍스트를 업데이트합니다.
 * @param total - 전체 데이터 개수
 */
function updateTotalCount(total) {
    const countElement = document.getElementById('total-count');
    if (countElement) {
        countElement.textContent = `총 ${total}건`;
    }
}
/**
 * 페이지네이션 데이터를 받아와 페이지 번호 버튼들을 동적으로 생성합니다.
 * @param pageData - 서버에서 받아온 페이지네이션 정보 객체
 */
function renderPagination(pageData) {
    const { totalPages, number: currentPage } = pageData;
    const container = document.getElementById('pagination-container');
    if (!container)
        return;
    container.innerHTML = ''; // 기존 페이지 버튼들 초기화
    // 이전 페이지 버튼
    const prevBtn = document.createElement('a');
    prevBtn.href = '#';
    prevBtn.className = 'page-arrow';
    prevBtn.textContent = '<';
    if (currentPage === 0)
        prevBtn.classList.add('disabled');
    prevBtn.dataset.page = (currentPage - 1).toString();
    container.appendChild(prevBtn);
    // 페이지 번호 버튼들
    for (let i = 0; i < totalPages; i++) {
        const pageLink = document.createElement('a');
        pageLink.href = '#';
        pageLink.className = 'page-link';
        pageLink.textContent = (i + 1).toString();
        pageLink.dataset.page = i.toString();
        if (i === currentPage) {
            pageLink.classList.add('active');
        }
        container.appendChild(pageLink);
    }
    // 다음 페이지 버튼
    const nextBtn = document.createElement('a');
    nextBtn.href = '#';
    nextBtn.className = 'page-arrow';
    nextBtn.textContent = '>';
    if (currentPage >= totalPages - 1)
        nextBtn.classList.add('disabled');
    nextBtn.dataset.page = (currentPage + 1).toString();
    container.appendChild(nextBtn);
}
document.addEventListener('DOMContentLoaded', () => {
    // --- HTML 요소 선택 ---
    const tabButtons = document.querySelectorAll('.tab-btn');
    const sortSelect = document.getElementById('sort-select');
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const paginationContainer = document.getElementById('pagination-container');
    // --- 이벤트 리스너 설정 ---
    // 1. 탭 버튼 클릭 이벤트
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // 클릭된 버튼의 status 불러와서 filterState 업데이트
            const newStatus = button.dataset.status;
            if (newStatus && newStatus !== filterState.status) {
                filterState.status = newStatus;
                filterState.page = 0;
                fetchGames();
                tabButtons.forEach(btn => btn.classList.remove('active'));
                button.classList.add('active');
            }
        });
    });
    // 2. 정렬 방식 변경 이벤트
    sortSelect?.addEventListener('change', (e) => {
        filterState.sort = e.target.value;
        fetchGames();
    });
    // 3. 검색 기능 (버튼 클릭 또는 엔터)
    const handleSearch = () => {
        filterState.keyword = searchInput.value;
        filterState.page = 0; // 검색 시 1페이지로 초기화
        fetchGames();
    };
    searchButton?.addEventListener('click', handleSearch);
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });
    // 4. 페이지네이션 클릭 이벤트
    paginationContainer?.addEventListener('click', (e) => {
        e.preventDefault();
        const target = e.target;
        if (target.tagName === 'A' && !target.classList.contains('disabled')) {
            const page = target.dataset.page;
            if (page) {
                filterState.page = parseInt(page, 10);
                fetchGames();
            }
        }
    });
    // 5. 날짜 선택기 (Flatpickr) 설정 - 수정됨
    const formatApiDate = (date) => {
        return date.toISOString().split('T')[0];
    };
    flatpickr("#date-range-container", {
        wrap: true,
        mode: "range",
        dateFormat: "Y. m. d",
        defaultDate: [filterState.startDate, filterState.endDate],
        locale: "ko",
        // onChange 함수의 파라미터에 명시적으로 타입을 지정
        onChange: (selectedDates, dateStr, instance) => {
            instance.input.value = dateStr;
            if (selectedDates.length === 2) {
                filterState.startDate = selectedDates[0].toISOString().split('T')[0];
                filterState.endDate = selectedDates[1].toISOString().split('T')[0];
                filterState.page = 0;
                fetchGames();
            }
        },
        onReady: (selectedDates, dateStr, instance) => {
            instance.input.value = dateStr;
        }
    });
    // 페이지가 처음 열릴 때 데이터 로드
    fetchGames();
});
export {};
