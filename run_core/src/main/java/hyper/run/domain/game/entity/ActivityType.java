package hyper.run.domain.game.entity;

public enum ActivityType {
    WALKING("걷기"),
    RUNNING("달리기");

    private final String name;

    ActivityType(String name) {
        this.name = name;
    }
}
