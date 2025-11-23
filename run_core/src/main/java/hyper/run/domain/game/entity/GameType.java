package hyper.run.domain.game.entity;

public enum GameType {
    SPEED("스피드"),
    HEARTBEAT("심박수"),
    VERTICAL_OSCILLATION("수직 진폭"),
    GROUND_CONTACT_TIME("지면 접촉 시간"),
    //    POWER("강성"),
    CADENCE("케이던스");
//    FLIGHT_TIME("체공시간"),

    private final String name;

    GameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
