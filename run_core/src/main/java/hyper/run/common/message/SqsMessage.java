package hyper.run.common.message;

import hyper.run.common.enums.JobType;


public interface SqsMessage {
    JobType getJobType();
}
