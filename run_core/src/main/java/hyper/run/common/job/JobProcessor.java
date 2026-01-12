package hyper.run.common.job;

import hyper.run.common.enums.JobType;
import hyper.run.common.message.SqsMessage;
import io.awspring.cloud.sqs.listener.Visibility;

public interface JobProcessor<T extends SqsMessage> {

    void process(T message, Visibility visibility);

    JobType getType();
}
