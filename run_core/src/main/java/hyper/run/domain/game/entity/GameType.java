package hyper.run.domain.game.entity;

public enum GameType {
    SPEED("스피드"),
    HEARTBEAT("심박수"),
    CADENCE("케이던스");

    private final String name;

    GameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
