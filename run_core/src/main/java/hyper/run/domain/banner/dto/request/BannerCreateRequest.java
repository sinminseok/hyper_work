package hyper.run.domain.banner.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerCreateRequest {
    private String title;
    private Integer displayOrder;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
