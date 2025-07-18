package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

//    public List<GameResponse> findAll() {
//        //단 여기서 자신이 신청하지 않은 종료된 게임은 반환하지 않는다.
//        gameRepository
//    }

    @Transactional
    public void applyGame(final String userEmail, final Long gameId){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(userEmail), "존재하지 않는 사용자 이메일 입니다.");
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), "존재하지 않는 경기 아이디입니다.");
        user.applyGame(game);
    }
}
