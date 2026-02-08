package hyper.run.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.service.GameHistoryCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 핸들러 - 실시간 경기 상태 전송
 * 성능 비교용 구현 (Polling vs WebSocket)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameHistoryCacheService gameHistoryCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 세션 관리: sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 사용자별 구독 정보: sessionId -> {gameId, userId}
    private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket 연결 성공: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("수신 메시지: sessionId={}, payload={}", session.getId(), payload);

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String action = (String) data.get("action");

            if ("subscribe".equals(action)) {
                // 구독 시작
                Long gameId = Long.valueOf(data.get("gameId").toString());
                Long userId = Long.valueOf(data.get("userId").toString());

                subscriptions.put(session.getId(), new SubscriptionInfo(gameId, userId));
                log.info("구독 시작: sessionId={}, gameId={}, userId={}", session.getId(), gameId, userId);

                // 즉시 현재 상태 전송
                sendGameStatus(session, gameId, userId);

            } else if ("unsubscribe".equals(action)) {
                // 구독 해제
                subscriptions.remove(session.getId());
                log.info("구독 해제: sessionId={}", session.getId());
            }

        } catch (Exception e) {
            log.error("메시지 처리 실패: sessionId={}, error={}", session.getId(), e.getMessage(), e);
            sendError(session, "Invalid message format");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        subscriptions.remove(session.getId());
        log.info("WebSocket 연결 종료: sessionId={}, status={}", session.getId(), status);
    }

    /**
     * 특정 사용자에게 게임 상태 전송
     */
    private void sendGameStatus(WebSocketSession session, Long gameId, Long userId) {
        try {
            GameInProgressWatchResponse status = gameHistoryCacheService.getUserStatus(gameId, userId);
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "status",
                    "data", status
            ));
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("게임 상태 전송 실패: sessionId={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    /**
     * 에러 메시지 전송
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", errorMessage
            ));
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("에러 메시지 전송 실패: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 모든 구독자에게 상태 브로드캐스트 (주기적 호출용)
     */
    public void broadcastGameStatus() {
        subscriptions.forEach((sessionId, info) -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                sendGameStatus(session, info.gameId, info.userId);
            }
        });
    }

    /**
     * 구독 정보 저장 클래스
     */
    private static class SubscriptionInfo {
        private final Long gameId;
        private final Long userId;

        public SubscriptionInfo(Long gameId, Long userId) {
            this.gameId = gameId;
            this.userId = userId;
        }
    }
}
