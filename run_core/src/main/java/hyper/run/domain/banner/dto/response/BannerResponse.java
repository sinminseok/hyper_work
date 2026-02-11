package hyper.run.domain.banner.dto.response;

import hyper.run.domain.banner.entity.Banner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private Integer displayOrder;
    private LocalDate startDate;
    private LocalDate endDate;

    public static BannerResponse from(Banner banner, String s3BaseUrl) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(s3BaseUrl + banner.getImageUrl())
                .displayOrder(banner.getDisplayOrder())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .build();
    }
}
