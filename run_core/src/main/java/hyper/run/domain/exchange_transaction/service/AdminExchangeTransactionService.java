package hyper.run.domain.exchange_transaction.service;

import hyper.run.domain.exchange_transaction.dto.response.AdminExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import hyper.run.domain.exchange_transaction.repository.ExchangeTransactionRepository;
import hyper.run.domain.exchange_transaction.repository.admin.CustomExchangeTransactionRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_EXCHANGE_ID;

@Service
@RequiredArgsConstructor
public class AdminExchangeTransactionService {

    private final UserRepository userRepository;
    private final ExchangeTransactionRepository exchangeTransactionRepository;
    private final CustomExchangeTransactionRepository customExchangeTransactionRepository;
    /** _____관리자_____
     * 환전 조회 메서드
     */
    public Page<AdminExchangeTransactionResponse> findExchanges(LocalDate startDate, LocalDate endDate, String keyword, ExchangeStatus exchangeStatus, Pageable pageable){
        return customExchangeTransactionRepository.findExchanges(startDate,endDate,keyword, exchangeStatus, pageable);
    }


    /**
     * 환전 완료 메서드
     */
    @Transactional
    public void complete(final Long exchangeId) {
        ExchangeTransaction exchangeTransaction = OptionalUtil.getOrElseThrow(exchangeTransactionRepository.findById(exchangeId), NOT_EXIST_EXCHANGE_ID);
        exchangeTransaction.setExchangeStatus(ExchangeStatus.COMPLETED);
    }

    /**
     * 환전 취소 메서드
     */
    @Transactional
    public void cancel(final Long exchangeId) {
        ExchangeTransaction exchangeTransaction = OptionalUtil.getOrElseThrow(exchangeTransactionRepository.findById(exchangeId), NOT_EXIST_EXCHANGE_ID);
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(exchangeTransaction.getUserId()),"존재하지 않는 사용자입니다.");
        double requestedPoint = exchangeTransaction.getAmount();
        user.increasePoint(requestedPoint);
        exchangeTransaction.setExchangeStatus(ExchangeStatus.CANCELLED);
    }
}
