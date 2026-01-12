package hyper.run.controller.game;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.service.GameManagementService;
import hyper.run.dto.game.AdminGameListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/games")
@RequiredArgsConstructor
public class AdminGameController {

    private final GameManagementService gameManagementService;

    @GetMapping
    public String gameList(
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "gameType", required = false) GameType gameType,
            @RequestParam(value = "gameDistance", required = false) GameDistance gameDistance,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        // 날짜 기본값 설정 (최근 1개월)
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Page<AdminGameListResponse> games = gameManagementService.getGameList(
                status, startDate, endDate, gameType, gameDistance, page, size
        );

        model.addAttribute("games", games);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("gameType", gameType);
        model.addAttribute("gameDistance", gameDistance);
        model.addAttribute("gameTypes", GameType.values());
        model.addAttribute("gameDistances", GameDistance.values());
        model.addAttribute("page", page);
        model.addAttribute("currentUri", "/admin/games");

        return "game/list";
    }

    @GetMapping("/{gameId}")
    public String gameDetail(@PathVariable Long gameId, Model model) {
        Game game = gameManagementService.getGameDetail(gameId);
        model.addAttribute("game", game);
        model.addAttribute("currentUri", "/admin/games");
        return "game/detail";
    }
}
