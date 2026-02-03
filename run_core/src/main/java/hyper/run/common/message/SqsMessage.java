package hyper.run.common.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hyper.run.common.enums.JobType;


public interface SqsMessage {
    @JsonIgnore
    JobType getJobType();
}
