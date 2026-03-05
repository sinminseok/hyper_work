package hyper.run.domain.payment.service;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.TransactionInfoResponse;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import hyper.run.domain.payment.dto.response.AppleTransactionResponse;
import hyper.run.domain.payment.entity.AppleTransaction;
import hyper.run.domain.payment.repository.AppleTransactionRepository;
import hyper.run.exception.custom.DuplicatedTransactionException;
import hyper.run.exception.custom.InvalidReceiptException;
import hyper.run.exception.custom.ReceiptVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

import static hyper.run.exception.ErrorMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleReceiptService {

    private final AppleTransactionRepository appleTransactionRepository;
    private final AppStoreServerAPIClient appStoreServerAPIClient;
    private final SignedDataVerifier signedDataVerifier;

    /**
     * Apple 영수증 검증 메서드 (App Store Server API v2)
     * @param transactionId Apple 거래 ID
     * @param productId 상품 ID
     * @param receiptData 하위 호환성을 위해 유지 (사용하지 않음)
     */
    @Transactional
    public AppleTransactionResponse verifyReceipt(String transactionId, String productId, String receiptData) {
        if (appleTransactionRepository.existsByTransactionId(transactionId)) {
            throw new DuplicatedTransactionException(DUPLICATED_APPLE_TRANSACTION);
        }

        try {
            TransactionInfoResponse transactionInfoResponse = appStoreServerAPIClient.getTransactionInfo(transactionId);
            String signedTransactionInfo = transactionInfoResponse.getSignedTransactionInfo();

            JWSTransactionDecodedPayload decodedPayload = signedDataVerifier.verifyAndDecodeTransaction(signedTransactionInfo);

            if (!productId.equals(decodedPayload.getProductId())) {
                throw new InvalidReceiptException(INVALID_APPLE_RECEIPT);
            }

            if (decodedPayload.getRevocationDate() != null) {
                throw new InvalidReceiptException(INVALID_APPLE_RECEIPT);
            }

            String environment = decodedPayload.getEnvironment() != null
                    ? decodedPayload.getEnvironment().getValue()
                    : "Production";

            AppleTransactionResponse response = AppleTransactionResponse.builder()
                    .transactionId(decodedPayload.getTransactionId())
                    .productId(decodedPayload.getProductId())
                    .purchaseDate(decodedPayload.getPurchaseDate() != null
                            ? Instant.ofEpochMilli(decodedPayload.getPurchaseDate())
                            : Instant.now())
                    .environment(environment)
                    .status("0")
                    .build();

            AppleTransaction appleTransaction = AppleTransaction.of(response);
            appleTransactionRepository.save(appleTransaction);

            return response;

        } catch (DuplicatedTransactionException | InvalidReceiptException e) {
            throw e;
        } catch (APIException e) {
            log.error("[Apple API v2] API 호출 실패: httpStatus={}, transactionId={}", e.getHttpStatusCode(), transactionId, e);
            throw new InvalidReceiptException(INVALID_APPLE_RECEIPT);
        } catch (VerificationException e) {
            log.error("[Apple API v2] JWS 서명 검증 실패: transactionId={}", transactionId, e);
            throw new InvalidReceiptException(INVALID_APPLE_RECEIPT);
        } catch (IOException e) {
            log.error("[Apple API v2] 통신 오류: transactionId={}", transactionId, e);
            throw new ReceiptVerificationException(APPLE_RECEIPT_VERIFICATION_FAILED);
        } catch (Exception e) {
            log.error("[Apple API v2] 예상치 못한 오류: transactionId={}", transactionId, e);
            throw new ReceiptVerificationException(APPLE_RECEIPT_VERIFICATION_FAILED);
        }
    }
}
