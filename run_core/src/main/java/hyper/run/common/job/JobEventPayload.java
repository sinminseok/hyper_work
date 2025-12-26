package hyper.run.common.job;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import hyper.run.common.enums.JobType;
import hyper.run.utils.GenericJsonConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;


@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonTypeIdResolver(JobEventPayload.JobEventTypeResolver.class)
public abstract class JobEventPayload {
    public abstract JobType getType();

    @Converter
    public static class JobEventPayloadConverter extends GenericJsonConverter<JobEventPayload> {
        public JobEventPayloadConverter(ObjectMapper objectMapper) {
            super(objectMapper, new TypeReference<>() {
            });
        }
    }

    public static class JobEventTypeResolver extends TypeIdResolverBase {
        private static final Map<JobType, Class<? extends JobEventPayload>> TYPE_MAP = new HashMap<>();

        static {
            JobType[] types = JobType.values();
            for (JobType type : types) {
                TYPE_MAP.put(type, type.getClazz());
            }
        }

        @Override
        public String idFromValue(Object value) {
            if (value instanceof JobEventPayload) {
                return ((JobEventPayload) value).getType().name();
            }
            throw new IllegalArgumentException("Unknown type: " + value);
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return idFromValue(value);
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            JobType eventType = JobType.valueOf(id);
            Class<? extends JobEventPayload> eventClass = TYPE_MAP.get(eventType);
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