package hyper.run.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleReceiptService {

    /**
     * Google Play 영수증 검증 메서드 (구현 예정)
     * @param orderId Google Play 주문 ID
     * @param productId 상품 ID
     * @param purchaseToken 구매 토큰
     */
    @Transactional
    public void verifyReceipt(String orderId, String productId, String purchaseToken) {
        // TODO: Google Play Developer API를 사용한 영수증 검증 구현
        log.warn("Google Play 영수증 검증 미구현: orderId={}, productId={}", orderId, productId);
        throw new UnsupportedOperationException("Google Play 영수증 검증은 아직 구현되지 않았습니다.");
    }
}
