package hyper.run.domain.banner.dto.response;

import hyper.run.domain.banner.entity.Banner;
import hyper.run.domain.banner.entity.BannerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBannerListResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private Integer displayOrder;
    private BannerStatus status;
    private String statusDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startDateFormatted;
    private String endDateFormatted;
    private LocalDateTime createDateTime;
    private String createDateTimeFormatted;

    public static AdminBannerListResponse from(Banner banner) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return AdminBannerListResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .displayOrder(banner.getDisplayOrder())
                .status(banner.getStatus())
                .statusDescription(banner.getStatus().getDescription())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .startDateFormatted(banner.getStartDate() != null ? banner.getStartDate().format(dateFormatter) : "-")
                .endDateFormatted(banner.getEndDate() != null ? banner.getEndDate().format(dateFormatter) : "-")
                .createDateTime(banner.getCreateDateTime())
                .createDateTimeFormatted(banner.getCreateDateTime() != null ? banner.getCreateDateTime().format(dateTimeFormatter) : "-")
                .build();
    }
}
