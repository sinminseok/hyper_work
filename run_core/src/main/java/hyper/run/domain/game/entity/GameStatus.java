package hyper.run.domain.game.entity;

public enum GameStatus {
    //todo AdminStatus enum 클래스 분리, 원래 enum 값들 복구
    SCHEDULED,          // 예정된 경기
    PROGRESS,           // 진행중인 경기
    IN_PROGRESS,        // 사용자의 경기 진행중
    FINISHED,           // 종료된 경기
    PARTICIPATE_FINISH, //내가 참여했던 경기중 끝난 경기
    REGISTRATION_COMPLETE, // 참가 신청완료
    REGISTRATION_OPEN      // 참가 신청 가능
}

