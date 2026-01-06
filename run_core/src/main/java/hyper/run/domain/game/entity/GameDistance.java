package hyper.run.domain.game.entity;

public enum GameDistance {
    ONE_KM_BY_WALK(20, "1KM", 1000),
    THREE_KM_BY_WALK(50, "3KM", 3000),
    FIVE_KM_BY_WALK(60, "5KM", 5000),
    NEWBIE_COURSE(50, "왕초보", 5000),       // 1시간, 5,000m
    FIVE_KM_COURSE(50, "5KM", 5000),       // 1시간, 5,000m
    TEN_KM_COURSE(90, "10KM", 10000),      // 2시간, 10,000m
    HALF_COURSE(180, "하프", 21097),         // 3시간, 하프 마라톤: 21,097m
    FULL_COURSE(300, "풀코스", 42195);       // 5시간, 풀 마라톤: 42,195m

    private final int time; // 분 단위
    private final String name;
    private final int distance;

    GameDistance(int time, String name, int distance) {
        this.time = time;
        this.name = name;
        this.distance = distance;
    }

    public boolean isWalk(){
        if(this == ONE_KM_BY_WALK || this ==  THREE_KM_BY_WALK || this == FIVE_KM_BY_WALK) return true;
        return false;
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
