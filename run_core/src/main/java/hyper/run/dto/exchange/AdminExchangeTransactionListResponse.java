package hyper.run.dto.exchange;

import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AdminExchangeTransactionListResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private double amount;
    private ExchangeStatus status;
    private String statusName;
    private LocalDateTime createdAt;
    private String createdAtFormatted;

    public static AdminExchangeTransactionListResponse from(ExchangeTransaction transaction, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = transaction.getCreateDateTime() != null
                ? transaction.getCreateDateTime().format(formatter)
                : "-";

        String statusName = getStatusNameInKorean(transaction.getExchangeStatus());

        return AdminExchangeTransactionListResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userEmail(user != null ? user.getEmail() : "-")
                .userName(user != null ? user.getName() : "-")
                .amount(transaction.getAmount())
                .status(transaction.getExchangeStatus())
                .statusName(statusName)
                .createdAt(transaction.getCreateDateTime())
                .createdAtFormatted(createdAtFormatted)
                .build();
    }

    private static String getStatusNameInKorean(ExchangeStatus status) {
        return switch (status) {
            case REQUESTED -> "신청됨";
            case CANCELLED -> "취소됨";
            case COMPLETED -> "완료됨";
        };
    }
}
