// --- 인터페이스 정의 (Interfaces) ---
interface User {
    email: string;
    name: string;
    phoneNumber: string;
    birth: string;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

interface FilterState {
    page: number;
    sort: string;
    size: number;
    keyword: string;
    searchCategory: string;
}

// --- 상태 관리 (State Management) ---
const filterState: FilterState = {
    page: 0,
    size: 6,
    sort: 'name,asc',
    keyword: '',
    searchCategory: 'name',
};

let currentEmail: string | null = null; // 선택된 사용자 ID 를 저장할 변수

// --- API 호출 (API Calls) ---
async function fetchUsers() {
    const params = new URLSearchParams({
        page: filterState.page.toString(),
        size: filterState.size.toString(),
        sort: filterState.sort,
    });

    if (filterState.searchCategory && filterState.keyword) {
        params.append('keyword', filterState.keyword);
        params.append('searchCategory', filterState.searchCategory);
    }
    const USER_URL = `http://localhost:8080/v1/api/admin/users?${params.toString()}`;
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        console.error('인증 토큰이 없습니다.');
        window.location.href = 'login.html';
        return;
    }

    try {
        const response = await fetch(USER_URL, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${accessToken}` }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const responseData = await response.json();
        const data: PageResponse<User> = responseData.data;

        renderTable(data);
        renderPagination(data);
        updateTotalCount(data.totalElements);

    } catch (error) {
        console.error("데이터를 불러오는 중 오류 발생:", error);
        const tableBody = document.getElementById('data-table-body');
        if (tableBody) {
            tableBody.innerHTML = '<tr><td colspan="5">데이터를 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
    }
}

async function deleteUser(email: string){
    const DELETE_URL = `http://localhost:8080/v1/api/admin/users/delete/${email}`;
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        console.error('인증 토큰이 없습니다.');
        return;
    }
    
    const response = await fetch(DELETE_URL, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${accessToken}` }
    });

    if (!response.ok) {
        throw new Error('사용자 삭제에 실패했습니다.');
    }
}

// --- 렌더링 함수 (Rendering Functions) ---
function renderTable(pageData: PageResponse<User>): void {
    const tableBody = document.getElementById('data-table-body');
    if (!tableBody) return;

    const { content: users, number: currentPage, size: pageSize } = pageData;

    if (users.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5">데이터가 없습니다.</td></tr>';
        return;
    }
    tableBody.innerHTML = users.map((user, index) => {
        const rowNum = currentPage * pageSize + index + 1;
        return `
        <tr class="user-row" style="cursor: pointer;" data-user-email="${user.email}" data-user-info='${JSON.stringify(user)}'>
            <td>${rowNum}</td>
            <td>${user.email}</td>
            <td>${user.name}</td>
            <td>${user.phoneNumber}</td>
            <td>${user.birth}</td>
        </tr>
    `;
    }).join('');
}

function renderPagination(pageData: PageResponse<User>): void {
    const { totalPages, number: currentPage } = pageData;
    const container = document.getElementById('pagination-container');
    if (!container) return;

    container.innerHTML = '';
    
    // 페이지네이션 로직은 기존과 동일...
    const prevBtn = document.createElement('a');
    prevBtn.href = '#';
    prevBtn.className = 'page-arrow';
    prevBtn.textContent = '<';
    if(currentPage === 0) prevBtn.classList.add('disabled');
    prevBtn.dataset.page = (currentPage - 1).toString();
    container.appendChild(prevBtn);

    for(let i = 0; i < totalPages;i++){
        const pageLink = document.createElement('a');
        pageLink.href = '#'
        pageLink.className = 'page-link';
        pageLink.textContent = (i + 1).toString();
        pageLink.dataset.page = i.toString();
        if(i === currentPage){
            pageLink.classList.add('active');
        }
        container.appendChild(pageLink);
    }

    const nextBtn = document.createElement('a');
    nextBtn.href = '#';
    nextBtn.className = 'page-arrow';
    nextBtn.textContent = '>';
    if(currentPage >= totalPages -1 ) nextBtn.classList.add('disabled');
    nextBtn.dataset.page = (currentPage + 1).toString();
    container.appendChild(nextBtn);
}

function updateTotalCount(total: number): void {
    const countElement = document.getElementById('total-count');
    if (countElement) {
        countElement.textContent = `총 ${total}건`;
    }
}

function closeAllDropdowns(exceptWrapper: HTMLElement | null = null): void {
    document.querySelectorAll<HTMLElement>('.custom-select-wrapper').forEach(wrapper => {
        if (wrapper !== exceptWrapper) {
            wrapper.classList.remove('open');
        }
    });
}

function openModal() {
    document.querySelector('.modal-overlay')?.classList.remove('hidden');
}

function closeModal() {
    document.querySelector('.modal-overlay')?.classList.add('hidden');
}

function showUserModal(modalId : string) {
    const modal = document.getElementById(modalId);
    if(modal){
        modal.classList.remove('hidden');
    }
}
function hideUserModal(modalId: string) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('hidden');
    }
}


// --- 이벤트 핸들러 (Event Handlers) ---
function handleSearch() {
    const searchInput = document.getElementById('searchInput') as HTMLInputElement;
    if (searchInput) {
        filterState.keyword = searchInput.value;
        filterState.page = 0;
        fetchUsers();
    }
}

function handlePaginationClick(event: MouseEvent) {
    event.preventDefault();
    const target = event.target as HTMLElement;
    if (target.tagName === 'A' && !target.classList.contains('disabled') && target.dataset.page) {
        filterState.page = parseInt(target.dataset.page, 10);
        fetchUsers();
    }
}

/** 테이블 행 클릭 시 모달을 여는 핸들러 (이벤트 위임 방식) */
function handleTableRowClick(event: MouseEvent) {
    const row = (event.target as HTMLElement).closest<HTMLTableRowElement>('.user-row');
    if (!row) return; // 행이 아닌 다른 곳을 클릭했다면 무시

    
    const userInfoString = row.dataset.userInfo;
    if (!userInfoString) return;

    const user: User = JSON.parse(userInfoString);
    currentEmail = user.email;
    
    // 팝업 안의 input 필드 채우기
    (document.getElementById('user-name') as HTMLInputElement).value = user.name;
    (document.getElementById('user-email') as HTMLInputElement).value = user.email;
    (document.getElementById('user-phoneNumber') as HTMLInputElement).value = user.phoneNumber;
    (document.getElementById('user-birth') as HTMLInputElement).value = user.birth;
    

    showModal('user-detail-modal');
}

function handleDropdownTriggerClick(event: MouseEvent): void {
    event.stopPropagation();
    const wrapper = (event.currentTarget as HTMLElement).closest<HTMLElement>('.custom-select-wrapper');
    if (wrapper) {
        closeAllDropdowns(wrapper);
        wrapper.classList.toggle('open');
    }
}

function handleDropdownOptionClick(event: MouseEvent): void {
    const clickedOption = event.currentTarget as HTMLElement;
    const wrapper = clickedOption.closest<HTMLElement>('.custom-select-wrapper');
    if (!wrapper) return;
    
    // UI 업데이트
    const triggerText = wrapper.querySelector('.custom-select-trigger span');
    const currentlySelected = wrapper.querySelector('.custom-option.selected');
    if (currentlySelected) currentlySelected.classList.remove('selected');
    clickedOption.classList.add('selected');
    if(triggerText) triggerText.textContent = clickedOption.textContent;
    wrapper.classList.remove('open');

    // filterState 업데이트 및 데이터 요청
    const selectedValue = clickedOption.dataset.value;
    if (!selectedValue) return;

    if (wrapper.id === 'sort-select-wrapper') {
        filterState.sort = selectedValue;
    } else if (wrapper.id === 'search-category-wrapper') {
        filterState.searchCategory = selectedValue;
    }
    fetchUsers();
}

function handleCheckboxOptionClick(event: MouseEvent): void {
    event.stopPropagation();
}
// 상세 정보 모달의 '삭제' 버튼 클릭 핸들러
function handleDeleteBtnClick() {
    console.log("삭제 버튼 클릭됨! 확인 창을 띄웁니다.");
    showModal('delete-confirm-modal');
}

// 삭제 확인 모달의 '확인' 버튼 클릭 핸들러
async function handleConfirmDelete() {
    if (!currentEmail) {
        alert('삭제할 사용자가 선택되지 않았습니다.');
        return;
    }
    try {
        await deleteUser(currentEmail);
        hideModal('delete-confirm-modal');
        showModal('delete-success-modal');
    } catch (error) {
        console.error(error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// 삭제 확인 모달의 '취소' 버튼 클릭 핸들러
function handleCancelDelete() {
    hideModal('delete-confirm-modal');
}

// 삭제 완료 모달의 '확인' 버튼 클릭 핸들러
function handleSuccessOk() {
    hideModal('delete-success-modal');
    hideModal('user-detail-modal'); // 상세 정보 모달도 닫기
    fetchUsers(); // 사용자 목록 새로고침
}

// --- 이벤트 리스너 바인딩 (Event Listener Binding) ---
function bindUsereventListeners(): void {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');

    const paginationContainer = document.getElementById('pagination-container');
    const tableBody = document.getElementById('data-table-body');

    const closeBtn = document.querySelector('.close-btn');

    const userDetailModalCloseBtn = document.querySelector('#user-detail-modal .close-btn');
    const mainDeleteBtn = document.getElementById('delete-user-btn');
    const cancelDeleteBtn = document.querySelector('#delete-confirm-modal .cancel-btn');
    const confirmDeleteBtn = document.querySelector('#delete-confirm-modal .confirm-btn');

    const successOkBtn = document.querySelector('#delete-success-modal .confirm-btn');
    const allDropdowns = document.querySelectorAll<HTMLElement>('.custom-select-wrapper');

    // 검색 기능
    searchButton?.addEventListener('click', handleSearch);
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') handleSearch();
    });

    // 페이지네이션
    paginationContainer?.addEventListener('click', handlePaginationClick);

    // 테이블 행 클릭 (이벤트 위임)
    tableBody?.addEventListener('click', handleTableRowClick);
    
    // 모달 닫기
    closeBtn?.addEventListener('click', closeModal);

     // 상세 정보 모달
     userDetailModalCloseBtn?.addEventListener('click', () => hideModal('user-detail-modal'));
     mainDeleteBtn?.addEventListener('click', handleDeleteBtnClick);
 
     // 삭제 확인 모달
     cancelDeleteBtn?.addEventListener('click', handleCancelDelete);
     confirmDeleteBtn?.addEventListener('click', handleConfirmDelete);
     // 삭제 완료 모달
    successOkBtn?.addEventListener('click', handleSuccessOk);

    // 커스텀 드롭다운
    allDropdowns.forEach(wrapper => {
        const trigger = wrapper.querySelector<HTMLElement>('.custom-select-trigger');
        const allOptions = wrapper.querySelectorAll<HTMLElement>('.custom-option');
        trigger?.addEventListener('click', handleDropdownTriggerClick);
        allOptions.forEach(option => {
            if (option.classList.contains('checkbox-option')) {
                option.addEventListener('click', handleCheckboxOptionClick);
            } else {
                option.addEventListener('click', handleDropdownOptionClick);
            }
        });
    });

    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', () => closeAllDropdowns());
}

// --- 애플리케이션 초기화 (Application Initialization) ---
function initializeUserApp(): void {
    bindUsereventListeners();
    fetchUsers();
}

document.addEventListener('DOMContentLoaded', initializeUserApp);