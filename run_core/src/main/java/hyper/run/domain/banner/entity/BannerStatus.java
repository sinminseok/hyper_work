package hyper.run.domain.banner.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BannerStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;
}
