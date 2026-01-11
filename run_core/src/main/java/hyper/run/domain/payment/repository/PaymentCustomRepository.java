package hyper.run.domain.payment.repository;

import hyper.run.domain.payment.dto.request.PaymentSearchRequest;
import hyper.run.domain.payment.dto.response.AdminPaymentResponse;
import hyper.run.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

public interface PaymentCustomRepository{
    Page<AdminPaymentResponse> searchPayments(PaymentSearchRequest searchRequest, Pageable pageable);
}
