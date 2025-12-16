package hyper.run.domain.outbox.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import hyper.run.utils.GenericJsonConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonTypeIdResolver(OutboxEventData.OutboxEventTypeResolver.class)
@Getter
public abstract class OutboxEventData {
    public abstract OutboxEventType getType();

    @Converter
    public static class OutboxEventDataConverter extends GenericJsonConverter<OutboxEventData> {
        public OutboxEventDataConverter(ObjectMapper objectMapper) {
            super(objectMapper, new TypeReference<>() {
            });
        }
    }

    public static class OutboxEventTypeResolver extends TypeIdResolverBase {
        private static final Map<OutboxEventType, Class<? extends OutboxEventData>> TYPE_MAP = new HashMap<>();

        static {
            OutboxEventType[] types = OutboxEventType.values();
            for (OutboxEventType type : types) {
                TYPE_MAP.put(type, type.getClazz());
            }
        }

        @Override
        public String idFromValue(Object value) {
            if (value instanceof OutboxEventData) {
                return ((OutboxEventData) value).getType().name();
            }
            throw new IllegalArgumentException("Unknown type: " + value);
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return idFromValue(value);
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            OutboxEventType eventType = OutboxEventType.valueOf(id);
            Class<? extends OutboxEventData> eventClass = TYPE_MAP.get(eventType);
            if (eventClass == null) {
                throw new IllegalArgumentException("Unknown event type: " + id);
            }
            return context.constructType(eventClass);
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }
}