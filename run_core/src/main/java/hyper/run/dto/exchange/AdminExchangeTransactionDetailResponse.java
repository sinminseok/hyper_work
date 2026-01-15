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
public class AdminExchangeTransactionDetailResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhoneNumber;
    private double amount;
    private String accountNumber;
    private String bankName;
    private ExchangeStatus status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdAtFormatted;
    private String updatedAtFormatted;

    public static AdminExchangeTransactionDetailResponse from(ExchangeTransaction transaction, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = transaction.getCreateDateTime() != null
                ? transaction.getCreateDateTime().format(formatter)
                : "-";
        String updatedAtFormatted = transaction.getModifiedDateTime() != null
                ? transaction.getModifiedDateTime().format(formatter)
                : "-";

        String statusName = getStatusNameInKorean(transaction.getExchangeStatus());

        return AdminExchangeTransactionDetailResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userEmail(user != null ? user.getEmail() : "-")
                .userName(user != null ? user.getName() : "-")
                .userPhoneNumber(user != null ? user.getPhoneNumber() : "-")
                .amount(transaction.getAmount())
                .accountNumber(transaction.getAccountNumber())
                .bankName(transaction.getBankName())
                .status(transaction.getExchangeStatus())
                .statusName(statusName)
                .createdAt(transaction.getCreateDateTime())
                .updatedAt(transaction.getModifiedDateTime())
                .createdAtFormatted(createdAtFormatted)
                .updatedAtFormatted(updatedAtFormatted)
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
