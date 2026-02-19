package hyper.run.domain.game.entity;

public enum GameType {
    SPEED("스피드"),
    HEARTBEAT("심박수"),
    GROUND_CONTACT_TIME("지면 접촉 시간"),
    VERTICAL_OSCILLATION("수직 진폭"),
    CADENCE("케이던스");

    private final String name;

    GameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
