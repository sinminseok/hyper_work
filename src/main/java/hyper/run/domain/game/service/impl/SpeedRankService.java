//package hyper.run.domain.game.service.impl;
//
//import hyper.run.domain.game.dto.response.RankResponse;
//import hyper.run.domain.game.repository.GameRepository;
//import hyper.run.domain.game.service.GameRankService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
///**
// * 가장 멀리 달리고 있는 사람순서대로 순위 정하기
// */
//@Service
//@RequiredArgsConstructor
//public class SpeedRankService implements GameRankService {
//
//    private final GameRepository gameRepository;
//
//    @Override
//    public RankResponse calculateRank() {
//        return null;
//    }
//
//    @Override
//    @Scheduled(fixedRate = 5000)
//    public void generateGame() {
//
//    }
//}
