package hyper.run.domain.outbox.entity;


import hyper.run.domain.outbox.data.AppleInAppData;

public enum OutboxEventType {
    APPLE_IN_APP(AppleInAppData.class);

    private final Class<? extends OutboxEventData> clazz;

    OutboxEventType(Class<? extends OutboxEventData> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends OutboxEventData> getClazz() {
        return clazz;
    }
}
