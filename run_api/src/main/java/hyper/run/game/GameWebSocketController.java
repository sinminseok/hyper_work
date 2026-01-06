package hyper.run.game;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.service.GameHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class GameWebSocketController {

    private final SimpMessagingTemplate template;
    private final GameHistoryService service;

    /**
     * 자신의 데이터(심박수, 스피드 등)를 이용해 순위를 갱신하는 요청
     */
    @MessageMapping(value = "/game/update")
    public void sendMessage(final GameHistoryUpdateRequest gameHistoryUpdateRequest) {
        GameInProgressWatchResponse response = service.updateGameHistory(gameHistoryUpdateRequest);
        template.convertAndSend("/sub/game/my/" + gameHistoryUpdateRequest.getGameId() + "/" +gameHistoryUpdateRequest.getUserId(), response); //게임 결과 반영 (웹소켓으로 응답)
    }
}
