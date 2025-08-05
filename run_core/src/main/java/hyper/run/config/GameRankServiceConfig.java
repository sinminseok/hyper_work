package hyper.run.config;

import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 각각의 GameType 별로 GameRankService 구현체를 Map 으로 주입하기 위한 사전 작업
 */
@Configuration
@RequiredArgsConstructor
public class GameRankServiceConfig {

    //GameRankService 를 이용한 구현체가 여러개 있으면 스프링이 자동으로 주입해줌
    private final List<GameRankService> gameRankServiceList;

    @Bean
    public Map<GameType, GameRankService> gameRankServices() {
        Map<GameType, GameRankService> map = new EnumMap<>(GameType.class);
        for (GameRankService service : gameRankServiceList) {
            map.put(service.getGameType(), service);
        }
        return map;
    }
}