package hyper.run.domain.outbox.repository;

import hyper.run.common.enums.JobType;
import hyper.run.domain.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    /**
     * 특정 타입의 미발행 이벤트 조회
     */
    List<OutboxEvent> findByTypeAndIsPublishedFalse(JobType type);

    /**
     * 재시도 횟수 제한 이내의 미발행 이벤트 조회
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.type = :type AND o.isPublished = false AND o.retryCount < :maxRetryCount")
    List<OutboxEvent> findPendingEventsWithRetryLimit(@Param("type") JobType type, @Param("maxRetryCount") int maxRetryCount);
}
