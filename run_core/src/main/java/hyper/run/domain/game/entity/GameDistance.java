package hyper.run.domain.game.entity;

public enum GameDistance {
    FIVE_KM_COURSE(1, "5KM", 5000),       // 1시간, 5,000m
    TEN_KM_COURSE(2, "10KM", 10000),      // 2시간, 10,000m
    HALF_COURSE(3, "하프", 21097),         // 3시간, 하프 마라톤: 21,097m
    FULL_COURSE(5, "풀코스", 42195);       // 5시간, 풀 마라톤: 42,195m

    private final int time;
    private final String name;
    private final int distance;

    GameDistance(int time, String name, int distance) {
        this.time = time;
        this.name = name;
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }
}
