package hyper.run.common.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JobProcessorUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JobProcessorUtil() {
    }

    public static <T> T parseMessage(String message, Class<T> targetClass) {
        try {
            return mapper.readValue(message, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON message into " + targetClass.getSimpleName(), e);
        }
    }
}
