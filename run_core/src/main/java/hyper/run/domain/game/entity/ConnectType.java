package hyper.run.domain.game.entity;

public enum ConnectType {
    NOT_STARTED("미시작"),
    WATCH("스마트 워치"),
    PHONE("스마트폰");

    private final String name;

    ConnectType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
