package hyper.run.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Google Play Developer API 구매 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePurchaseResponse {

    /**
     * 구매 상태
     * 0: Purchased (구매됨)
     * 1: Canceled (취소됨)
     * 2: Pending (보류 중)
     */
    @JsonProperty("purchaseState")
    private Integer purchaseState;

    /**
     * 소비 상태
     * 0: Yet to be consumed (아직 소비되지 않음)
     * 1: Consumed (소비됨)
     */
    @JsonProperty("consumptionState")
    private Integer consumptionState;

    /**
     * 개발자가 설정한 페이로드
     */
    @JsonProperty("developerPayload")
    private String developerPayload;

    /**
     * 주문 ID
     */
    @JsonProperty("orderId")
    private String orderId;

    /**
     * 구매 타입
     * 0: Test (테스트 구매)
     * 1: Promo (프로모션 구매)
     */
    @JsonProperty("purchaseType")
    private Integer purchaseType;

    /**
     * 구매 시간 (밀리초)
     */
    @JsonProperty("purchaseTimeMillis")
    private Long purchaseTimeMillis;

    /**
     * 승인 상태
     * 0: Yet to be acknowledged
     * 1: Acknowledged
     */
    @JsonProperty("acknowledgementState")
    private Integer acknowledgementState;

    /**
     * 리전 코드
     */
    @JsonProperty("regionCode")
    private String regionCode;
}
