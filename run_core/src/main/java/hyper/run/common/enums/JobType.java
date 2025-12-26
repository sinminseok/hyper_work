package hyper.run.common.enums;

import hyper.run.common.job.JobEventPayload;
import hyper.run.domain.game.event.GameFinishedJobPayload;
import hyper.run.domain.payment.event.PaymentJobPayload;

public enum JobType {
    PAYMENT_CREATED("payment-created", PaymentJobPayload.class),
    GAME_CREATED("game-created", GameFinishedJobPayload.class),
    GAME_FINISHED("game-finished", GameFinishedJobPayload.class);

    private final String name;
    private final Class<? extends JobEventPayload> clazz;

    JobType(String name, Class<? extends JobEventPayload> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<? extends JobEventPayload> getClazz() {
        return clazz;
    }
}
