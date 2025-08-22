package hyper.run.domain.exchange_transaction.service;


import hyper.run.domain.exchange_transaction.dto.request.ExchangeTransactionRequest;
import hyper.run.domain.exchange_transaction.dto.response.AdminExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.dto.response.ExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import hyper.run.domain.exchange_transaction.repository.ExchangeTransactionRepository;
import hyper.run.domain.exchange_transaction.repository.admin.CustomExchangeTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class ExchangeTransactionService {

    private final UserRepository userRepository;
    private final ExchangeTransactionRepository exchangeTransactionRepository;
    private final CustomExchangeTransactionRepository customExchangeTransactionRepository;

    /**
     * 환전 신청 메서드
     */
    @Transactional
    public void save(ExchangeTransactionRequest exchangeTransactionRequest){
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(exchangeTransactionRequest.getUserId()), NOT_EXIST_USER_ID);
        user.validateExchange(exchangeTransactionRequest.getAmount());
        user.decreasePoint(exchangeTransactionRequest.getAmount());
        ExchangeTransaction exchangeTransaction = exchangeTransactionRequest.toEntity();
        exchangeTransactionRepository.save(exchangeTransaction);
    }

    public List<ExchangeTransactionResponse> findMyExchangeHistories(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        List<ExchangeTransaction> byUserId = exchangeTransactionRepository.findByUserId(user.getId());
        return byUserId.stream()
                .map(ExchangeTransactionResponse::from)
                .collect(Collectors.toList());
    }


}
