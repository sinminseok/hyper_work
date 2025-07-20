package hyper.run.domain.game.entity;

public enum GameDistance {
    FIVE_KM_COURSE(1, "5KM"),     // 1시간
    TEN_KM_COURSE(2, "10KM"),      // 2시간
    HALF_COURSE(3, "하프"),        // 3시간
    FULL_COURSE(5, "풀코스");        // 5시간

    private final int time;
    private final String name;

    GameDistance(int time, String name) {
        this.time = time;
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public String getName() {
        return name;
    }
}
