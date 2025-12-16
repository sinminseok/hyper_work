package hyper.run.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.AttributeConverter;
import java.io.IOException;

@Slf4j
public abstract class GenericJsonConverter<T> implements AttributeConverter<T, String> {

    private final ObjectMapper objectMapper;
    private final TypeReference<T> typeReference;

    public GenericJsonConverter(ObjectMapper objectMapper, TypeReference<T> typeReference) {
        this.objectMapper = objectMapper;
        this.typeReference = typeReference;
    }

    @Override
    public String convertToDatabaseColumn(T additionalData) {
        try {
            return objectMapper.writeValueAsString(additionalData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T convertToEntityAttribute(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (IOException e) {
            log.error("Failed to read json str {}. due to", jsonStr, e);
            throw new RuntimeException(e);
        }
    }
}
