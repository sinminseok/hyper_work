package hyper.run.domain.banner.service;

import hyper.run.domain.banner.dto.request.BannerCreateRequest;
import hyper.run.domain.banner.dto.response.AdminBannerDetailResponse;
import hyper.run.domain.banner.dto.response.AdminBannerListResponse;
import hyper.run.domain.banner.entity.Banner;
import hyper.run.domain.banner.entity.BannerStatus;
import hyper.run.domain.banner.repository.BannerRepository;
import hyper.run.utils.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerManagementService {

    private final BannerRepository bannerRepository;
    private final FileService fileService;

    private static final int REQUIRED_WIDTH = 786;
    private static final int REQUIRED_HEIGHT = 340;
    private static final String REQUIRED_CONTENT_TYPE = "image/png";

    public Page<AdminBannerListResponse> getBannerList(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Banner> banners;
        if (status == null || status.isEmpty()) {
            banners = bannerRepository.findAllOrderByDisplayOrder(pageable);
        } else {
            BannerStatus bannerStatus = BannerStatus.valueOf(status);
            banners = bannerRepository.findByStatusOrderByDisplayOrder(bannerStatus, pageable);
        }

        return banners.map(AdminBannerListResponse::from);
    }

    public AdminBannerDetailResponse getBannerDetail(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        return AdminBannerDetailResponse.from(banner);
    }

    @Transactional
    public Long createBanner(BannerCreateRequest request, MultipartFile imageFile) {
        log.info("Creating banner - title: {}, status: {}, displayOrder: {}",
                request.getTitle(), request.getStatus(), request.getDisplayOrder());

        // 1. Request 검증
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            throw new IllegalArgumentException("배너 상태는 필수입니다. 전달된 값: null");
        }

        // 2. 이미지 검증
        validateBannerImage(imageFile);

        // 3. S3 업로드 (비동기)
        String imageUrl = uploadBannerImage(imageFile);

        // 4. 엔티티 생성
        BannerStatus status;
        try {
            status = BannerStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 배너 상태입니다: " + request.getStatus());
        }

        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(imageUrl)
                .displayOrder(request.getDisplayOrder())
                .status(status)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        // 4. 저장
        Banner saved = bannerRepository.save(banner);
        log.info("Banner created. bannerId: {}, title: {}", saved.getId(), saved.getTitle());
        return saved.getId();
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        // S3 파일 삭제 (비동기)
        fileService.deleteFile(banner.getImageUrl());

        // 엔티티 삭제
        bannerRepository.delete(banner);
        log.info("Banner deleted. bannerId: {}, title: {}", banner.getId(), banner.getTitle());
    }

    /**
     * 배너 이미지 검증
     * - PNG 형식 확인
     * - 이미지 크기 확인 (786x340)
     */
    private void validateBannerImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 필요합니다.");
        }

        // 형식 검증
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.equals(REQUIRED_CONTENT_TYPE)) {
            throw new IllegalArgumentException("PNG 형식만 업로드 가능합니다.");
        }

        // 크기 검증 (ImageIO 사용)
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
            }

            if (image.getWidth() != REQUIRED_WIDTH || image.getHeight() != REQUIRED_HEIGHT) {
                throw new IllegalArgumentException(
                        String.format("이미지 크기는 %dx%d 픽셀이어야 합니다. (현재: %dx%d)",
                                REQUIRED_WIDTH, REQUIRED_HEIGHT,
                                image.getWidth(), image.getHeight())
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.");
        }
    }

    /**
     * S3에 배너 이미지 업로드
     */
    private String uploadBannerImage(MultipartFile imageFile) {
        String url = fileService.toUrls(imageFile);
        fileService.fileUpload(imageFile, url);
        return url;
    }
}
