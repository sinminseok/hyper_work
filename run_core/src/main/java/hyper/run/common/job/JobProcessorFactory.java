package hyper.run.common.job;

import hyper.run.common.message.SqsMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class JobProcessorFactory {

    private final Map<Enum<?>, JobProcessor<? extends SqsMessage>> processorMap = new HashMap<>();

    public JobProcessorFactory(List<JobProcessor<? extends SqsMessage>> jobProcessorList) {
        jobProcessorList.forEach(processor -> processorMap.put(processor.getType(), processor));
    }

    @SuppressWarnings("unchecked")
    public <T extends SqsMessage> JobProcessor<T> getProcessor(Enum<?> jobNameEnum) {
        return (JobProcessor<T>) processorMap.get(jobNameEnum);
    }
}
