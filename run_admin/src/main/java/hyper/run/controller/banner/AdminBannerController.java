package hyper.run.controller.banner;

import hyper.run.domain.banner.dto.request.BannerCreateRequest;
import hyper.run.domain.banner.dto.response.AdminBannerDetailResponse;
import hyper.run.domain.banner.dto.response.AdminBannerListResponse;
import hyper.run.domain.banner.entity.BannerStatus;
import hyper.run.domain.banner.service.BannerManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerManagementService bannerManagementService;

    /**
     * 배너 목록 조회
     */
    @GetMapping
    public String bannerList(
            @RequestParam(value = "status", required = false, defaultValue = "") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<AdminBannerListResponse> banners = bannerManagementService.getBannerList(status, page, size);

        model.addAttribute("banners", banners);
        model.addAttribute("status", status);
        model.addAttribute("bannerStatuses", BannerStatus.values());
        model.addAttribute("page", page);
        model.addAttribute("currentUri", "/banners");

        return "banner/list";
    }

    /**
     * 배너 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("bannerStatuses", BannerStatus.values());
        model.addAttribute("currentUri", "/banners");
        return "banner/form";
    }

    /**
     * 배너 생성
     */
    @PostMapping
    public String createBanner(
            @RequestParam("title") String title,
            @RequestParam("displayOrder") Integer displayOrder,
            @RequestParam("status") String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        log.info("Received banner creation request - title: {}, status: {}, displayOrder: {}, imageFile: {}",
                title, status, displayOrder, imageFile.getOriginalFilename());

        try {
            BannerCreateRequest request = BannerCreateRequest.builder()
                    .title(title)
                    .displayOrder(displayOrder)
                    .status(status)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            Long bannerId = bannerManagementService.createBanner(request, imageFile);
            redirectAttributes.addFlashAttribute("message", "배너가 성공적으로 등록되었습니다.");
            return "redirect:/banners/" + bannerId;
        } catch (IllegalArgumentException e) {
            log.error("Banner creation failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/banners/new";
        }
    }

    /**
     * 배너 상세 조회
     */
    @GetMapping("/{bannerId}")
    public String bannerDetail(@PathVariable Long bannerId, Model model) {
        AdminBannerDetailResponse banner = bannerManagementService.getBannerDetail(bannerId);
        model.addAttribute("banner", banner);
        model.addAttribute("currentUri", "/banners");
        return "banner/detail";
    }

    /**
     * 배너 삭제 (AJAX)
     */
    @DeleteMapping("/{bannerId}")
    @ResponseBody
    public ResponseEntity<String> deleteBanner(@PathVariable Long bannerId) {
        try {
            bannerManagementService.deleteBanner(bannerId);
            return ResponseEntity.ok("배너가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("Banner deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
