package hyper.run.banner;

import hyper.run.domain.banner.dto.response.BannerResponse;
import hyper.run.domain.banner.service.BannerService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/banners")
public class BannerController {

    private final BannerService bannerService;

    /**
     * 활성화된 배너 목록 조회 API
     * - ACTIVE 상태이고 현재 날짜가 유효한 배너만 반환
     * - displayOrder 기준 오름차순 정렬
     */
    @GetMapping
    public ResponseEntity<?> getActiveBanners() {
        List<BannerResponse> banners = bannerService.getActiveBanners();
        SuccessResponse response = new SuccessResponse(true, "배너 목록 조회 성공", banners);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
