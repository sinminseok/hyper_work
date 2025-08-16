
declare var flatpickr: any;
export type PaymentState = 'PAYMENT_COMPLETED' | 'REFUND_REQUESTED' | 'REFUND_REJECTED' | 'REFUND_COMPLETED';

const paymentStateToKorean: { [key in PaymentState]: string } = {
    'PAYMENT_COMPLETED': '결제 완료',
    'REFUND_REQUESTED': '환불 요청중',
    'REFUND_REJECTED': '환불 거절',
    'REFUND_COMPLETED': '환불 완료'
};

// 인터페이스 정의
interface Payment {
    paymentId: number;
    paymentAt: string;
    paymentMethod: string;
    price: number;
    userName: string;
    paymentState: PaymentState;
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
    keyword?: string;

    startDate?: string;
    endDate?: string;
    minAmount?: number;
    maxAmount?: number;
    state?: PaymentState;
}

// --- 상태 관리 (State Management) ---
const filterState: FilterState = {
    page: 0,
    size: 10,
    sort: 'paymentAt,asc',
};

async function fetchPayments() {
    const params = new URLSearchParams({
        page: filterState.page.toString(),
        size: filterState.size.toString(),
        sort: filterState.sort,
    })

    if (filterState.keyword) params.append('keyword', filterState.keyword);
    if (filterState.startDate) params.append('startDate', filterState.startDate);
    if (filterState.endDate) params.append('endDate', filterState.endDate);
    if (filterState.minAmount) params.append('minAmount', filterState.minAmount.toString());
    if (filterState.maxAmount) params.append('maxAmount', filterState.maxAmount.toString());
    if (filterState.state) params.append('state', filterState.state);

    const PAYMENT_URL = `http://localhost:8080/v1/api/admin/payments?${params.toString()}`;
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        console.error('인증 토큰이 없습니다.');
        window.location.href = 'login.html';
        return;
    }

    try {
        const response = await fetch(PAYMENT_URL, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${accessToken}` }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const responseData = await response.json();
        const data: PageResponse<Payment> = responseData.data;

        renderTable(data);
        renderPagination(data);
        updateTotalCount(data.totalElements);

    } catch (error) {
        console.error("데이터를 불러오는 중 오류 발생:", error);
        const tableBody = document.getElementById('data-table-body');
        if (tableBody) {
            tableBody.innerHTML = '<tr><td colspan="8">데이터를 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
    }
}
    
// --- 렌더링 함수 (Rendering Functions) ---
function renderTable(pageData: PageResponse<Payment>): void {
    const tableBody = document.getElementById('data-table-body') as HTMLTableSectionElement;

    if (!tableBody) return;

    const { content: payment, number: currentPage, size: pageSize } = pageData;

    if (payment.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5">데이터가 없습니다.</td></tr>';
        return;
    }
    tableBody.innerHTML = payment.map((payment, index) => {
        // 숫자를 한국 원화 형식으로 변환
        const formattedAmount = payment.price.toLocaleString('ko-KR', { 
            style: 'currency', 
            currency: 'KRW' 
        });

        const rowNum = currentPage * pageSize + index + 1;

        // paymentStateToKorean 맵을 사용하여 한글로 변환
        const koreanPaymentState = paymentStateToKorean[payment.paymentState] || payment.paymentState;

        // '환불 요청중' 상태일 때만 clickable-row 클래스 추가
        const isClickable = payment.paymentState === 'REFUND_REQUESTED';
        const rowClass = isClickable ? 'clickable-row' : '';

        return `
        <tr class="${rowClass}" data-payment-id="${payment.paymentId}"   data-payment-info='${JSON.stringify(payment)}'>
            <td>${rowNum}</td>
            <td>${payment.paymentAt.split('T')[0]}</td>
            <td>${payment.paymentMethod}</td>
            <td>${formattedAmount}</td>
            <td>${payment.userName}</td>
            <td>${koreanPaymentState}</td>
        </tr>
    `;
    }).join('');
}

/** 테이블 행 클릭 시 상세 페이지로 이동하는 핸들러 */
function handleTableRowClick(event: MouseEvent) {
    const row = (event.target as HTMLElement).closest<HTMLTableRowElement>('tr');
    
    //  clickable-row 클래스가 있는 행만 반응하도록 변경
    if (!row || !row.classList.contains('clickable-row')) return;

    const paymentId = row.dataset.paymentId;
    const paymentInfoString = row.dataset.paymentInfo;
    if (!paymentId || !paymentInfoString) return;

    sessionStorage.setItem('selectedPaymentDetail',paymentInfoString);


    // 모달을 띄우는 대신, paymentId를 가지고 새로운 페이지로 이동
    window.location.href = `refund-detail.html?paymentId=${paymentId}`;
}

function renderPagination(pageData: PageResponse<Payment>): void {
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

function handleSearchWithFilters() {
    const keyword = (document.getElementById('searchInput') as HTMLInputElement).value;
    const startDate = (document.getElementById('filter-start-date') as HTMLInputElement).value;
    const endDate = (document.getElementById('filter-end-date') as HTMLInputElement).value;
    const minAmount = (document.getElementById('filter-min-amount') as HTMLInputElement).value;
    const maxAmount = (document.getElementById('filter-max-amount') as HTMLInputElement).value;
    const state = (document.getElementById('filter-state') as HTMLSelectElement).value;
    

    filterState.keyword = keyword;
    filterState.startDate = startDate;
    filterState.endDate = endDate;
    filterState.minAmount = minAmount ? parseInt(minAmount, 10) : undefined;
    filterState.maxAmount = maxAmount ? parseInt(maxAmount, 10) : undefined;
    
    if (state === "") {
        delete filterState.state;
    } else {
        filterState.state = state as PaymentState;
    }
    filterState.page = 0;

    fetchPayments();
}

function handlePaginationClick(event: MouseEvent) {
    event.preventDefault();
    const target = event.target as HTMLElement;
    if (target.tagName === 'A' && !target.classList.contains('disabled') && target.dataset.page) {
        filterState.page = parseInt(target.dataset.page, 10);
        fetchPayments();
    }
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
    }  else if (wrapper.id === 'filter-status-dropdown') {
        // 팝업 안의 필터는 '검색' 버튼을 눌렀을 때만 적용되므로,
        // 여기서는 filterState 값만 변경합니다.
        filterState.state = selectedValue as PaymentState;;
    }
    fetchPayments();
}
function handleCheckboxOptionClick(event: MouseEvent) {
    event.stopPropagation(); // 드롭다운 닫힘 방지
    const wrapper = (event.target as HTMLElement).closest<HTMLElement>('#sort-dropdown');
    if (!wrapper) return;
    
    const selectedOption = wrapper.querySelector<HTMLElement>('.custom-option.selected');
    const sortBy = selectedOption?.dataset.value || 'paymentAt';
    const isAsc = (event.target as HTMLInputElement).checked;
    filterState.sort = `${sortBy},${isAsc ? 'asc' : 'desc'}`;
    fetchPayments();
}

// 이벤트 리스너 바인딩 
function bindEventListeners(): void {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const paginationContainer = document.getElementById('pagination-container');
    const allDropdowns = document.querySelectorAll<HTMLElement>('.custom-select-wrapper');
    const toggleButton = document.getElementById('toggle-filter-popup');
    const filterPopup = document.getElementById('search-filter-popup');

    const tableBody = document.getElementById('data-table-body');
    tableBody?.addEventListener('click', handleTableRowClick);

    searchButton?.addEventListener('click', handleSearchWithFilters);
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') handleSearchWithFilters();
    });

    paginationContainer?.addEventListener('click', handlePaginationClick);

    toggleButton?.addEventListener('click', (event) => {
        event.stopPropagation();
        filterPopup?.classList.toggle('hidden');
    });

    document.addEventListener('click', (event) => {
        if (filterPopup && toggleButton && !filterPopup.contains(event.target as Node) && event.target !== toggleButton) {
            filterPopup.classList.add('hidden');
        }
    });

    allDropdowns.forEach(wrapper => {
        const trigger = wrapper.querySelector<HTMLElement>('.custom-select-trigger');
        trigger?.addEventListener('click', handleDropdownTriggerClick);

        wrapper.querySelectorAll<HTMLElement>('.custom-option').forEach(option => {
            if (option.classList.contains('checkbox-option')) {
                option.querySelector('input')?.addEventListener('click', handleCheckboxOptionClick);
            } else {
                option.addEventListener('click', handleDropdownOptionClick);
            }
        });
    });
} 
/** 날짜 범위 변경을 처리하는 함수 (flatpickr용) */
function handleDateChange(selectedDates: Date[]) {
    if (selectedDates.length === 2) {
        filterState.startDate = selectedDates[0].toISOString().split('T')[0];
        filterState.endDate = selectedDates[1].toISOString().split('T')[0];
        filterState.page = 0;
        fetchPayments();
    }
}

/** 날짜 선택기 UI의 텍스트를 업데이트하는 함수 (flatpickr용) */
function updateDateInput(selectedDates: Date[], dateStr: string, instance: FlatpickrInstance) {
    // flatpickr의 input 요소에 선택된 날짜 문자열을 표시합니다.
    instance.input.value = dateStr;
}
type FlatpickrInstance = {
    input: HTMLInputElement;
};


function initializeApp(): void {
    bindEventListeners();
    fetchPayments();

    flatpickr("#date-range-container", {
        wrap: true,
        mode: "range",
        dateFormat: "Y. m. d",
        defaultDate: [filterState.startDate, filterState.endDate],
        locale: "ko",
        onChange: handleDateChange,
        onReady: updateDateInput, // UI 준비 시에도 input 업데이트
    });
}

document.addEventListener('DOMContentLoaded', initializeApp);