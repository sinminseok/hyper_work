package hyper.run.domain.banner.entity;

import hyper.run.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(
        name = "banner",
        indexes = {
                @Index(name = "idx_banner_display_order", columnList = "display_order"),
                @Index(name = "idx_banner_status", columnList = "status")
        }
)
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Banner extends BaseTimeEntity<Banner> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BannerStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
