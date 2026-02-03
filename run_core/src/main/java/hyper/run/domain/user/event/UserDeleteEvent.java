package hyper.run.domain.user.event;

/**
 * 사용자 탈퇴 이벤트
 * 관련 데이터 정리 후 사용자 삭제 처리
 */
public record UserDeleteEvent(
        Long userId,
        String profileUrl
) {
    public static UserDeleteEvent from(Long userId, String profileUrl) {
        return new UserDeleteEvent(userId, profileUrl);
    }
}
