package hyper.run.domain.banner.service;

import hyper.run.domain.banner.dto.response.BannerResponse;
import hyper.run.domain.banner.entity.Banner;
import hyper.run.domain.banner.entity.BannerStatus;
import hyper.run.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    @Value("${cloud.aws.s3.base-url:https://hyper-run-image.s3.ap-northeast-2.amazonaws.com/}")
    private String s3BaseUrl;

    /**
     * 활성화된 배너 목록 조회 (사용자용)
     * - ACTIVE 상태인 배너만 조회
     * - 현재 날짜가 startDate ~ endDate 범위 내인 배너만 포함
     * - displayOrder 기준 오름차순 정렬
     */
    public List<BannerResponse> getActiveBanners() {
        LocalDate today = LocalDate.now();

        List<Banner> banners = bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));

        return banners.stream()
                .filter(banner -> banner.getStatus() == BannerStatus.ACTIVE)
                .filter(banner -> isValidDate(banner, today))
                .map(banner -> BannerResponse.from(banner, s3BaseUrl))
                .collect(Collectors.toList());
    }

    /**
     * 배너의 유효 기간 체크
     * - startDate가 null이면 시작일 제약 없음
     * - endDate가 null이면 종료일 제약 없음
     */
    private boolean isValidDate(Banner banner, LocalDate today) {
        LocalDate startDate = banner.getStartDate();
        LocalDate endDate = banner.getEndDate();

        // startDate 체크 (null이면 통과)
        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }

        // endDate 체크 (null이면 통과)
        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }

        return true;
    }
}
