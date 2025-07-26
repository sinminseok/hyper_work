package hyper.run.domain.game.entity;

public enum GameStatus {
    IN_PROGRESS,        // 경기 진행중
    PARTICIPATE_FINISH, //내가 참여했던 경기중 끝난 경기
    REGISTRATION_COMPLETE, // 참가 신청완료
    REGISTRATION_OPEN      // 참가 신청 가능
}

